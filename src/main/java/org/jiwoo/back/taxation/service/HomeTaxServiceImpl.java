package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HomeTaxServiceImpl implements HomeTaxService{

    @Value("${homeTax.api.url}")
    private String apiUrl;

    @Value("${homeTax.api.serviceKey}")
    private String serviceKey;

    @Value("${homeTax.api.authorization}")
    private String authorization;

    @Qualifier("defaultTemplate")
    private final RestTemplate restTemplate;

    public HomeTaxServiceImpl(@Qualifier("defaultTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getBusinessType(String businessCode) {

        log.info("\n***** 서비스 잘 들어왔어");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("b_no", new String[]{businessCode});
        log.info("\n***** 사업자 등록 번호 : " + businessCode);
        log.info("\n***** requestData : " + requestData.get("b_no"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        log.info("\n***** apiUrl : " + apiUrl);
        log.info("\n***** serviceKey : " + serviceKey);
        log.info("\n***** authorization : " + authorization);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

        try {
            String url = String.format("%s?serviceKey=%s", apiUrl, serviceKey);
            URI uri = new URI(url);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);
            log.info("\n***** api 호출하고 돌아옴");
            Map<String, Object> response = responseEntity.getBody();

            if(response != null && response.containsKey("data")){
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");

                if(!dataList.isEmpty()){
                    Map<String, Object> data = dataList.get(0);
                    return (String) data.get("tax_type");
                }

            }

            return "API로부터 유효한 응답이 없습니다.";

        } catch (HttpClientErrorException e) {

            log.error("API 요청 중 에러 발생: " + e.getMessage());
            return "API 요청 중 에러 발생: " + e.getMessage();

        } catch (Exception e) {

            log.error("기타 에러 발생: " + e.getMessage());
            return "기타 에러 발생: " + e.getMessage();

        }
    }
}
