package org.jiwoo.back.marketresearch.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.category.service.CategoryService;
import org.jiwoo.back.common.OpenAI.service.OpenAIService;
import org.jiwoo.back.marketresearch.dto.MarketSizeGrowthDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketResearchServiceImplTest {

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

    @Mock
    private OpenAIService openAIService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private MarketResearchServiceImpl marketResearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
}