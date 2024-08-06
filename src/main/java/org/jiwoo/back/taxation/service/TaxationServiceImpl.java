package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.common.OpenAI.service.OpenAIServiceImpl;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
public class TaxationServiceImpl implements TaxationService{

    private UserRepository userRepository;
    private BusinessRepository businessRepository;

    @Autowired
    private OpenAIServiceImpl openAIService;

    public TaxationServiceImpl(UserRepository userRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    // business code로 회원 정보 조회
    @Override
    public AuthDTO findByBusinessCode(BusinessDTO businessDTO) {
        int businessId = businessDTO.getId();
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NoSuchElementException("사업을 찾을 수 없습니다: " + businessId));

        User user = business.getUser();

        AuthDTO authDTO = new AuthDTO(user.getName(), user.getEmail(), user.getPassword(), user.getProvider(), user.getSnsId(), user.getBirthDate(), user.getGender(), user.getPhoneNo());

        return authDTO;
    }

    // gpt 호출
    @Override
    public String getGPTResponse(TaxationDTO taxationDTO) throws OpenAIResponseFailException {

        String prompt = "너는 금융 거래 내역 데이터를 간편장부 형식으로 변환하는 도우미야. 사용자의 과세 유형(일반과세자 또는 간이과세자)과 사업 유형에 따라 부가가치세(VAT)율을 결정해야 해. 다음 정보가 필요해:\n" +
                "\n" +
                "### 부가가치세 정보\n" +
                "자세한 부가가치세율 정보는 이걸 참조해 : \n" +
                "{\n" +
                taxationDTO.getVatInfo() +
                "}\n" +
                "자세한 종합소득세 정보는 이걸 참조해 : \n" +
                "{\n" +
                taxationDTO.getIncomeRates() +
                "\n}" +
                "현재 날짜는 다음과 같아.\n" +
                taxationDTO.getCurrentDate() +"\n"
                +
                "### 사용자 정보\n" +
                "- 과세 유형: " + taxationDTO.getBusinessType() + "\n" +
                "- 사업 정보: " + taxationDTO.getBusinessContent() + "\n" +
                "- 입력 거래 데이터의 은행/카드사 정보: " + taxationDTO.getBank() + "\n" +
                "\n" +
                "### 입력 거래 데이터\n" +
                taxationDTO.getTransactionList() + "\n" +
                "소득/세액공제 데이터는 다음과 같아. \n" +
                "### 소득/세액 공제 데이터\n" +
                "{\n" +
                taxationDTO.getIncomeTaxProof() + "\n"
                +"\n}" +
                "### 출력 간편장부 형식\n" +
                "| 일자       | 계정과목     | 거래내용        | 거래처      | 수입 (금액)  | 수입 (부가세) | 비용 (금액)  | 비용 (부가세) | 자산 증감 (금액) | 자산 증감 (부가세) | 비고          |\n" +
                "|------------|--------------|-----------------|--------------|--------------|--------------|--------------|--------------|-------------------|-------------------|---------------|\n" +
                "\n" +
                "다음 거래 데이터를 위의 형식으로 변환해줘. 사용자의 과세 유형과 사업 유형에 따라 부가가치세를 계산해줘.\n" +
                "\n" +
                "### 사용자의 과세 유형: " + taxationDTO.getBusinessType() + "\n" +
                "### 사용자의 사업 내용:" + taxationDTO.getBusinessContent() + "\n" +
                "\n" +
                "\n" +
                "### 출력 간편장부 형식\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "------------------------------------------------------------------------------------------------------------------------------------------\n";

        String gptResponse = openAIService.generateAnswer(prompt);

        return gptResponse;
    }





}
