package org.jiwoo.back.business.service;

import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.aggregate.entity.StartupStage;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.business.repository.StartupStageRepository;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.repository.UserRepository;
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
    // 사업 정보 조회
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

    // 사업 정보 저장
    private static final String USER_EMAIL = "test@example.com";
    private static final int USER_ID = 1;
    private static final int STARTUP_STAGE_ID = 1;
    private static final String STARTUP_STAGE_NAME = "초기 단계";

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StartupStageRepository startupStageRepository;

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
        User mockUser = createMockUser();
        StartupStage mockStartupStage = createMockStartupStage();
        setFieldWithReflection(mockBusiness, "user", mockUser);
        setFieldWithReflection(mockBusiness, "startupStage", mockStartupStage);

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

    private User createMockUser() {
        User mockUser = User.builder().email(USER_EMAIL).build();
        setFieldWithReflection(mockUser, "id", USER_ID);
        return mockUser;
    }

    private StartupStage createMockStartupStage() {
        StartupStage mockStartupStage = StartupStage.builder().name(STARTUP_STAGE_NAME).build();
        setFieldWithReflection(mockStartupStage, "id", STARTUP_STAGE_ID);
        return mockStartupStage;
    }

    @DisplayName("사업 정보 저장")
    @Test
    void saveBusiness() throws ParseException {
        // Given
        BusinessDTO businessDTO = createBusinessDTO();
        User mockUser = createMockUser();
        StartupStage mockStartupStage = createMockStartupStage();

        Business mockSavedBusiness = createMockBusiness(BUSINESS_ID, BUSINESS_NAME);
        setFieldWithReflection(mockSavedBusiness, "user", mockUser);
        setFieldWithReflection(mockSavedBusiness, "startupStage", mockStartupStage);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(mockUser);
        when(startupStageRepository.findById(STARTUP_STAGE_ID)).thenReturn(Optional.of(mockStartupStage));
        when(businessRepository.save(any(Business.class))).thenReturn(mockSavedBusiness);

        // When
        BusinessDTO result = businessService.saveBusiness(businessDTO, USER_EMAIL);

        // Then
        assertNotNull(result);
        assertEquals(BUSINESS_ID, result.getId());
        assertEquals(BUSINESS_NAME, result.getBusinessName());
        assertEquals(BUSINESS_NUMBER, result.getBusinessNumber());
        assertEquals(BUSINESS_SCALE, result.getBusinessScale());
        assertEquals(BUSINESS_BUDGET, result.getBusinessBudget());
        assertEquals(BUSINESS_CONTENT, result.getBusinessContent());
        assertEquals(BUSINESS_PLATFORM, result.getBusinessPlatform());
        assertEquals(BUSINESS_LOCATION, result.getBusinessLocation());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(BUSINESS_START_DATE, sdf.format(result.getBusinessStartDate()));
        assertEquals(NATION, result.getNation());
        assertEquals(INVESTMENT_STATUS, result.getInvestmentStatus());
        assertEquals(CUSTOMER_TYPE, result.getCustomerType());
        verify(businessRepository, times(1)).save(any(Business.class));
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(startupStageRepository, times(1)).findById(STARTUP_STAGE_ID);
        verify(businessRepository, times(1)).save(any(Business.class));
    }

    private BusinessDTO createBusinessDTO() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(BUSINESS_START_DATE);

        return new BusinessDTO(
                0, // ID는 0으로 설정 (새로운 비즈니스이므로)
                BUSINESS_NAME,
                BUSINESS_NUMBER,
                BUSINESS_SCALE,
                BUSINESS_BUDGET,
                BUSINESS_CONTENT,
                BUSINESS_PLATFORM,
                BUSINESS_LOCATION,
                startDate,
                NATION,
                INVESTMENT_STATUS,
                CUSTOMER_TYPE,
                USER_ID,
                STARTUP_STAGE_ID
        );
    }

    private void setFieldWithReflection(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value using reflection", e);
        }
    }
}
