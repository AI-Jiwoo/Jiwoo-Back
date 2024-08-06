package org.jiwoo.back.taxation.dto;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaxationResponseDTO {


    private double totalSales;         // 총 매출액
    private double grossIncome;        // 총 소득
    private double netSales;           // 순 매출액
    private Date startDate;            // 시작날짜
    private Date endDate;               // 마지막 날짜
    private String lossStatus;          // 적자여부
    private double incomeTax;           // 종합소득세
    private int businessId;             // 사업코드
    private double additionalCost;      // 세금절세를 위한 추가비용
}
