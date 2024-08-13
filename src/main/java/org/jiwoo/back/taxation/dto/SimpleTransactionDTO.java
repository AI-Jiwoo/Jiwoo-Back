package org.jiwoo.back.taxation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SimpleTransactionDTO {

    private String date;                // 날짜
    private String accountCategory;     // 계정과목
    private String transactionDetail;   // 거래내용
    private String client;              // 거래처
    private double incomeAmount;        // 수입 금액
    private double incomeVAT;        // 수입 부가세
    private double costAmount;          // 비용 금액
    private double costVAT;          // 비용 부가세
    private double assetsAmount;        // 사업용자산(유무형자산) 금액
    private double assetsVAT;        // 사업용자산(유무형자산) 부가세
    private String remark;                // 비고
}
