package org.jiwoo.back.taxation.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    //거래내역 파일 변환(텍스트화)
    List<String> preprocessTransactionFiles(List<MultipartFile> transactionFiles) throws Exception;

    //세액/소득공제 파일 변환(텍스트화)
    String preprocessIncomeTaxProofFiles(MultipartFile incomeTaxProofFile) throws Exception;


}
