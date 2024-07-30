package org.jiwoo.back.marketresearch.service;

import org.jiwoo.back.marketresearch.aggregate.vo.ResponsePythonServerVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.category.service.CategoryService;
import org.jiwoo.back.marketresearch.dto.MarketSizeGrowthDTO;
import org.jiwoo.back.common.OpenAI.service.OpenAIService;
import org.jiwoo.back.marketresearch.dto.SimilarServicesAnalysisDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketResearchServiceImpl implements MarketResearchService {

    private final OpenAIService openAIService;
    private final CategoryService categoryService;
    private final RestTemplate restTemplate;

    @Value("${python.server.url.search}")
    private String pythonServerUrl;

    public MarketResearchServiceImpl(OpenAIService openAIService, CategoryService categoryService, @Qualifier("defaultTemplate") RestTemplate restTemplate) {
        this.openAIService = openAIService;
        this.categoryService = categoryService;
        this.restTemplate = restTemplate;
    }

    /* 설명. 시장 규모, 성장률 조회 */
    @Override
    public MarketSizeGrowthDTO getMarketSizeAndGrowth(BusinessDTO businessDTO) {
        try {
            String categoryNames = categoryService.getCategoryNameByBusinessId(businessDTO.getId());
            String prompt = generatePrompt(businessDTO, categoryNames);
            log.info("Generated prompt: {}", prompt);

            String response = openAIService.generateAnswer(prompt);
            log.info("Received response from OpenAI: {}", response);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Error in getMarketSizeAndGrowth", e);
            throw new RuntimeException("Failed to get market size and growth data: " + e.getMessage(), e);
        }
    }

    private String generatePrompt(BusinessDTO businessDTO, String categoryNames) {
        return String.format(
                "다음 정보를 바탕으로 %s 분야의 IT 시장 규모와 성장률 정보를 제공해주세요:\n" +
                        "사업 분야: %s\n" +
                        "사업 규모: %s\n" +
                        "국가: %s\n" +
                        "고객유형: %s\n" +
                        "사업유형: %s\n" +
                        "사업내용: %s\n\n" +
                        "응답은 반드시 다음 형식을 따라주시고, 가능한 한 상세히 설명해주세요:\n" +
                        "시장 규모: [금액] (구체적인 수치와 함께 시장 규모에 대한 부연 설명)\n" +
                        "성장률: [비율] (성장률에 대한 추가 정보, 예: 투자 동향, 시장 전망 등)\n\n" +
                        "예시:\n" +
                        "시장 규모: 약 100억원으로 추정됩니다. 이는 작년 대비 15%% 증가한 수치이며, 주요 기업들의 적극적인 투자로 인해 시장이 확대되고 있습니다.\n" +
                        "성장률: 연간 20%% 성장 중이며, 특히 외국인 투자가 크게 증가하고 있습니다. 향후 5년간 이러한 성장세가 지속될 것으로 전망됩니다.\n\n" +
                        "만약 정확한 정보를 제공할 수 없다면, 대략적인 추정치나 관련 산업의 통계를 활용하여 설명해주세요. 완전히 알 수 없는 경우에만 '정보 없음'으로 표시해주세요.",
                categoryNames,
                categoryNames,
                businessDTO.getBusinessScale(),
                businessDTO.getNation(),
                businessDTO.getCustomerType(),
                businessDTO.getBusinessPlatform(),
                businessDTO.getBusinessContent()
        );
    }

    private MarketSizeGrowthDTO parseResponse(String response) {
        log.info("Parsing response: {}", response);
        String marketSize = "정보 없음";
        String growthRate = "정보 없음";

        int marketSizeStart = response.indexOf("시장 규모:");
        int marketSizeEnd = response.indexOf("성장률:");
        if (marketSizeStart != -1 && marketSizeEnd != -1) {
            marketSize = response.substring(marketSizeStart + 6, marketSizeEnd).trim();
        } else if (marketSizeStart != -1) {
            marketSize = response.substring(marketSizeStart + 6).trim();
        }

        int growthRateStart = response.indexOf("성장률:");
        if (growthRateStart != -1) {
            growthRate = response.substring(growthRateStart + 4).trim();
        }

        return new MarketSizeGrowthDTO(marketSize, growthRate);
    }

    /* 설명. 유사 서비스 분석 조회 */
    @Override
    public SimilarServicesAnalysisDTO analyzeSimilarServices(BusinessDTO businessDTO) {
        try {
            String categoryNames = categoryService.getCategoryNameByBusinessId(businessDTO.getId());
            List<ResponsePythonServerVO> similarServices = getSimilarServicesFromPythonServer(businessDTO);

            String analysis = analyzeWithOpenAI(businessDTO, similarServices, categoryNames);
            return new SimilarServicesAnalysisDTO(convertToStringList(similarServices), analysis);
        } catch (Exception e) {
            log.error("Error in analyzeSimilarServices", e);
            throw new RuntimeException("Failed to analyze similar services: " + e.getMessage(), e);
        }
    }

    private List<ResponsePythonServerVO> getSimilarServicesFromPythonServer(BusinessDTO businessDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = createRequestBody(businessDTO);

        log.debug("BusinessDTO: {}", businessDTO);
        log.debug("Request body: {}", requestBody);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<List<ResponsePythonServerVO>> response = restTemplate.exchange(
                    pythonServerUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<ResponsePythonServerVO>>() {}
            );
            List<ResponsePythonServerVO> body = response.getBody();
            log.debug("Response from Python server: {}", body);
            return body != null ? body : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("Error from Python server: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get similar services from Python server", e);
        }
    }

    private Map<String, Object> createRequestBody(BusinessDTO businessDTO) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", businessDTO.getId());
        requestBody.put("businessName", businessDTO.getBusinessName());
        requestBody.put("businessNumber", businessDTO.getBusinessNumber());
        requestBody.put("businessScale", businessDTO.getBusinessScale());
        requestBody.put("businessBudget", businessDTO.getBusinessBudget());
        requestBody.put("businessContent", businessDTO.getBusinessContent());
        requestBody.put("businessPlatform", businessDTO.getBusinessPlatform());
        requestBody.put("businessLocation", businessDTO.getBusinessLocation());
        requestBody.put("businessStartDate", formatDate(businessDTO.getBusinessStartDate()));
        requestBody.put("nation", businessDTO.getNation());
        requestBody.put("investmentStatus", businessDTO.getInvestmentStatus());
        requestBody.put("customerType", businessDTO.getCustomerType());
        requestBody.put("business_field", categoryService.getCategoryNameByBusinessId(businessDTO.getId()));
        return requestBody;
    }

    String formatDate(Date date) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (Exception e) {
            log.error("Error formatting date: {}", date, e);
            return null;
        }
    }

    private String analyzeWithOpenAI(BusinessDTO businessDTO, List<ResponsePythonServerVO> similarServices, String categoryNames) throws Exception {
        String prompt = createSimilarServicesPrompt(businessDTO, similarServices, categoryNames);
        log.info("Generated prompt: {}", prompt);
        return openAIService.generateAnswer(prompt);
    }

    private String createSimilarServicesPrompt(BusinessDTO businessDTO, List<ResponsePythonServerVO> similarServices, String categoryNames) {
        String similarServicesString = convertToString(similarServices);
        return String.format(
                "유사 서비스를 제공하는 기업들을 분석해주세요(강점, 약점, 특징, 전략):\n" +
                        "유사한 서비스를 제공하는 기업: %s\n\n" +
                        "사업 내용: %s\n" +
                        "사업 분야: %s\n",
                        similarServicesString,
                businessDTO.getBusinessContent(),
                categoryNames
        );
    }

    private String convertToString(List<ResponsePythonServerVO> similarServices) {
        return similarServices.stream()
                .map(service -> String.format("%s (유사도: %.2f)", service.getBusinessName(), service.getSimilarityScore()))
                .collect(Collectors.joining(", "));
    }

    private List<String> convertToStringList(List<ResponsePythonServerVO> similarServices) {
        return similarServices.stream()
                .map(service -> String.format("%s (유사도: %.2f)", service.getBusinessName(), service.getSimilarityScore()))
                .collect(Collectors.toList());
    }
}