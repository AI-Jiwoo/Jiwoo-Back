package org.jiwoo.back.taxaxtion.service;

import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.taxation.service.FileServiceImpl;
import org.jiwoo.back.taxation.service.TaxationServiceImpl;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.dto.AuthDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class TaxServiceImplTest {

    private static final int BUSINESS_ID = 11;
    private static final String BUSINESS_NAME = "신규 테크";
    private static final String BUSINESS_NUMBER = "123-45-67890";
    private static final String BUSINESS_SCALE = "1인 창업";
    private static final double BUSINESS_BUDGET = 10000000.0;
    private static final String BUSINESS_CONTENT = "사업내용";
    private static final String BUSINESS_PLATFORM = "온라인";
    private static final String BUSINESS_START_DATE = "2024-07-30";
    private static final String NATION = "대한민국";
    private static final String INVESTMENT_STATUS = "초기 투자 유치";
    private static final String CUSTOMER_TYPE = "B2C";

    private static final int USER_ID = 4;
    private static final String USER_NAME = "김유저";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String USER_PROVIDER = "provider";
    private static final String USER_SNS_ID = "snsId";
//    private static final Date USER_BIRTH_DATE = new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01");
    private static final String USER_GENDER = "M";
    private static final String USER_PHONE_NO = "010-1234-5678";

    private LocalDate USER_BIRTH_DATE;

    private static final String BANK = "기업은행";


    @InjectMocks
    private TaxationServiceImpl taxationServiceImpl;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private FileServiceImpl fileService;



    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);
        USER_BIRTH_DATE = LocalDate.parse("1990-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @DisplayName("사업 정보로 유저 정보 조회 테스트")
    @Test
    void testFindUserInfo() throws ParseException {
        //given
        //사업정보
        BusinessDTO businessDTO = new BusinessDTO();
        businessDTO.setId(BUSINESS_ID);
        businessDTO.setBusinessName(BUSINESS_NAME);
        businessDTO.setBusinessNumber(BUSINESS_NUMBER);
        businessDTO.setBusinessScale(BUSINESS_SCALE);
        businessDTO.setBusinessBudget(BUSINESS_BUDGET);
        businessDTO.setBusinessContent(BUSINESS_CONTENT);
        businessDTO.setBusinessPlatform(BUSINESS_PLATFORM);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse(BUSINESS_START_DATE);
        businessDTO.setBusinessStartDate(startDate);

        businessDTO.setNation(NATION);
        businessDTO.setInvestmentStatus(INVESTMENT_STATUS);
        businessDTO.setCustomerType(CUSTOMER_TYPE);

        // Mock 동작 설정
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockUser.getName()).thenReturn(USER_NAME);
        when(mockUser.getEmail()).thenReturn(USER_EMAIL);
        when(mockUser.getPassword()).thenReturn(USER_PASSWORD);
        when(mockUser.getProvider()).thenReturn(USER_PROVIDER);
        when(mockUser.getSnsId()).thenReturn(USER_SNS_ID);
        doAnswer(invocation ->  USER_BIRTH_DATE).when(mockUser).getBirthDate();
//        when(mockUser.getBirthDate()).thenReturn(USER_BIRTH_DATE);
        when(mockUser.getGender()).thenReturn(USER_GENDER);
        when(mockUser.getPhoneNo()).thenReturn(USER_PHONE_NO);

        Business mockBusiness = mock(Business.class);
        when(mockBusiness.getUser()).thenReturn(mockUser);

        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.of(mockBusiness));

        //when
        //사업 번호로 유저 정보 조회
        AuthDTO authDTO = taxationServiceImpl.findByBusinessCode(businessDTO);

        //then
        assertNotNull(authDTO);
        Assertions.assertEquals(USER_NAME, authDTO.getName());
        Assertions.assertEquals(USER_EMAIL, authDTO.getEmail());
        Assertions.assertEquals(USER_PASSWORD, authDTO.getPassword());
        Assertions.assertEquals(USER_PROVIDER, authDTO.getProvider());
        Assertions.assertEquals(USER_SNS_ID, authDTO.getSnsId());
        Assertions.assertEquals(USER_BIRTH_DATE, authDTO.getBirthDate());
        Assertions.assertEquals(USER_GENDER, authDTO.getGender());
        Assertions.assertEquals(USER_PHONE_NO, authDTO.getPhoneNo());

        System.out.println(authDTO);
    }


    @Test
    void testTaxService() {
        //given
        //은행이름, 거래내역 파일, 소득/세액공제 증명서류, 사업정보
        //사업정보 -> 회원정보 가져오기


        //when
        //then

    }

    @DisplayName("파일 변환 서비스 테스트")
    @ParameterizedTest
    @MethodSource("fileProvider")
    void testPreprocessFile(MultipartFile file, String expectedContent) throws Exception {
        //given
        //파일 입력
        String result = fileService.preprocessFile(file);

        //when

        //then
        assertNotNull(result);
        assertTrue(result.contains(expectedContent));
        System.out.println(result);
    }




    // 테스트용 파일
    static Stream<Arguments> fileProvider() throws IOException {
        return Stream.of(
                Arguments.of(createXlsxFile(), "test1"),
                Arguments.of(createXlsFile(), "test1"),
                Arguments.of(createPdfFile(), "PDF content placeholder"), // 실제 PDF 내용으로 변경 필요
                Arguments.of(createCsvFile(), "test1"),
                Arguments.of(createZipFile(), "test1")
        );
    }

    private static MultipartFile createXlsxFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("test1");
        row.createCell(1).setCellValue("test2");
        row.createCell(2).setCellValue("test3");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(baos.toByteArray()));
    }

    private static MultipartFile createXlsFile() throws IOException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("test1");
        row.createCell(1).setCellValue("test2");
        row.createCell(2).setCellValue("test3");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", new ByteArrayInputStream(baos.toByteArray()));
    }

    private static MultipartFile createPdfFile() throws IOException {
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return new MockMultipartFile("file", "test.pdf", "application/pdf", new ByteArrayInputStream(out.toByteArray()));
    }

    private static MultipartFile createCsvFile() {
        String content = "test1,test2,test3\nvalue1,value2,value3";
        return new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    private static MultipartFile createZipFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8);
        zos.putNextEntry(new ZipEntry("test.csv"));
        zos.write("test1,test2,test3\nvalue1,value2,value3".getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        zos.close();
        return new MockMultipartFile("file", "test.zip", "application/zip", new ByteArrayInputStream(baos.toByteArray()));
    }

}
