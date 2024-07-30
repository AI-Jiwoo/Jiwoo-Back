package org.jiwoo.back.business.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.aggregate.entity.StartupStage;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.business.repository.StartupStageRepository;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final StartupStageRepository startupStageRepository;

    @Autowired
    public BusinessServiceImpl(BusinessRepository businessRepository,
                               UserRepository userRepository,
                               StartupStageRepository startupStageRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.startupStageRepository = startupStageRepository;
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
        Optional<StartupStage> startupStageOptional = startupStageRepository.findById(businessDTO.getStartupStageId());

        if (user == null || startupStageOptional.isEmpty()) {
            throw new IllegalArgumentException("User or StartupStage not found");
        }

        StartupStage startupStage = startupStageOptional.get();

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
                .user(user)  // 이 부분이 중요합니다
                .startupStage(startupStage)
                .build();

        Business savedBusiness = businessRepository.save(business);

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
}