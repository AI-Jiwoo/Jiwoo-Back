package org.jiwoo.back.marketresearch.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.marketresearch.dto.MarketSizeGrowthDTO;

public interface MarketResearchService {
    MarketSizeGrowthDTO getMarketSizeAndGrowth(BusinessDTO businessDTO);
}