package org.jiwoo.back.accelerating.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.category.service.CategoryService;
import org.jiwoo.back.common.OpenAI.service.OpenAIService;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.accelerating.aggregate.vo.ResponsePythonServerVO;
import org.jiwoo.back.accelerating.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketResearchServiceImplTest {
    // 시장 규모, 성장률 조회
    private static final int BUSINESS_ID_1 = 1;
    private static final int BUSINESS_ID_2 = 2;
    private static final int BUSINESS_ID_3 = 3;
    private static final String BUSINESS_SCALE = "중소기업";
    private static final String NATION = "대한민국";
    private static final String CUSTOMER_TYPE = "B2B";
    private static final String BUSINESS_PLATFORM = "SaaS";
    private static final String BUSINESS_CONTENT = "AI 기반 데이터 분석";
    private static final String CATEGORY_NAMES = "IT, 인공지능";
    private static final String NEW_TECH_CATEGORY = "신기술";
    private static final String MOCK_RESPONSE = "시장 규모: 약 1000억원으로 추정됩니다. 이는 작년 대비 20% 증가한 수치입니다.\n" +
            "성장률: 연간 25% 성장 중이며, 향후 5년간 지속적인 성장이 예상됩니다.";
    private static final String MOCK_RESPONSE_NO_MARKET_SIZE = "성장률: 매우 빠르게 성장 중입니다.";
    private static final String EXPECTED_GROWTH_RATE_NO_MARKET_SIZE = "매우 빠르게 성장 중입니다.";

    private static final String EXPECTED_MARKET_SIZE = "약 1000억원으로 추정됩니다. 이는 작년 대비 20% 증가한 수치입니다.";
    private static final String EXPECTED_GROWTH_RATE = "연간 25% 성장 중이며, 향후 5년간 지속적인 성장이 예상됩니다.";
    private static final String NO_INFO = "정보 없음";
    private static final String DATABASE_ERROR = "Database error";

    // 유사 서비스 분석
    private static final String PYTHON_SERVER_URL = "http://localhost:8000/api";  // 테스트용 URL
    private static final String SIMILAR_SERVICES_ANALYSIS = "유사 서비스 분석 결과";
    private static final String SIMILAR_SERVICE_1 = "유사 서비스 1";
    private static final String SIMILAR_SERVICE_2 = "유사 서비스 2";
    private static final double SIMILARITY_SCORE_1 = 0.8;
    private static final double SIMILARITY_SCORE_2 = 0.7;

    // 트렌드 및 주요고객 조회
    private static final String MOCK_TREND_RESPONSE = "트렌드: AI 기술의 발전으로 데이터 분석 시장이 급성장하고 있습니다.\n" +
            "주요 고객: 20-40대 젊은 전문직 종사자들이 주요 고객층입니다.\n" +
            "기술 동향: 머신러닝과 딥러닝 기술이 지속적으로 발전하고 있습니다.";

    // 시장조사 이력 저장
    private static final String MARKET_INFORMATION = "시장 정보 테스트";
    private static final String COMPETITOR_ANALYSIS = "경쟁사 분석 테스트";
    private static final String MARKET_TRENDS = "시장 트렌드 테스트";
    private static final String REGULATION_INFORMATION = "규제 정보 테스트";
    private static final String MARKET_ENTRY_STRATEGY = "시장 진입 전략 테스트";

    @Mock
    private OpenAIService openAIService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;


    @InjectMocks
    private MarketResearchServiceImpl marketResearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(marketResearchService, "pythonServerUrl", PYTHON_SERVER_URL);
        ReflectionTestUtils.setField(marketResearchService, "jdbcTemplate", jdbcTemplate);
    }

    @DisplayName("사업규모와 성장률 조회 성공")
    @Test
    void getMarketSizeAndGrowth_Success() throws Exception {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_1);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_1)).thenReturn(CATEGORY_NAMES);
        when(openAIService.generateAnswer(any())).thenReturn(MOCK_RESPONSE);

        // When
        MarketSizeGrowthDTO result = marketResearchService.getMarketSizeAndGrowth(businessDTO);

        // Then
        assertNotNull(result);
        assertEquals(EXPECTED_MARKET_SIZE, result.getMarketSize());
        assertEquals(EXPECTED_GROWTH_RATE, result.getGrowthRate());

        verify(categoryService).getCategoryNameByBusinessId(BUSINESS_ID_1);
        verify(openAIService).generateAnswer(any());
    }

    @DisplayName("시장 규모 정보 없을 때 성장률만 조회 성공")
    @Test
    void getMarketSizeAndGrowth_NoMarketSizeInfo() throws Exception {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_2);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_2)).thenReturn(NEW_TECH_CATEGORY);
        when(openAIService.generateAnswer(any())).thenReturn(MOCK_RESPONSE_NO_MARKET_SIZE);

        // When
        MarketSizeGrowthDTO result = marketResearchService.getMarketSizeAndGrowth(businessDTO);

        // Then
        assertEquals(NO_INFO, result.getMarketSize());
        assertEquals(EXPECTED_GROWTH_RATE_NO_MARKET_SIZE, result.getGrowthRate());
    }

    @DisplayName("데이터베이스 오류 시 예외 발생 확인")
    @Test
    void getMarketSizeAndGrowth_Exception() {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_3);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_3)).thenThrow(new RuntimeException(DATABASE_ERROR));

        // When & Then
        assertThrows(RuntimeException.class, () -> marketResearchService.getMarketSizeAndGrowth(businessDTO));
    }

    private BusinessDTO createBusinessDTO(int id) {
        BusinessDTO businessDTO = new BusinessDTO();
        businessDTO.setId(id);
        businessDTO.setBusinessScale(BUSINESS_SCALE);
        businessDTO.setNation(NATION);
        businessDTO.setCustomerType(CUSTOMER_TYPE);
        businessDTO.setBusinessPlatform(BUSINESS_PLATFORM);
        businessDTO.setBusinessContent(BUSINESS_CONTENT);
        return businessDTO;
    }

    @DisplayName("유사 서비스 분석 조회 성공")
    @Test
    void analyzeSimilarServices_Success() throws OpenAIResponseFailException {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_1);
        List<ResponsePythonServerVO> mockResponse = Arrays.asList(
                new ResponsePythonServerVO(SIMILAR_SERVICE_1, new BusinessInfoDTO(), SIMILARITY_SCORE_1),
                new ResponsePythonServerVO(SIMILAR_SERVICE_2, new BusinessInfoDTO(), SIMILARITY_SCORE_2)
        );

        // categoryService.getCategoryNameByBusinessId가 두 번 호출될 것을 예상
        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_1)).thenReturn(CATEGORY_NAMES);
        when(restTemplate.exchange(
                eq(PYTHON_SERVER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));
        when(openAIService.generateAnswer(any())).thenReturn(SIMILAR_SERVICES_ANALYSIS);

        // When
        SimilarServicesAnalysisDTO result = marketResearchService.analyzeSimilarServices(businessDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getSimilarServices().size());
        assertEquals(SIMILAR_SERVICES_ANALYSIS, result.getAnalysis());

        List<String> expectedSimilarServices = Arrays.asList(
                String.format("%s (유사도: %.2f)", SIMILAR_SERVICE_1, SIMILARITY_SCORE_1),
                String.format("%s (유사도: %.2f)", SIMILAR_SERVICE_2, SIMILARITY_SCORE_2)
        );
        assertEquals(expectedSimilarServices, result.getSimilarServices());

        // categoryService.getCategoryNameByBusinessId가 정확히 두 번 호출되었는지 확인
        verify(categoryService, times(2)).getCategoryNameByBusinessId(BUSINESS_ID_1);
        verify(restTemplate).exchange(
                eq(PYTHON_SERVER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
        verify(openAIService).generateAnswer(any());
    }

    @DisplayName("Python 서버에서 유사 서비스 조회 실패")
    @Test
    void getSimilarServicesFromPythonServer_Failure() {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_2);

        when(restTemplate.exchange(
                eq(PYTHON_SERVER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        // When & Then
        assertThrows(RuntimeException.class, () -> marketResearchService.analyzeSimilarServices(businessDTO));

        verify(restTemplate).exchange(
                eq(PYTHON_SERVER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @DisplayName("날짜 포맷팅 테스트")
    @Test
    void formatDate_Success() {
        // Given
        Date date = new Date(120, 0, 1); // 2020-01-01

        // When
        String formattedDate = marketResearchService.formatDate(date);

        // Then
        assertEquals("2020-01-01", formattedDate);
    }

    @DisplayName("날짜 포맷팅 실패 (null 입력)")
    @Test
    void formatDate_NullInput() {
        // When
        String formattedDate = marketResearchService.formatDate(null);

        // Then
        assertNull(formattedDate);
    }

    @DisplayName("트렌드, 주요 고객, 기술 동향 조회 성공")
    @Test
    void getTrendCustomerTechnology_Success() throws OpenAIResponseFailException {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_1);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_1)).thenReturn(CATEGORY_NAMES);
        when(openAIService.generateAnswer(any())).thenReturn(MOCK_TREND_RESPONSE);

        // When
        TrendCustomerTechnologyDTO result = marketResearchService.getTrendCustomerTechnology(businessDTO);

        // Then
        assertNotNull(result);
        assertEquals("AI 기술의 발전으로 데이터 분석 시장이 급성장하고 있습니다.", result.getTrend());
        assertEquals("20-40대 젊은 전문직 종사자들이 주요 고객층입니다.", result.getMainCustomers());
        assertEquals("머신러닝과 딥러닝 기술이 지속적으로 발전하고 있습니다.", result.getTechnologyTrend());

        verify(categoryService).getCategoryNameByBusinessId(BUSINESS_ID_1);
        verify(openAIService).generateAnswer(any());
    }

    @DisplayName("트렌드, 주요 고객, 기술 동향 조회 - 빈 응답")
    @Test
    void getTrendCustomerTechnology_EmptyResponse() throws OpenAIResponseFailException {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_2);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_2)).thenReturn(CATEGORY_NAMES);
        when(openAIService.generateAnswer(any())).thenReturn("");

        // When
        TrendCustomerTechnologyDTO result = marketResearchService.getTrendCustomerTechnology(businessDTO);

        // Then
        assertNotNull(result);
        assertEquals(NO_INFO, result.getTrend());
        assertEquals(NO_INFO, result.getMainCustomers());
        assertEquals(NO_INFO, result.getTechnologyTrend());

        verify(categoryService).getCategoryNameByBusinessId(BUSINESS_ID_2);
        verify(openAIService).generateAnswer(any());
    }

    @DisplayName("트렌드, 주요 고객, 기술 동향 조회 - 예외 발생")
    @Test
    void getTrendCustomerTechnology_Exception() {
        // Given
        BusinessDTO businessDTO = createBusinessDTO(BUSINESS_ID_3);

        when(categoryService.getCategoryNameByBusinessId(BUSINESS_ID_3)).thenThrow(new RuntimeException(DATABASE_ERROR));

        // When & Then
        assertThrows(RuntimeException.class, () -> marketResearchService.getTrendCustomerTechnology(businessDTO));

        verify(categoryService).getCategoryNameByBusinessId(BUSINESS_ID_3);
    }

    @DisplayName("시장 조사 이력 저장 성공")
    @Test
    void saveMarketResearchHistory_Success() {
        // Given
        MarketResearchHistoryDTO historyDTO = createMarketResearchHistoryDTO(BUSINESS_ID_1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> marketResearchService.saveMarketResearchHistory(historyDTO));

        // Verify
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), any(Object[].class));
        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @DisplayName("시장 조사 이력 저장 실패 - 존재하지 않는 비즈니스 ID")
    @Test
    void saveMarketResearchHistory_NonExistentBusinessId() {
        // Given
        MarketResearchHistoryDTO historyDTO = createMarketResearchHistoryDTO(BUSINESS_ID_1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> marketResearchService.saveMarketResearchHistory(historyDTO));
    }

    @DisplayName("시장 조사 이력 저장 실패 - 데이터베이스 오류")
    @Test
    void saveMarketResearchHistory_DatabaseError() {
        // Given
        MarketResearchHistoryDTO historyDTO = createMarketResearchHistoryDTO(BUSINESS_ID_1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> marketResearchService.saveMarketResearchHistory(historyDTO));
    }

    private MarketResearchHistoryDTO createMarketResearchHistoryDTO(int businessId) {
        MarketResearchHistoryDTO dto = new MarketResearchHistoryDTO();
        dto.setBusinessId(businessId);
        dto.setMarketInformation(MARKET_INFORMATION);
        dto.setCompetitorAnalysis(COMPETITOR_ANALYSIS);
        dto.setMarketTrends(MARKET_TRENDS);
        dto.setRegulationInformation(REGULATION_INFORMATION);
        dto.setMarketEntryStrategy(MARKET_ENTRY_STRATEGY);
        return dto;
    }
}