package org.jiwoo.back.taxation.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileServiceImpl implements FileService {


    //파일 전처리
    @Override
    public String preprocessFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("파일 이름이 존재하지 않습니다.");
        }

        //확장자 가져오기
        String fileExtension = getFileExtension(fileName);

        //확장자에 따라 다르게 처리
        try (InputStream inputStream = file.getInputStream()) {
            return processFileByExtension(fileExtension, inputStream);
        }
    }

    //파일 확장자 추출
    private String getFileExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일의 확장자가 올바르지 않습니다. " + fileName);
        }
        return fileName.substring(dotIndex + 1);
    }

    // 확장자별 처리
    private String processFileByExtension(String fileExtension, InputStream inputStream) throws IOException {
        switch (fileExtension.toLowerCase()) {
            case "xlsx":
                return preprocessXlsxInputStream(inputStream);
            case "xls":
                return preprocessXlsInputStream(inputStream);
            case "pdf":
                return preprocessPDFInputStream(inputStream);
            case "csv":
                return preprocessCsvInputStream(inputStream);
            case "zip":
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
                    sb.append(processFileByExtension(fileExtension, zis)).append("\n");
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
