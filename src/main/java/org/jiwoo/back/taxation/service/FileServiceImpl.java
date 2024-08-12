package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FileServiceImpl implements FileService {


    //거래내역 파일 전처리
    @Override
    public List<String> preprocessTransactionFiles(List<MultipartFile> transactionFiles) throws Exception {

        log.info("거래내역 파일 처리 시작");

        List<String> processedFiles = new ArrayList<>();

        log.info("fileSize : " + transactionFiles.size());

        for(int i = 0; i < transactionFiles.size(); i++){

            MultipartFile file = transactionFiles.get(i);

            String fileName = file.getName();
            log.info("거래내역 fileName : " + file.getName());

            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("파일 이름이 존재하지 않습니다.");
            }
            log.info("fileName : " + fileName);

            //확장자 가져오기
            String fileExtension = getFileExtension(fileName);

            //확장자에 따라 다르게 처리
            try (InputStream inputStream = file.getInputStream()) {
                processedFiles.add(processFileByExtension(fileExtension, inputStream));
                log.info("파일 하나 확장자 처리했어");
            }
            log.info("=========파일 텍스트화 완료========" + transactionFiles.get(i).getName());
            log.info("총 파일 수 : " + transactionFiles.size());
        }
        return processedFiles;
    }

    @Override
    // 세액/소득공제 파일 전처리
    public String preprocessIncomeTaxProofFiles(MultipartFile incomeTaxProofFile) throws Exception {

        log.info("소득/세액공제 파일 처리 시작");

        String fileName = incomeTaxProofFile.getName();
        log.info("소득/세액공제 fileName : " + fileName);

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 존재하지 않습니다.");
        }

        //확장자 가져오기
        String fileExtension = getFileExtension(fileName);

        //확장자에 따라 다르게 처리
        try (InputStream inputStream = incomeTaxProofFile.getInputStream()) {
            return processFileByExtension(fileExtension, inputStream);
        }
    }

    //파일 확장자 추출
    private String getFileExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        log.info("getFileExtension 파일 dot index : " + dotIndex);
        log.info("getFIleExtension 파일 이름 : " + fileName);

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            log.info("*****파일 dotIndex : " + dotIndex);
            log.info("*****파일 dotIndex == fileName.length()-1 : " + (dotIndex == fileName.length()-1));
            throw new IllegalArgumentException("파일의 확장자가 올바르지 않습니다. " + fileName);
        }

        log.info("==================================파일 하나 확장자 변경===================================");
        return fileName.substring(dotIndex + 1);
    }

    // 확장자별 처리
    private String processFileByExtension(String fileExtension, InputStream inputStream) throws IOException {
        switch (fileExtension.toLowerCase()) {
            case "xlsx":
                log.info("xlsx 파일 텍스트화 완료");
                return preprocessXlsxInputStream(inputStream);
            case "xls":
                log.info("xls 파일 텍스트화 완료");
                return preprocessXlsInputStream(inputStream);
            case "pdf":
                log.info("pdf 파일 텍스트화 완료");
                return preprocessPDFInputStream(inputStream);
            case "csv":
                log.info("csv 파일 텍스트화 완료");
                return preprocessCsvInputStream(inputStream);
            case "zip":
                log.info("zip 파일 텍스트화 완료");
                return preprocessZipFile(inputStream);
            default:
                throw new IllegalArgumentException("지원하지 않는 파일 타입입니다. : " + fileExtension);
        }
    }


    //zip파일 처리
    private String preprocessZipFile(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    String fileName = zipEntry.getName();
                    String fileExtension = getFileExtension(fileName);

                    //ZipInputStream을 다시 열기 위해 한 번 읽고 다시 읽을 수 있도록 함
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(zis, baos);
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    sb.append(processFileByExtension(fileExtension, bais)).append("\n");
                }
                zis.closeEntry();
            }
        }
        return sb.toString();
    }


    //Xlsx 파일 처리
    private String preprocessXlsxInputStream(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        return preprocessWorkbook(workbook);
    }

    //Xls 파일 처리
    private String preprocessXlsInputStream(InputStream inputStream) throws IOException {
        Workbook workbook = new HSSFWorkbook(inputStream);
        return preprocessWorkbook(workbook);
    }

    //Xlsx, xls 공통 로직
    private String preprocessWorkbook(Workbook workbook) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.toString()).append(" ");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }


    // pdf 파일 처리
    private String preprocessPDFInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (PDDocument document = PDDocument.load(inputStream)) {
            // PDF 파일 로드
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            // PDF 파일 텍스트 추출
            sb.append(pdfTextStripper.getText(document));
            // 추출한 텍스트를 StringBuilder에 추가
        }

        return sb.toString();
    }

    //csv 파일 처리
    private String preprocessCsvInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

}
