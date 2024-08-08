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
public class VATServiceImpl implements VATService {

    private List<Map<String, String>> regularVatRates = new ArrayList<>(); // 일반 과세자
    private List<Map<String, String>> simplifiedVatRates = new ArrayList<>(); // 간이 과세자
    private LocalDate latestVATDate;

    // 날짜 포맷(YYYY.MM.DD)
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('.')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral('.')
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
            .toFormatter(Locale.getDefault())
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public Map<String, List<Map<String, String>>> getVATInfo() {
        Map<String, List<Map<String, String>>> vatInfo = new HashMap<>();
        vatInfo.put("regularVat", regularVatRates);
        vatInfo.put("simplifiedVat", simplifiedVatRates);
        return vatInfo;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateVATRates() {
        String url = "https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?mi=2275&cntntsId=7696";
        try {
            Document doc = Jsoup.connect(url).get();
            Element latestRegularVatTable = findLatestRegularVatTable(doc);
            Element latestSimplifiedVatTable = findLatestSimplifiedVatTable(doc);


            if (latestRegularVatTable != null) {
                this.regularVatRates.clear();
                List<Map<String, String>> rates = extractTaxRatesFromTable(latestRegularVatTable, "부가가치세", "일반과세자");
                this.regularVatRates.addAll(rates);
            }

            if (latestSimplifiedVatTable != null) {
                this.simplifiedVatRates.clear();
                List<Map<String, String>> rates = extractTaxRatesFromTable(latestSimplifiedVatTable, "부가가치세", "간이과세자");
                this.simplifiedVatRates.addAll(rates);
            }

            this.latestVATDate =  calculateLatestVATDate(doc);

            log.info("일반 과세자 정보: {}", this.regularVatRates);
            log.info("간이 과세자 정보: {}", this.simplifiedVatRates);
            log.info("Latest VAT date: {}", this.latestVATDate);
        } catch (IOException e) {
            log.error("부가가치세 정보를 가져오는 중 에러가 발생했습니다.", e);
        }
    }

    //일반과세자 최신정보
    private Element findLatestRegularVatTable(Document doc) {
        Elements tables = doc.select("div.tbl_st > table");
        Element latestTable = null;
        LocalDate latestDate = LocalDate.MIN;
        int currentYear = LocalDate.now().getYear();

        for (Element table : tables) {
            Element caption = table.selectFirst("caption");
            if (caption != null && caption.text().contains("일반과세자")) {
                Optional<LocalDate> tableDate = extractDateFromCaption(caption.text(), currentYear);

                if (tableDate.isEmpty() && latestTable == null) {
                    // 날짜가 없는 첫 번째 일반과세자 테이블을 선택
                    latestTable = table;
                } else if (tableDate.isPresent() && tableDate.get().isAfter(latestDate)) {
                    latestDate = tableDate.get();
                    latestTable = table;
                }
            }
        }

        return latestTable;
    }

    // 간이과세자 최신정보
    private Element findLatestSimplifiedVatTable(Document doc) {
        Elements tables = doc.select("div.tbl_st > table");
        Element latestTable = null;
        LocalDate latestDate = LocalDate.MIN;
        int currentYear = LocalDate.now().getYear();

        for (Element table : tables) {
            Element caption = table.selectFirst("caption");
            if (caption != null && caption.text().contains("간이과세자")) {
                Optional<LocalDate> tableDate = extractDateFromCaption(caption.text(), currentYear);

                if (tableDate.isPresent() && tableDate.get().isAfter(latestDate)) {
                    latestDate = tableDate.get();
                    latestTable = table;
                }
            }
        }

        return latestTable;
    }

    private LocalDate calculateLatestVATDate(Document doc) {
        Elements captions = doc.select("div.tbl_st > table > caption");
        LocalDate latestDate = LocalDate.MIN;
        int currentYear = LocalDate.now().getYear();

        for (Element caption : captions) {
            Optional<LocalDate> tableDate = extractDateFromCaption(caption.text(), currentYear);

            if (tableDate.isPresent() && tableDate.get().isAfter(latestDate)) {
                latestDate = tableDate.get();
            }
        }

        return latestDate != LocalDate.MIN ? latestDate : null;
    }


    private Optional<LocalDate> extractDateFromCaption(String caption, int currentYear) {

        log.info("extracDateFromCaption 진입 후 caption : " + caption);

        try {
            Pattern pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
            Matcher matcher = pattern.matcher(caption);

            if (matcher.find()) {
                String dateText = matcher.group();
                log.info("\n*****YYYY.MM.DD 형식 : " + dateText);
                return Optional.of(LocalDate.parse(dateText, DATE_FORMATTER));
            }
        } catch (Exception e) {
            log.warn("캡션에서 날짜 파싱 실패 : {}", caption, e);
        }
        return Optional.empty();
    }


    private List<Map<String, String>> extractTaxRatesFromTable(Element table, String category, String vatType) {
        List<Map<String, String>> taxRates = new ArrayList<>();
        Elements rows = table.select("tbody > tr");
        log.info("extractTaxRatesFromTable 과세타입 : {}", vatType);

        for (Element row : rows) {
            if (row.select("th").size() > 0) {
                continue;
            }
            Elements cols = row.select("td");
            log.info("Row columns: {}", cols.size());

            if (cols.size() >= 2) {
                String description = cols.get(0).text().trim();
                String taxRate = cols.get(1).text().trim();
                log.info("Description: {}, Tax rate: {}", description, taxRate);

                Map<String, String> taxRateMap = new HashMap<>();
                taxRateMap.put("category", category);
                taxRateMap.put("description", description);
                taxRateMap.put("tax_rate", taxRate);
                taxRateMap.put("vatType", vatType);

                taxRates.add(taxRateMap);
            } else {
                log.warn("Skipping row with insufficient columns: {}", row.html());
            }
        }
        log.info("Extracted {} tax rates from table with VAT type: {}", taxRates.size(), vatType);
        return taxRates;
    }

    @Override
    public String getFormattedTaxRates() {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 날짜: ").append(LocalDate.now()).append("\n");

        log.info("latestVATDate: {}", latestVATDate);

        if (latestVATDate != null) {
            sb.append("부가가치세 - ").append(latestVATDate).append(" 기준\n");
            sb.append("일반 과세자:\n");
            for (Map<String, String> rate : regularVatRates) {
                sb.append("업종: ").append(rate.get("description")).append(", 세율: ").append(rate.get("tax_rate")).append("\n");
            }
            sb.append("간이 과세자:\n");
            for (Map<String, String> rate : simplifiedVatRates) {
                sb.append("업종: ").append(rate.get("description")).append(", 부가가치율: ").append(rate.get("tax_rate")).append("\n");
            }
        }
        return sb.toString();
    }
}

