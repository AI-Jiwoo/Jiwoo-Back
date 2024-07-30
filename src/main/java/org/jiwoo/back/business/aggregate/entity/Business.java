package org.jiwoo.back.business.aggregate.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.jiwoo.back.marketresearch.aggregate.entity.MarketResearch;
import org.jiwoo.back.user.aggregate.entity.User;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tbl_business")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "BUSINESS_NAME")
    private String businessName;

    @Column(name = "BUSINESS_NUMBER")
    private String businessNumber;

    @Column(name = "BUSINESS_SCALE")
    private String businessScale;

    @Column(name = "BUSINESS_BUDGET")
    private double businessBudget;

    @Column(name = "BUSINESS_CONTENT")
    private String businessContent;

    @Column(name = "BUSINESS_PLATFORM")
    private String businessPlatform;

    @Column(name = "BUSINESS_LOCATION")
    private String businessLocation;

    @Column(name = "BUSINESS_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date businessStartDate;

    @Column(name = "NATION")
    private String nation;

    @Column(name = "INVESTMENT_STATUS")
    private String investmentStatus;

    @Column(name = "CUSTOMER_TYPE")
    private String customerType;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MarketResearch> marketResearches;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BusinessCategory> businessCategories;
}
