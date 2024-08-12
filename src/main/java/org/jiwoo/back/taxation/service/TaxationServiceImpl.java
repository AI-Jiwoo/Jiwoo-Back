package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.business.service.BusinessService;
import org.jiwoo.back.common.OpenAI.service.OpenAIService;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.taxation.aggregate.entity.Taxation;
import org.jiwoo.back.taxation.dto.FileDTO;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalTime.now;

@Service
@Slf4j
public class TaxationServiceImpl implements TaxationService{

    private UserRepository userRepository;
    private BusinessRepository businessRepository;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private FileService fileService;

    @Autowired
    private HomeTaxAPIService homeTaxAPIService;

    @Autowired
    private IncomeTaxService incomeTaxService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private VATService vatService;

    public TaxationServiceImpl(UserRepository userRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

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


    // 세무처리
    @Transactional(readOnly = true)
    @Override
    public String getTaxation(List<MultipartFile> transactionFiles,
                              MultipartFile incomeTaxProof,
                              int businessId,
                              String bank) throws Exception {

        TaxationDTO taxationDTO = dataToDTO(transactionFiles, incomeTaxProof, businessId, bank);

        String gptResponse = getGPTResponse(taxationDTO);
        log.info("\n**** GPT Response : " + gptResponse);

        return gptResponse;
    }

    //Data -> DTO
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public TaxationDTO dataToDTO(List<MultipartFile> transactionFiles,
                                 MultipartFile incomeTaxProof,
                                 int businessId,
                                 String bank) throws Exception {

        TaxationDTO taxationDTO = new TaxationDTO();

        //거래내역 파일 텍스트화
        FileDTO transactionListDTO = transactionFileToText(transactionFiles);
        taxationDTO.setTransactionList(transactionListDTO);

        //소득/세액공제 파일 텍스트화
        FileDTO incomeTaxProofDTO = incomeTaxProofToText(incomeTaxProof);
        taxationDTO.setIncomeTaxProof(incomeTaxProofDTO);

        //사업번호로 사업정보 조회
        BusinessDTO businessDTO = businessService.findBusinessById(businessId);
        taxationDTO.setBusinessId(String.valueOf(businessId));
        taxationDTO.setBusinessCode(businessDTO.getBusinessNumber());
        taxationDTO.setBusinessContent(businessDTO.getBusinessContent());

        //사업자등록번호로 사업자 유형 조회
        String businessType = findBusinessType(businessDTO.getBusinessNumber());
        log.info("\n***** 사업자 유형 조회했어");
        taxationDTO.setBusinessType(businessType);

        // 은행 정보
        taxationDTO.setBank(bank);

        // 현재 날짜
        taxationDTO.setCurrentDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        log.info("\n***** 현재 날짜 저장했어");

        // 부가가치세 정보
        vatService.updateVATRates();
        String vatInfo = vatService.getFormattedTaxRates();
        log.info("\n*****부가가치세 정보 : {}", vatInfo);
        taxationDTO.setCurrentDate(vatInfo);

        // 종합소득세 정보
        incomeTaxService.updateIncomeTaxRates();
        String incomeRates = incomeTaxService.getFormattedTaxRates();
        log.info("\n*****종합소득세 정보 : {}", incomeRates);
        taxationDTO.setIncomeRates(incomeRates);

        log.info("\n*****taxationDTO : " + taxationDTO);
        return taxationDTO;
    }

    // 거래내역 파일 텍스트화
    private FileDTO transactionFileToText(List<MultipartFile> transactionFiles) throws Exception {
        List<String> transactionList = fileService.preprocessTransactionFiles(transactionFiles);

        FileDTO transactionListDTO = new FileDTO();
        for(int i = 0; i<transactionList.size(); i++){
            String transactionFileName = transactionFiles.get(i).getOriginalFilename();
            String transactionFileContent = transactionList.get(i);

            transactionListDTO.setFileName(transactionFileName);
            transactionListDTO.setContent(transactionFileContent);
        }

        return transactionListDTO;
    }

    //소득/세액공제 파일 텍스트화
    private FileDTO incomeTaxProofToText(MultipartFile incomeTaxProof) throws Exception {
        FileDTO incomeTaxProofDTO = new FileDTO();

        String incomeTaxProofContent = fileService.preprocessIncomeTaxProofFiles(incomeTaxProof);
        String incomeTaxProofFileName = incomeTaxProof.getOriginalFilename();
        incomeTaxProofDTO.setFileName(incomeTaxProofFileName);
        incomeTaxProofDTO.setContent(incomeTaxProofContent);

        return incomeTaxProofDTO;
    }

    //사업자등록번호로 사업자 유형 조회
    private String findBusinessType(String businessCode) {
        return homeTaxAPIService.getBusinessType(businessCode);
    }

    //gpt 요청 및 응답
    @Override
    public String getGPTResponse(TaxationDTO taxationDTO) throws OpenAIResponseFailException {

        String prompt = getGPTPrompt(taxationDTO);
        log.info("\n***** AI 프롬프트 : " + prompt);

        String response = openAIService.generateAnswer(prompt);
//        log.info("\n***** gpt 응답 : \n" + response);

        return response;
    }

    // gpt 프롬프트
    public String getGPTPrompt(TaxationDTO taxationDTO) throws OpenAIResponseFailException {

        return String.format("당신은 세무 전문가입니다. 다음 정보를 바탕으로 거래내역을 간편장부 형식으로 변환하고, 세무 분석 결과를 제공합니다.\n" +
                        "\n" +
                        "1. **거래내역** (텍스트 형식): %s\n" +
                        "2. **현재 날짜**: %s\n" +
                        "3. **사업자 정보**:\n" +
                        "   - 은행/카드사 정보: %s \n" +
                        "   - 사업자 유형: %s \n" +
                        "   - 사업내용: %s \n" +
                        "4. **부가가치세 정보**: %s \n" +
                        "5. **종합소득세 정보**: %s \n" +
                        "6. **소득/세액공제 증명서류 (텍스트)**: %s \n" +
                        "\n" +
                        "**요청사항:**\n" +
                        "\n" +
                        "1. 주어진 정보를 바탕으로 거래내역을 다음 간편장부 형식으로 변환:\n" +
                        "| 일자       | 계정과목 | 거래내용    | 거래처      | 수입 (금액) | 수입 (부가세) | 비용 (금액) | 비용 (부가세) | 자산 증감 (금액) | 자산 증감 (부가세) | 비고          |\n" +
                        "|------------|----------|-------------|-------------|-------------|---------------|-------------|---------------|-------------------|-------------------|---------------|\n" +
                        "| 예시       | 체크     | 예금이자    | 농협        | 0           | 0             | 4.0         | 0             | 4.0               | 0                 |               |\n" +
                        "\n" +
                        "2. 소득/세액공제 증명서류를 분석하여 다음 항목 계산: (**안에 계산한 값 넣기)\n" +
                        "   - 예상 종합소득세: ** (만원) \n" +
                        "   - 총 매출액: ** (만원)\n" +
                        "   - 총 소득: ** (만원)\n" +
                        "   - 순 매출액: ** (만원)\n" +
                        "   - 적자 유무: 흑자 또는 적자\n" +
                        "   - 세금 절세를 위한 추가 비용: ** (만원)\n" +
                        "\n" +
                        "3. 거래내역의 시작 날짜와 마지막 날짜:\n" +
                        "   - 시작 날짜: YYYY-MM-DD\n" +
                        "   - 마지막 날짜: YYYY-MM-DD\n" +
                        "\n" +
                        "**추가 참고 사항**:\n" +
                        "- 간편장부 형식의 각 열은 \"일자\", \"계정과목\", \"거래내용\", \"거래처\", \"수입 (금액)\", \"수입 (부가세)\", \"비용 (금액)\", \"비용 (부가세)\", \"자산 증감 (금액)\", \"자산 증감 (부가세)\", \"비고\" 입니다.\n" +
                        "- 모든 계산 결과는 명확하게 숫자로 표시.\n" +
                        "- 매출과 소득이 없는 경우, 각 항목을 0원으로 계산.\n" +
                        "- 적자 유무는 \"흑자\" 또는 \"적자\"로만 표시.\n" +
                        "- 간편장부 형식 모든 내용을 다 출력.\n" +
                        "- 부가가치세 정보를 참고해서 간편장부 안에 부가세를 입력.\n" +
                        "- 종합소득세 정보를 참고해서 예상 종합소득세, 세금 절세를 위한 추가비용을 계산.\n" +
                        "- 종합소득세 계산 로직도 종합소득세 결과 아래에 작성.\n" +
                        "- 계산 항목(**)에 계산 결과를 만원 단위로 출력.\n" +
                        "- 문장형으로 대답하지 않고 필요한 정보만 제공.\n",
                /*거래내역*/ taxationDTO.getTransactionList().getContent(),
                /*현재 날짜*/ taxationDTO.getCurrentDate(),
                /*은행 정보*/ taxationDTO.getBank(),
                /*사업자 유형*/ taxationDTO.getBusinessType(),
                /*사업 내용*/ taxationDTO.getBusinessContent(),
                /*부가가치세 정보*/ taxationDTO.getVatInfo(),
                /*종합소득세 정보*/ taxationDTO.getIncomeRates(),
                /*소득/세액공제 내용*/ taxationDTO.getIncomeTaxProof().getContent()
                );

    }


    // gpt 응답 데이터 파싱
    public void parseResponse(String gptResponse, String businessId) throws ParseException {

        String incomeTax = extractValue(gptResponse, "예상 종합소득세:");
        String totalSales =  extractValue(gptResponse, "총 매출액:");
        String grossIncome = extractValue(gptResponse, "총 소득:");
        String netSales = extractValue(gptResponse, "순 매출액");
        String startDateStr = extractValue(gptResponse, "거래내역의 시작 날짜:");
        String lastDateStr = extractValue(gptResponse, "거래내역의 마지막 날짜:");
        String lossStatus = extractValue(gptResponse, "적자 유무:");
        String additionalTaxSavings = extractValue(gptResponse, "세금 절세를 위한 추가 비용:");


        // String을 Date로 변환
        Date startDate = new Date(dateFormat.parse(startDateStr).getTime());
        Date lastDate = new Date(dateFormat.parse(lastDateStr).getTime());


        /*Taxation taxtion = Taxation.builder()
                .incomeTax(new BigDecimal(incomeTax))
                .totalSales(new BigDecimal(totalSales))
                .grossIncome(new BigDecimal(grossIncome))
                .netSales(new BigDecimal(netSales))
                .startDate(startDate)
                .lastDate(lastDate)
                .lossStatus(lossStatus)
                .businessId(businessId)
                .build();*/
    }

    // 파싱 메소드
    private String extractValue(String gptResponse, String key){
        Pattern pattern = Pattern.compile(key + "\\s*(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(gptResponse);

        if(matcher.find()){
            return matcher.group(1);
        }

        return "0";
    }



}
