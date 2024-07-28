package org.jiwoo.back.business.query.service;

import org.jiwoo.back.business.query.dto.BusinessDTO;

import java.util.List;

public interface BusinessService {
    BusinessDTO findBusinessById(int id);

//    List<BusinessDTO> findBusinessbyUserId();
}
