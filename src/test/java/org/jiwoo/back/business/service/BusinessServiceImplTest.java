package org.jiwoo.back.business.service;

import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Optional;

class BusinessServiceImplTest {

    private static final int BUSINESS_ID = 1;
    private static final int NON_EXISTENT_BUSINESS_ID = 999;
    private static final String BUSINESS_NAME = "Test Business";
    private static final String BUSINESS_NUMBER = "123-45-67890";
    private static final String BUSINESS_SCALE = "중소기업";
    private static final double BUSINESS_BUDGET = 1000000.0;
    private static final String BUSINESS_CONTENT = "Test content";
    private static final String BUSINESS_PLATFORM = "온라인 플랫폼";
    private static final String BUSINESS_LOCATION = "서울";
    private static final String BUSINESS_START_DATE = "2023-01-01";
    private static final String NATION = "대한민국";
    private static final String INVESTMENT_STATUS = "시드";
    private static final String CUSTOMER_TYPE = "B2C";
    private static final double DELTA = 0.001;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessServiceImpl businessService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("사업 ID로 사업 조회")
    @Test
    void findBusinessById() throws ParseException {
        // Given
        Business mockBusiness = createMockBusiness(BUSINESS_ID, BUSINESS_NAME);

        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.of(mockBusiness));

        // When
        BusinessDTO result = businessService.findBusinessById(BUSINESS_ID);

        // Then
        assertNotNull(result);
        assertEquals(BUSINESS_ID, result.getId());
        assertEquals(BUSINESS_NAME, result.getBusinessName());
        assertEquals(BUSINESS_NUMBER, result.getBusinessNumber());
        assertEquals(BUSINESS_SCALE, result.getBusinessScale());
        assertEquals(BUSINESS_BUDGET, result.getBusinessBudget(), DELTA);
        assertEquals(BUSINESS_CONTENT, result.getBusinessContent());
        assertEquals(BUSINESS_PLATFORM, result.getBusinessPlatform());
        assertEquals(BUSINESS_LOCATION, result.getBusinessLocation());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(BUSINESS_START_DATE, sdf.format(result.getBusinessStartDate()));
        assertEquals(NATION, result.getNation());
        assertEquals(INVESTMENT_STATUS, result.getInvestmentStatus());
        assertEquals(CUSTOMER_TYPE, result.getCustomerType());

        verify(businessRepository, times(1)).findById(BUSINESS_ID);
    }

    @DisplayName("사업 ID 없는 경우 Null 반환")
    @Test
    void findBusinessById_NotFound() {
        // Given
        when(businessRepository.findById(NON_EXISTENT_BUSINESS_ID)).thenReturn(Optional.empty());

        // When
        BusinessDTO result = businessService.findBusinessById(NON_EXISTENT_BUSINESS_ID);

        // Then
        assertNull(result);
        verify(businessRepository, times(1)).findById(NON_EXISTENT_BUSINESS_ID);
    }

    private Business createMockBusiness(int id, String name) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(BUSINESS_START_DATE);

        return Business.builder()
                .id(id)
                .businessName(name)
                .businessNumber(BUSINESS_NUMBER)
                .businessScale(BUSINESS_SCALE)
                .businessBudget(BUSINESS_BUDGET)
                .businessContent(BUSINESS_CONTENT)
                .businessPlatform(BUSINESS_PLATFORM)
                .businessLocation(BUSINESS_LOCATION)
                .businessStartDate(startDate)
                .nation(NATION)
                .investmentStatus(INVESTMENT_STATUS)
                .customerType(CUSTOMER_TYPE)
                .build();
    }
}