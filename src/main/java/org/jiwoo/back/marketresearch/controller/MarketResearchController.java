package org.jiwoo.back.marketresearch.controller;

import org.jiwoo.back.marketresearch.service.MarketResearchService;
import org.jiwoo.back.marketresearch.dto.MarketSizeGrowthDTO;
import org.jiwoo.back.marketresearch.aggregate.vo.ResponseMarketResearchVO;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/market-research")
public class MarketResearchController {

    private final MarketResearchService marketResearchService;

    public MarketResearchController(MarketResearchService marketResearchService) {
        this.marketResearchService = marketResearchService;
    }

    /* 설명. 사업 정보 기반 시장규모, 성장률 조회 */
    @PostMapping("/market-size-growth")
    public ResponseEntity<ResponseMarketResearchVO> getMarketSizeAndGrowth(@RequestBody BusinessDTO businessDTO) {
        try {
            MarketSizeGrowthDTO result = marketResearchService.getMarketSizeAndGrowth(businessDTO);
            ResponseMarketResearchVO response = new ResponseMarketResearchVO("조회 성공", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ResponseMarketResearchVO errorResponse = new ResponseMarketResearchVO("조회 실패: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}