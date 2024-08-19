package org.jiwoo.back.supprotProgram.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.service.BusinessService;
import org.jiwoo.back.supprotProgram.aggregate.dto.SupportProgramDTO;
import org.jiwoo.back.supprotProgram.aggregate.entity.SupportProgram;
import org.jiwoo.back.supprotProgram.aggregate.entity.SupportProgramBusiness;
import org.jiwoo.back.supprotProgram.repository.SupportProgramBusinessRepository;
import org.jiwoo.back.supprotProgram.repository.SupportProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SupportProgramServiceImpl implements SupportProgramService {

    private final SupportProgramRepository supportProgramRepository;
    private final SupportProgramBusinessRepository supportProgramBusinessRepository;
    private final BusinessService businessService;
    private final RestTemplate restTemplate;

    @Value("${python.server.url.support}")
    private String pythonSupportUrl;

    @Autowired
    public SupportProgramServiceImpl(SupportProgramRepository supportProgramRepository,
                                     SupportProgramBusinessRepository supportProgramBusinessRepository,
                                     BusinessService businessService,
                                     @Qualifier("defaultTemplate") RestTemplate restTemplate) {
        this.supportProgramRepository = supportProgramRepository;
        this.supportProgramBusinessRepository = supportProgramBusinessRepository;
        this.businessService = businessService;
        this.restTemplate = restTemplate;
    }


    @Override
    public void insertSupportProgram(List<SupportProgramDTO> supportProgramDTO) {

        for (SupportProgramDTO program : supportProgramDTO) {

            SupportProgram supportProgram = supportProgramRepository.save(dtoToEntity(program));
            int supportProgramId = supportProgram.getId();
            List<String> similarBusinessesName = findSimilarBusinesses(program);

            for (String businessName : similarBusinessesName) {

                int businessId = businessService.findBusinessByName(businessName).getId();

                supportProgramBusinessRepository.save(SupportProgramBusiness.builder()
                        .supportProgramId(supportProgramId)
                        .businessId(businessId)
                        .build());
            }
        }
    }


    private SupportProgram dtoToEntity(SupportProgramDTO dto) {

        return SupportProgram.builder()
                .name(dto.getName())
                .target(dto.getTarget())
                .scareOfSupport(dto.getScareOfSupport())
                .supportContent(dto.getSupportContent())
                .supportCharacteristics(dto.getSupportCharacteristics())
                .supportInfo(dto.getSupportInfo())
                .supportYear(dto.getSupportYear())
                .originUrl(dto.getOriginUrl())
                .build();
    }


    private List<String> findSimilarBusinesses(SupportProgramDTO supportProgramDTO) {

        List<String> businessNames = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> queryMap = new HashMap<>();

            queryMap.put("name", supportProgramDTO.getName());
            queryMap.put("target", supportProgramDTO.getTarget());
            queryMap.put("scare_of_support", supportProgramDTO.getScareOfSupport());
            queryMap.put("support_content", supportProgramDTO.getSupportContent());
            queryMap.put("support_characteristics", supportProgramDTO.getSupportCharacteristics());
            queryMap.put("support_info", supportProgramDTO.getSupportInfo());
            queryMap.put("support_year", supportProgramDTO.getSupportYear());

            requestMap.put("query", queryMap);
            requestMap.put("k", 10);  // 유사한 결과의 수
            requestMap.put("threshold", 0.7);  // 유사도 임계값

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonSupportUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(response.getBody());

                for (JsonNode resultNode : responseJson) {
                    String businessName = resultNode.path("content").path("businessName").asText();
                    businessNames.add(businessName);
                }
                log.info("Successfully retrieved similar businesses: {}", businessNames);
            } else {
                log.warn("Unexpected response from Python server: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve similar businesses: {}", e.getMessage());
        }
        return businessNames;
    }
}
