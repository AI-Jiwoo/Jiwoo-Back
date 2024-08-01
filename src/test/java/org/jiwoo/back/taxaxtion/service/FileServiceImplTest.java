package org.jiwoo.back.taxaxtion.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jiwoo.back.taxation.service.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceImplTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
                Arguments.of(createPdfFile(), "This is a sample PDF document."), // 실제 PDF 내용으로 변경 필요
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
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("This is a sample PDF document.");
            contentStream.endText();
        }

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
