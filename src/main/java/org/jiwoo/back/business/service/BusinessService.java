package org.jiwoo.back.business.service;

import org.jiwoo.back.business.dto.BusinessDTO;

import java.util.List;

public interface BusinessService {
    BusinessDTO findBusinessById(int id);
    BusinessDTO saveBusiness(BusinessDTO businessDTO, String userEmail);
    List<BusinessDTO> findAllBusinessesByUser(String userEmail);
}