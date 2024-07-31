package org.jiwoo.back.marketresearch.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.marketresearch.dto.MarketResearchHistoryDTO;
import org.jiwoo.back.marketresearch.dto.MarketSizeGrowthDTO;
import org.jiwoo.back.marketresearch.dto.SimilarServicesAnalysisDTO;
import org.jiwoo.back.marketresearch.dto.TrendCustomerTechnologyDTO;

public interface MarketResearchService {
    MarketSizeGrowthDTO getMarketSizeAndGrowth(BusinessDTO businessDTO);
    SimilarServicesAnalysisDTO analyzeSimilarServices(BusinessDTO businessDTO);
    TrendCustomerTechnologyDTO getTrendCustomerTechnology(BusinessDTO businessDTO);
    void saveMarketResearchHistory(MarketResearchHistoryDTO historyDTO);
}