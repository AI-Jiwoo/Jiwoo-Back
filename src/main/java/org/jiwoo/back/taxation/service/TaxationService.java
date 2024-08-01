package org.jiwoo.back.taxation.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.user.dto.AuthDTO;
import org.springframework.web.multipart.MultipartFile;

public interface TaxationService {

    // 사업정보로 회원정보 가져오기
    AuthDTO findByBusinessCode(BusinessDTO businessDTO);

}
