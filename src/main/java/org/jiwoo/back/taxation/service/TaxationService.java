package org.jiwoo.back.taxation.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.user.dto.AuthDTO;

public interface TaxationService {

    // 사업정보로 회원정보 가져오기
    AuthDTO findByBusinessCode(BusinessDTO businessDTO);


    // gpt ai에게 정보 물어보기
    String getGPTResponse(TaxationDTO taxationDTO) throws OpenAIResponseFailException;
}
