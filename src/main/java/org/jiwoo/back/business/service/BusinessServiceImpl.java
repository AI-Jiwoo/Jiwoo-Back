package org.jiwoo.back.business.service;

import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    @Autowired
    public BusinessServiceImpl(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Override
    public BusinessDTO findBusinessById(int id) {
        return businessRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private BusinessDTO convertToDTO(Business business) {
        BusinessDTO dto = new BusinessDTO();
        dto.setId(business.getId());
        dto.setBusinessName(business.getBusinessName());
        dto.setBusinessNumber(business.getBusinessNumber());
        dto.setBusinessScale(business.getBusinessScale());
        dto.setBusinessBudget(business.getBusinessBudget());
        dto.setBusinessContent(business.getBusinessContent());
        dto.setBusinessPlatform(business.getBusinessPlatform());
        dto.setBusinessLocation(business.getBusinessLocation());
        dto.setBusinessStartDate(business.getBusinessStartDate());
        dto.setNation(business.getNation());
        dto.setInvestmentStatus(business.getInvestmentStatus());
        dto.setCustomerType(business.getCustomerType());
        dto.setUserId(business.getUserId());
        dto.setStartupStageId(business.getStartupStageId());
        return dto;
    }
}