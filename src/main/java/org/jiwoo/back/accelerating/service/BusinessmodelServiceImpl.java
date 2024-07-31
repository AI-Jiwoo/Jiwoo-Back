package org.jiwoo.back.accelerating.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.accelerating.aggregate.vo.ResponsePythonServerVO;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.tomcat.util.http.FastHttpDateFormat.formatDate;

@Service
@Slf4j
public class BusinessmodelServiceImpl implements BusinessmodelService {

    private final CategoryService categoryService;
    private final RestTemplate restTemplate;

    @Value("${python.server.url.search}")
    private String pythonServerUrl;

    @Autowired
    public BusinessmodelServiceImpl(CategoryService categoryService, @Qualifier("defaultTemplate") RestTemplate restTemplate) {
        this.categoryService = categoryService;
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<List<ResponsePythonServerVO>> getSimilarServices(BusinessDTO businessDTO) {
        String categoryNames = categoryService.getCategoryNameByBusinessId(businessDTO.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = createRequestBody(businessDTO);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<List<ResponsePythonServerVO>> response = restTemplate.exchange(
                    pythonServerUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<ResponsePythonServerVO>>() {}
            );
            log.info("Similar services retrieved successfully for business ID: {}", businessDTO.getId());
            return response;
        } catch (Exception e) {
            log.error("Error retrieving similar services for business ID: {}", businessDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(new ResponsePythonServerVO("Error", null, 0.0)));
        }
    }

    private Map<String, Object> createRequestBody(BusinessDTO businessDTO) {
        Map<String, Object> requestBody = new HashMap<>();
        String categoryName = null;
        try {
            categoryName = categoryService.getCategoryNameByBusinessId(businessDTO.getId());
        } catch (Exception e) {
            log.warn("Failed to retrieve category name for business ID: {}", businessDTO.getId(), e);
            categoryName = "Unknown";
        }
        requestBody.put("businessPlatform", businessDTO.getBusinessPlatform());
        requestBody.put("businessScale", businessDTO.getBusinessScale());
        requestBody.put("business_field", categoryName);
        requestBody.put("businessStartDate", formatDate(businessDTO.getBusinessStartDate()));
        requestBody.put("investmentStatus", businessDTO.getInvestmentStatus());
        requestBody.put("customerType", businessDTO.getCustomerType());
        return requestBody;
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}

