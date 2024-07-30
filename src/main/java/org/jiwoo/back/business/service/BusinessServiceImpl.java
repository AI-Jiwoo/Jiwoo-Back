package org.jiwoo.back.business.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.aggregate.entity.StartupStage;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.business.repository.StartupStageRepository;
import org.jiwoo.back.category.service.CategoryService;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final StartupStageRepository startupStageRepository;
    private final CategoryService categoryService;
    private final RestTemplate restTemplate;

    @Value("${python.server.url.insert}")
    private String pythonInsertUrl;

    @Autowired
    public BusinessServiceImpl(BusinessRepository businessRepository,
                               UserRepository userRepository,
                               StartupStageRepository startupStageRepository,
                               CategoryService categoryService,
                               @Qualifier("defaultTemplate") RestTemplate restTemplate) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.startupStageRepository = startupStageRepository;
        this.categoryService = categoryService;
        this.restTemplate = restTemplate;
    }

    @Override
    public BusinessDTO findBusinessById(int id) {
        return businessRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public BusinessDTO saveBusiness(BusinessDTO businessDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        StartupStage startupStage = startupStageRepository.findById(businessDTO.getStartupStageId())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 창업 단계입니다. ID: " + businessDTO.getStartupStageId()));

        Business business = Business.builder()
                .businessName(businessDTO.getBusinessName())
                .businessNumber(businessDTO.getBusinessNumber())
                .businessScale(businessDTO.getBusinessScale())
                .businessBudget(businessDTO.getBusinessBudget())
                .businessContent(businessDTO.getBusinessContent())
                .businessPlatform(businessDTO.getBusinessPlatform())
                .businessLocation(businessDTO.getBusinessLocation())
                .businessStartDate(businessDTO.getBusinessStartDate())
                .nation(businessDTO.getNation())
                .investmentStatus(businessDTO.getInvestmentStatus())
                .customerType(businessDTO.getCustomerType())
                .user(user)
                .startupStage(startupStage)
                .build();

        Business savedBusiness = businessRepository.save(business);

        // Vector DB에 저장
        saveToVectorDb(savedBusiness);

        return convertToDTO(savedBusiness);
    }

    @Override
    public List<BusinessDTO> findAllBusinessesByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        List<Business> businesses = businessRepository.findAllByUser(user);
        return businesses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BusinessDTO convertToDTO(Business business) {
        return new BusinessDTO(
                business.getId(),
                business.getBusinessName(),
                business.getBusinessNumber(),
                business.getBusinessScale(),
                business.getBusinessBudget(),
                business.getBusinessContent(),
                business.getBusinessPlatform(),
                business.getBusinessLocation(),
                business.getBusinessStartDate(),
                business.getNation(),
                business.getInvestmentStatus(),
                business.getCustomerType(),
                business.getUser().getId(),
                business.getStartupStage().getId()
        );
    }

    private void saveToVectorDb(Business business) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> vectorDbRequest = new HashMap<>();
            vectorDbRequest.put("businessName", business.getBusinessName());

            Map<String, Object> info = new HashMap<>();
            info.put("businessPlatform", business.getBusinessPlatform());
            info.put("businessScale", business.getBusinessScale());
            info.put("business_field", categoryService.getCategoryNameByBusinessId(business.getId()));
            info.put("businessStartDate", business.getBusinessStartDate().toString());
            info.put("investmentStatus", business.getInvestmentStatus());
            info.put("customerType", business.getCustomerType());

            vectorDbRequest.put("info", info);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(vectorDbRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    pythonInsertUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully saved business to Vector DB: {}", business.getBusinessName());
            } else {
                log.warn("Unexpected response from Vector DB: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to save business to Vector DB: {}", e.getMessage());
        }
    }
}