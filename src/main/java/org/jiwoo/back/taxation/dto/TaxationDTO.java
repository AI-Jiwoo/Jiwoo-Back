package org.jiwoo.back.taxation.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaxationDTO {

    private FileDTO transactionList;  // 거래내역 파일들 (multi)
    private FileDTO incomeTaxProof;   // 소득/세액공제 파일 (one)

    private String businessId;          // 사업 번호
    private String businessCode;        // 사업자 등록 번호
    private String currentDate;         // 현재 날짜
    private String bank;                // 은행 정보
    private String businessType;        // 사업자유형 정보
    private String businessContent;     // 사업 내용
    private String vatInfo;             // 부가가치세 정보
    private String incomeRates;         // 종합소득세 정보


}
