package org.jiwoo.back.taxation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.business.service.BusinessService;
import org.jiwoo.back.common.OpenAI.service.OpenAIService;
import org.jiwoo.back.common.exception.OpenAIResponseFailException;
import org.jiwoo.back.taxation.aggregate.entity.Taxation;
import org.jiwoo.back.taxation.dto.FileDTO;
import org.jiwoo.back.taxation.dto.SimpleTransactionDTO;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.taxation.dto.TaxationResponseDTO;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static java.time.LocalTime.now;

@Service
@Slf4j
public class TaxationServiceImpl implements TaxationService {

    @Value("${python.server.url.taxation}")
    private String pythonServerUrl;

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

    @Autowired
    private AsyncDataSenderService asyncDataSenderService;

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

    // 비동기로 데이터를 전송하고 결과를 수신
    public CompletableFuture<String> sendToPythonServerAsync(String jsonInputString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(pythonServerUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder responseBuilder = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            responseBuilder.append(responseLine.trim());
                        }
                        return responseBuilder.toString();
                    }
                } else {
                    log.error("파이썬 서버 접속 실패: " + responseCode);
                    throw new IOException("파이썬 서버 응답 오류: " + responseCode);
                }
            } catch (Exception e) {
                throw new RuntimeException("파이썬 서버 전송 오류", e);
            }
        });
    }

    // 세무처리
    @Transactional(readOnly = true)
    @Override
    public TaxationResponseDTO getTaxation(List<MultipartFile> transactionFiles,
                                           MultipartFile incomeTaxProof,
                                           int businessId,
                                           String bank) throws Exception {

        TaxationDTO taxationDTO = dataToDTO(transactionFiles, incomeTaxProof, businessId, bank);

        // Python 서버로 데이터 전송
        String pythonResponse = sendToPythonServer(taxationDTO);

        // 응답 처리 및 파싱
        TaxationResponseDTO taxationResponseDTO = parseResponse(pythonResponse);


// gpt 직접 호출 (java)
//        String gptResponse = getGPTResponse(taxationDTO);
//        log.info("\n**** GPT Response : " + gptResponse);

//        return gptResponse;
        return taxationResponseDTO;
    }


    // Python 서버
    public String sendToPythonServer(TaxationDTO taxationDTO) throws IOException {

        // Python 서버 URL 설정
        URL url = new URL(pythonServerUrl);
        HttpURLConnection conn = null;
        int maxRetries = 3;
        int attempts = 0;
        String response = null;

        while (attempts < maxRetries) {

            try {
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setConnectTimeout(5000);   // 연결 타임아웃
                conn.setReadTimeout(5000);      // 읽기 타임아웃
                conn.setDoOutput(true);

                // DTO -> JSON
                String jsonInputString = convertDTOToJSON(taxationDTO);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 응답 받기
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder responseBuilder = new StringBuilder();
                        String responseLine;

                        while ((responseLine = br.readLine()) != null) {
                            responseBuilder.append(responseLine.trim());
                        }

                        response = responseBuilder.toString();
                    }

                    break; // 요청 성공
                } else {
                    log.error("\n😢 파이썬 서버 접속 실패 : " + responseCode);
                }
            } catch (IOException e) {
                attempts++;
                log.warn("시도 : " + attempts + " 번 실패. 재시도중...", e);

                if (attempts >= maxRetries) {
                    throw new IOException("\n😢파이썬 서버에 요청 보내기 실패 : " + maxRetries + " 번쨰 시도.", e);
                }

                try {
                    //재시도 전 대기
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new IOException("\n😢Thread was interrupted during retry wait time", ex);
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        if (response == null) {
            throw new IOException("\n😢파이썬으로부터 올바른 응답 받기 실패.");
        }

        return response;
    }



    //Input Data -> DTO
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

        // 은행 정보
        taxationDTO.setBank(bank);

        // 현재 날짜
        taxationDTO.setCurrentDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        log.info("\n***** 현재 날짜 저장했어");

        // 비동기 작업 : 사업자등록번호로 사업자 유형 조회
        CompletableFuture<String> businessTypeFuture = CompletableFuture.supplyAsync(() ->{
            try{

                return findBusinessType(businessDTO.getBusinessNumber());
            }catch(Exception e){
                log.error("사업자 유형 정보 조회 실패 : ", e);
                return "부가가치세 일반과세자";
            }
        }).exceptionally(ex -> {
            log.error("사업자 유형 조회 중 예외 발생 : ", ex);
            return "부가가치세 일반과세자";
        });

        // 비동기 작업 : 종합소득세 정보
        CompletableFuture<String> incomeRatesFuture = CompletableFuture.supplyAsync(()->{
            try{
                incomeTaxService.updateIncomeTaxRates();
                return incomeTaxService.getFormattedTaxRates();
            }catch (Exception e){
                log.error("종합소득세 정보 조회 실패 : ", e);
                return null;
            }
        }).exceptionally(ex -> {
            log.error("종합소득세 정보 조회 중 예외 발생 : " , ex);
            return null;
        });

        // 비동기 작업 : 부가가치세 정보
        CompletableFuture<String> vatInfoFuture = CompletableFuture.supplyAsync(() -> {
            try{
                vatService.updateVATRates();
                return vatService.getFormattedTaxRates();
            }catch (Exception e){
                log.error("부가가치세 정보 조회 실패 : ", e);
                return null;
            }
        }).exceptionally(ex -> {
            log.error("부가가치세 정보 조회 중 예외 발생 : ", ex);
            return null;
        });

        // 비동기 작업 완료 후 taxationDTO 에 저장
        CompletableFuture.allOf(businessTypeFuture, incomeRatesFuture, vatInfoFuture).join();

        taxationDTO.setBusinessType(businessTypeFuture.get());
        taxationDTO.setIncomeRates(incomeRatesFuture.get());
        taxationDTO.setVatInfo(vatInfoFuture.get());

        log.info("\n*****taxationDTO : " + taxationDTO);
        return taxationDTO;
    }

    // Input DTO -> JSON
    private String convertDTOToJSON(TaxationDTO taxationDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // DTO객체를 JSON 문자열로 변환
        String json = objectMapper.writeValueAsString(taxationDTO);

        return json;
    }

    // Output JSON -> DTO
    public TaxationResponseDTO parseResponse(String pythonResponse) throws ParseException {

        ObjectMapper objectMapper = new ObjectMapper();
        TaxationResponseDTO responseDTO = null;

        try {
            responseDTO = objectMapper.readValue(pythonResponse, TaxationResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("\n😢파이썬 서버로부터 JSON 응답을 받는 중 오류 발생 : ", e);
            throw new RuntimeException("\n😢파이썬 서버로부터 응답 받기 실패 : ", e);
        }


        return responseDTO;
    }




    // 거래내역 파일 텍스트화
    private FileDTO transactionFileToText(List<MultipartFile> transactionFiles) throws Exception {
        List<String> transactionList = fileService.preprocessTransactionFiles(transactionFiles);

        FileDTO transactionListDTO = new FileDTO();
        for (int i = 0; i < transactionList.size(); i++) {
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
                        "6. ** 총 소득 공제 **: %s \n" +
                        "7. ** 총 세액 공제 **: %s \n" +
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
                        "   - 총 소득 공제 : ** (만원)\n" +
                        "   - 총 세액 공제 : ** (만원)\n" +
                        "   - 적자 유무: 흑자 또는 적자\n" +
                        "   - 세금 절세를 위한 방법 : **\n" +
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
                /*소득공제 */ "605,800 원",
                /*세액공제 */ "9,774,345 원"
        );

    }

    // 파싱 메소드
    private String extractValue(String gptResponse, String key) {
        Pattern pattern = Pattern.compile(key + "\\s*(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(gptResponse);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "0";
    }


}
