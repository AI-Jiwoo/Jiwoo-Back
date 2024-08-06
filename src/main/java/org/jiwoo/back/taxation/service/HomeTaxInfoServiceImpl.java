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
public class HomeTaxInfoServiceImpl implements HomeTaxInfoService {

    //부가가치세
    private List<Map<String, String>> vatRates = new ArrayList<>(); //일반 과세자
    private List<Map<String, String>> simplifiedVatRates = new ArrayList<>(); //간이 과세자
    private LocalDate latestVATDate;

    //종합소득세
    private List<Map<String, String>> incomeTaxRates = new ArrayList<>();
    private LocalDate latestIncomeTaxDate;
    private String latestIncomeTaxYear;


    //날짜 포맷(YYYY.MM.DD)
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('.')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral('.')
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
            .toFormatter(Locale.getDefault())
            .withResolverStyle(ResolverStyle.STRICT);


    public List<Map<String, String>> getVATInfo() {
        String result = getFormattedTaxRates("부가가치세");

        return vatRates;
    }

    public List<Map<String, String>> getIncomeTaxRates() {
        return incomeTaxRates;
    }

    // 정기적으로 세율 업데이트 (매일 자정)
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void updateTaxRates() {

        updateVATRates();
        updateIncomeTaxRates();
    }

    // 부가가치세 정보 업데이트
    private void updateVATRates() {

        String url = "https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?mi=2275&cntntsId=7696";
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("구분", "부가가치세");
        this.vatRates = updateInfo(url, categoryMap);
    }

    // 종합소득세 정보 업데이트
    private void updateIncomeTaxRates() {

        String url = "https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?mi=2227&cntntsId=7667";
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("구분", "종합소득세");
        this.incomeTaxRates = updateInfo(url, categoryMap);
    }

    //업데이트
    private List<Map<String, String>> updateInfo(String url, Map<String, String> categoryMap){
        List<Map<String, String>> updateInfo = new ArrayList<>();
        String category = categoryMap.get("구분");

        try {
            //url 크롤링
            Document doc = Jsoup.connect(url).get();

            //가장 최신 날짜를 기준으로 테이블 선택
            Element latestTable = findLatestTable(doc, category);
            if(latestTable != null){
                log.info("\n***** " + category + "의 최신 정보가 있대요");
                updateInfo.addAll(extractTaxRatesFromTable(latestTable, category));
            }else{
                log.info("\n***** " + category + "의 최신 정보가 없대요");
            }

        } catch (IOException e) {
            log.error("\n*****" + category + "를 가져오는 중 에러가 발생했습니다.", e);
        }

        log.info("\n***** 정보 : " + updateInfo.get(0).toString());
        return updateInfo;
    }


    //최신 날짜 테이블 찾기
    private Element findLatestTable(Document doc, String category) {
        Elements tables = doc.select("table");
        Element latestTable = null;
        LocalDate latestDate = LocalDate.MIN;
        int currentYear = LocalDate.now().getYear();
        LocalDate fallbackDate = LocalDate.MIN;

        for (Element table : tables) {
            Element caption = table.selectFirst("caption");
            if (caption != null) {
                String captionText = caption.text();
                LocalDate tableDate = extractDateFromCaption(captionText, category, currentYear).orElse(null);
//                System.out.println("\n***** 기준 날짜 : " + tableDate);
                log.info("\n***** 기준 날짜 : " + tableDate);

                if (tableDate != null && tableDate.isAfter(latestDate)) {
                    latestDate = tableDate;
                    latestTable = table;

                    // 종합소득세의 경우 최신 연도 정보 저장
                    if(category.equals("종합소득세")){
                        this.latestIncomeTaxYear = extractYearFromCaption(captionText);
                    }
                }

                //기준 날짜가 없으면 가장 최신 날짜 저장
                if(tableDate != null && tableDate.isAfter(fallbackDate)){
                    log.info("\n***** 기준 날짜가 없어서 최신 날짜 저장");
                    fallbackDate = tableDate;
                    log.info("\n***** fallbackDate : " + fallbackDate);
                }

            }
        }
        //최신 날짜가 없으면 가장 최신 테이블로 설정
        if(latestTable == null && fallbackDate != LocalDate.MIN){
            for(Element table : tables){
                Element caption = table.selectFirst("caption");
                if(caption != null){
                    String captionText = caption.text();
                    LocalDate tableDate = extractDateFromCaption(captionText, category, fallbackDate.getYear()).orElse(null);

                    if(tableDate != null && tableDate.equals(fallbackDate)){
                        latestTable = table;
                        if(category.equals("종합소득세")){
                            this.latestIncomeTaxYear = extractYearFromCaption(captionText);
                        }
                        break;
                    }
                }
            }
            log.info("\n***** 기준 날짜가 없어 최신 테이블을 선택하였습니다.");
        }

        // 최신 날짜 저장
        if (category.equals("부가가치세")) {
            this.latestVATDate = latestDate != LocalDate.MIN ? latestDate : fallbackDate;
        } else {
            this.latestIncomeTaxDate = latestDate != LocalDate.MIN ? latestDate : fallbackDate;
        }

        return latestTable;
    }


    //기준 날짜 가져오기
    private Optional<LocalDate> extractDateFromCaption(String caption, String category, int currentYear) {
        try {
            Pattern pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
            Matcher matcher = pattern.matcher(caption);

            if (matcher.find()) {   //YYYY.MM.DD 형식의 날짜가 있으면
                String dateText = matcher.group();
                log.info("*****\nYYYY.MM.DD 형식 : " + dateText);
                return Optional.of(LocalDate.parse(dateText, DATE_FORMATTER));
            }
            else if(category.equals("종합소득세")){  //종합소득세의 경우 연도 범위를 찾음

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
                        }else{
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
//            e.printStackTrace();
        }
        return Optional.empty();
    }

    private String extractYearFromCaption(String caption) {
        try{
            Pattern pattern = Pattern.compile("\\d{4}");
            Matcher matcher = pattern.matcher(caption);
            StringBuilder years = new StringBuilder();
            while(matcher.find()){
                if(years.length() > 0){
                    years.append("-");
                }
                years.append(matcher.group());
            }
            return years.toString();
        }catch (Exception e){
            log.warn("캡션에서 연도 파싱 실패 : {}", caption, e);
        }
        return "";
    }

    private List<Map<String, String>> extractTaxRatesFromTable(Element table, String category) {
        List<Map<String, String>> taxRates = new ArrayList<>();
        Elements rows = table.select("tr");
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() > 1) {
                String description = cols.get(0).text().trim();
                String taxRate = cols.get(1).text().trim();
                Map<String, String> taxRateMap = new HashMap<>();

                taxRateMap.put("category", category);
                taxRateMap.put("description", description);
                taxRateMap.put("tax_rate", taxRate);

                if (category.equals("종합소득세") && cols.size() > 2) {
                    String additionalInfo = cols.get(2).text().trim();
                    taxRateMap.put("additional_info", additionalInfo);
                }
                taxRates.add(taxRateMap);
            }
        }
        return taxRates;
    }

    //문자열로 반환
    public String getFormattedTaxRates(String category) {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 날짜: ").append(LocalDate.now()).append("\n");

        if(category.equals("부가가치세")){
            if (latestVATDate != null) {
                sb.append("부가가치세 - ").append(latestVATDate).append(" 기준\n");
                for (Map<String, String> rate : vatRates) {
                    sb.append("업종: ").append(rate.get("description")).append(", 부가가치율: ").append(rate.get("tax_rate")).append("\n");
                }
            }
        }

        if(category.equals("종합소득세")){
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
        }


        return sb.toString();
    }



}
