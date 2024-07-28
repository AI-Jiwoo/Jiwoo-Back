package org.jiwoo.back.business.query.service;

import org.jiwoo.back.business.query.aggregate.entity.Business;
import org.jiwoo.back.business.query.dto.BusinessDTO;
import org.jiwoo.back.business.query.repository.BusinessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private final BusinessMapper businessMapper;

    public BusinessServiceImpl(BusinessMapper businessMapper) {
        this.businessMapper = businessMapper;
    }

    @Override
    public BusinessDTO findBusinessById(int id) {
        Business business = businessMapper.findById(id);

        if (business == null) {
            return null;
        }

        BusinessDTO businessDTO = new BusinessDTO();
        businessDTO.setId(business.getId());
        businessDTO.setBusinessName(business.getBusinessName());
        businessDTO.setBusinessNumber(business.getBusinessNumber());
        businessDTO.setBusinessScale(business.getBusinessScale());
        businessDTO.setBusinessBudget(business.getBusinessBudget());
        businessDTO.setBusinessContent(business.getBusinessContent());
        businessDTO.setBusinessLocation(business.getBusinessLocation());
        businessDTO.setBusinessStartDate(business.getBusinessStartDate());
        businessDTO.setNation(business.getNation());
        businessDTO.setInvestmentStatus(business.getInvestmentStatus());
        businessDTO.setCustomerType(business.getCustomerType());
        businessDTO.setUserId(business.getUserId());
        businessDTO.setStartupStageId(business.getStartupStageId());

        return businessDTO;
    }
}
