package org.jiwoo.back.taxation.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessForTaxDTO {

    private int id;
    private String businessName;
    private String businessNumber;
    private String businessScale;
    private double businessBudget;
    private String businessContent;
    private String businessPlatform;
    private String businessLocation;
    private Date businessStartDate;
    private String nation;
    private String investmentStatus;
    private String customerType;
    private int userId;
    private int startupStageId;
    private List<Integer> categoryIds;
}