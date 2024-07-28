package org.jiwoo.back.business.query.dto;

import java.util.Date;

public class BusinessDTO {
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

    public BusinessDTO() {
    }

    public BusinessDTO(int id, String businessName, String businessNumber, String businessScale, double businessBudget, String businessContent, String businessPlatform, String businessLocation, Date businessStartDate, String nation, String investmentStatus, String customerType, int userId, int startupStageId) {
        this.id = id;
        this.businessName = businessName;
        this.businessNumber = businessNumber;
        this.businessScale = businessScale;
        this.businessBudget = businessBudget;
        this.businessContent = businessContent;
        this.businessPlatform = businessPlatform;
        this.businessLocation = businessLocation;
        this.businessStartDate = businessStartDate;
        this.nation = nation;
        this.investmentStatus = investmentStatus;
        this.customerType = customerType;
        this.userId = userId;
        this.startupStageId = startupStageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public String getBusinessScale() {
        return businessScale;
    }

    public void setBusinessScale(String businessScale) {
        this.businessScale = businessScale;
    }

    public double getBusinessBudget() {
        return businessBudget;
    }

    public void setBusinessBudget(double businessBudget) {
        this.businessBudget = businessBudget;
    }

    public String getBusinessContent() {
        return businessContent;
    }

    public void setBusinessContent(String businessContent) {
        this.businessContent = businessContent;
    }

    public String getBusinessPlatform() {
        return businessPlatform;
    }

    public void setBusinessPlatform(String businessPlatform) {
        this.businessPlatform = businessPlatform;
    }

    public String getBusinessLocation() {
        return businessLocation;
    }

    public void setBusinessLocation(String businessLocation) {
        this.businessLocation = businessLocation;
    }

    public Date getBusinessStartDate() {
        return businessStartDate;
    }

    public void setBusinessStartDate(Date businessStartDate) {
        this.businessStartDate = businessStartDate;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getInvestmentStatus() {
        return investmentStatus;
    }

    public void setInvestmentStatus(String investmentStatus) {
        this.investmentStatus = investmentStatus;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStartupStageId() {
        return startupStageId;
    }

    public void setStartupStageId(int startupStageId) {
        this.startupStageId = startupStageId;
    }

    @Override
    public String toString() {
        return "BusinessDTO{" +
                "id=" + id +
                ", businessName='" + businessName + '\'' +
                ", businessNumber='" + businessNumber + '\'' +
                ", businessScale='" + businessScale + '\'' +
                ", businessBudget=" + businessBudget +
                ", businessContent='" + businessContent + '\'' +
                ", businessPlatform='" + businessPlatform + '\'' +
                ", businessLocation='" + businessLocation + '\'' +
                ", businessStartDate=" + businessStartDate +
                ", nation='" + nation + '\'' +
                ", investmentStatus='" + investmentStatus + '\'' +
                ", customerType='" + customerType + '\'' +
                ", userId=" + userId +
                ", startupStageId=" + startupStageId +
                '}';
    }
}
