package org.jiwoo.back.accelerating.service;

import org.jiwoo.back.accelerating.aggregate.vo.ResponsePythonServerVO;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BusinessmodelService {
    ResponseEntity<List<ResponsePythonServerVO>> getSimilarServices(BusinessDTO businessDTO);
}
