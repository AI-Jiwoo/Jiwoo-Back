package org.jiwoo.back.business.service;

import org.jiwoo.back.business.dto.BusinessDTO;

public interface BusinessService {
    BusinessDTO findBusinessById(int id);
}