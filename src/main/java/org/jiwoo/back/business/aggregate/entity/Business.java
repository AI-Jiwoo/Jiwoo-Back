package org.jiwoo.back.business.aggregate.entity;

import java.util.Date;


public class Business {
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

    public Business() {
    }

    public Business(int id, String businessName, String businessNumber, String businessScale, double businessBudget, String businessContent, String businessPlatform, String businessLocation, Date businessStartDate, String nation, String investmentStatus, String customerType, int userId, int startupStageId) {
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

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getBusinessScale() {
        return businessScale;
    }

    public double getBusinessBudget() {
        return businessBudget;
    }

    public String getBusinessContent() {
        return businessContent;
    }

    public String getBusinessPlatform() {
        return businessPlatform;
    }

    public String getBusinessLocation() {
        return businessLocation;
    }

    public Date getBusinessStartDate() {
        return businessStartDate;
    }

    public String getNation() {
        return nation;
    }

    public String getInvestmentStatus() {
        return investmentStatus;
    }

    public String getCustomerType() {
        return customerType;
    }

    public int getUserId() {
        return userId;
    }

    public int getStartupStageId() {
        return startupStageId;
    }

    @Override
    public String toString() {
        return "Business{" +
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
