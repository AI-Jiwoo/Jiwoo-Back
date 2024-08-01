package org.jiwoo.back.accelerating.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.accelerating.dto.MarketResearchHistoryDTO;
import org.jiwoo.back.accelerating.dto.MarketSizeGrowthDTO;
import org.jiwoo.back.accelerating.dto.SimilarServicesAnalysisDTO;
import org.jiwoo.back.accelerating.dto.TrendCustomerTechnologyDTO;

public interface MarketResearchService {
    MarketSizeGrowthDTO getMarketSizeAndGrowth(BusinessDTO businessDTO);
    SimilarServicesAnalysisDTO analyzeSimilarServices(BusinessDTO businessDTO);
    TrendCustomerTechnologyDTO getTrendCustomerTechnology(BusinessDTO businessDTO);
    void saveMarketResearchHistory(MarketResearchHistoryDTO historyDTO);
}