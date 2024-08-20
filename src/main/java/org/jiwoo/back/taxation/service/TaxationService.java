package org.jiwoo.back.taxation.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.taxation.dto.TaxationResponseDTO;
import org.jiwoo.back.user.dto.AuthDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaxationService {

    // 사업정보로 회원정보 가져오기
    AuthDTO findByBusinessCode(BusinessDTO businessDTO);

    // 세무처리
    TaxationResponseDTO getTaxation(List<MultipartFile> transactionFiles,
                                    MultipartFile incomeTaxProof,
                                    String question1,
                                    String question2,
                                    String question3,
                                    String question4,
                                    String question5,
                                    int businessId,
                                    String bank) throws Exception;

    // gpt 요청 및 응답
    String getGPTResponse(TaxationDTO taxationDTO) throws OpenAIResponseFailException;


}
