package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IncomeTaxServiceImpl implements IncomeTaxService {

    private List<Map<String, String>> incomeTaxRates = new ArrayList<>();
    private LocalDate latestIncomeTaxDate;
    private String latestIncomeTaxYear;

    // 날짜 포맷(YYYY.MM.DD)
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('.')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral('.')
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
            .toFormatter(Locale.getDefault())
            .withResolverStyle(ResolverStyle.STRICT);


    // 종합소득세 정보 반환
    @Override
    public List<Map<String, String>> getIncomeTaxRates() {
        return incomeTaxRates;
    }

    // 종합소득세 정보 업데이트 (매일 자정에 실행)
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateIncomeTaxRates() {
        String url = "https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?mi=2227&cntntsId=7667";
        try {
            Document doc = Jsoup.connect(url).get();
            List<Element> relevantTables = findLatestTablesForIncomeTax(doc);

            this.incomeTaxRates.clear();

            for (Element table : relevantTables) {
                this.incomeTaxRates.addAll(extractTaxRatesFromIncomeTaxTable(table));
            }

            // 최신 날짜 설정
            this.latestIncomeTaxDate = relevantTables.stream()
                    .map(table -> extractDateFromCaption(table.selectFirst("caption").text(), "종합소득세", LocalDate.now().getYear()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            this.latestIncomeTaxYear = latestIncomeTaxDate != null ? String.valueOf(latestIncomeTaxDate.getYear()) : null;

            log.info("Latest Income Tax Date: {}", this.latestIncomeTaxDate);
            log.info("종합소득세 정보: {}", this.incomeTaxRates);
        } catch (IOException e) {
            log.error("종합소득세 정보를 가져오는 중 에러가 발생했습니다.", e);
        }
    }


    // 최신 날짜 테이블 찾기 (종합소득세)
    private List<Element> findLatestTablesForIncomeTax(Document doc) {
        List<Element> relevantTables = new ArrayList<>();
        Elements tables = doc.select("div.tbl_st > table");
        LocalDate currentDate = LocalDate.now();
        LocalDate closestDate = null;

        for (Element table : tables) {
            Element caption = table.selectFirst("caption");
            if (caption != null) {
                String captionText = caption.text().toLowerCase();
                log.info("Table caption for Income Tax: {}", captionText);
                if (captionText.contains("종합소득세")) {
                    Optional<LocalDate> tableDate = extractDateFromCaption(captionText, "종합소득세", currentDate.getYear());
                    if (tableDate.isPresent()) {
                        if (closestDate == null || tableDate.get().isAfter(closestDate)) {
                            closestDate = tableDate.get();
                            relevantTables.clear();
                            relevantTables.add(table);
                        } else if (tableDate.get().isEqual(closestDate)) {
                            relevantTables.add(table);
                        }
                    }
                }
            } else {
                log.warn("Skipped table without caption: {}", table);
            }
        }

        log.info("Found {} relevant tables for Income Tax", relevantTables.size());
        return relevantTables;
    }


    // 기준 날짜 가져오기
    private Optional<LocalDate> extractDateFromCaption(String caption, String category, int currentYear) {
        try {
            Pattern pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
            Matcher matcher = pattern.matcher(caption);

            if (matcher.find()) {   //YYYY.MM.DD 형식의 날짜가 있으면
                String dateText = matcher.group();
                log.info("\n*****YYYY.MM.DD 형식 : " + dateText);
                return Optional.of(LocalDate.parse(dateText, DATE_FORMATTER));
            } else if (category.equals("종합소득세")) {  //종합소득세의 경우 연도 범위를 찾음

                log.info("\n*****종합소득세 날짜 찾기");
                pattern = Pattern.compile("\\d{4}");
                matcher = pattern.matcher(caption);

                List<Integer> years = new ArrayList<>();
                while (matcher.find()) {
                    log.info("\n*****종합소득세 패턴 찾았다");
                    int year = Integer.parseInt(matcher.group());
                    log.info("\n*****종합소득세 연도 : " + year);
                    years.add(year);
                }

                // 연도 범위 처리
                if (!years.isEmpty()) {
                    if (years.size() == 1) {
                        int year = years.get(0);
                        if (year == currentYear) {
                            log.info("\n***** 현재 연도랑 똑같음");
                        } else {
                            log.info("\n***** 현재 연도랑 다름");
                        }
                        return Optional.of(LocalDate.of(year, 1, 1));
                    } else if (years.size() == 2) {
                        int startYear = years.get(0);
                        int endYear = years.get(1);
                        log.info("\n*****연속 연도래요: " + startYear + " ~ " + endYear);
                        if (currentYear >= startYear && currentYear <= endYear) {
                            return Optional.of(LocalDate.of(currentYear, 1, 1));
                        } else {
                            // 현재 연도가 범위 내에 포함되지 않으면 가장 최신 연도 반환
                            return Optional.of(LocalDate.of(endYear, 1, 1));
                        }
                    }
                }
                log.info("\n*****종합소득세 패턴 못찾음");
            }
        } catch (Exception e) {
            log.warn("캡션에서 날짜 파싱 실패 : {}", caption, e);
        }
        return Optional.empty();
    }


    // 종합소득세 테이블에서 세율 추출
    private List<Map<String, String>> extractTaxRatesFromIncomeTaxTable(Element table) {
        List<Map<String, String>> taxRates = new ArrayList<>();
        Elements rows = table.select("tr");
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() > 1) {
                String description = cols.get(0).text().trim();
                String taxRate = cols.get(1).text().trim();
                Map<String, String> taxRateMap = new HashMap<>();

                taxRateMap.put("category", "종합소득세");
                taxRateMap.put("description", description);
                taxRateMap.put("tax_rate", taxRate);

                if (cols.size() > 2) {
                    String additionalInfo = cols.get(2).text().trim();
                    taxRateMap.put("additional_info", additionalInfo);
                }
                taxRates.add(taxRateMap);
            }
        }
        return taxRates;
    }

    // 문자열로 반환
    @Override
    public String getFormattedTaxRates() {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 날짜: ").append(LocalDate.now()).append("\n");

        if (latestIncomeTaxDate != null) {
            sb.append("종합소득세 - ").append(latestIncomeTaxYear).append(" 기준\n");
            for (Map<String, String> rate : incomeTaxRates) {
                sb.append("과세표준: ").append(rate.get("description"))
                        .append(", 세율: ").append(rate.get("tax_rate"));
                if (rate.containsKey("additional_info")) {
                    sb.append(", 누진공세: ").append(rate.get("additional_info"));
                }
                sb.append("\n");
            }

        }

        return sb.toString();
    }
}
