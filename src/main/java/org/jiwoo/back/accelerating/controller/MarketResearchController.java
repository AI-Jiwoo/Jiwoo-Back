package org.jiwoo.back.accelerating.controller;

import org.jiwoo.back.accelerating.aggregate.vo.ResponseSimilarVO;
import org.jiwoo.back.accelerating.aggregate.vo.ResponseTrendCustomerTechnologyVO;
import org.jiwoo.back.accelerating.dto.MarketResearchHistoryDTO;
import org.jiwoo.back.accelerating.service.MarketResearchService;
import org.jiwoo.back.accelerating.aggregate.vo.ResponseMarketResearchVO;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.BiFunction;
import java.util.function.Function;

@RestController
@RequestMapping("/market-research")
public class MarketResearchController {

    private final MarketResearchService marketResearchService;

    public MarketResearchController(MarketResearchService marketResearchService) {
        this.marketResearchService = marketResearchService;
    }

    /* 설명. 사업 정보 기반 시장규모, 성장률 조회 */
    @PostMapping("/market-size-growth")
    public ResponseEntity<?> getMarketSizeAndGrowth(@RequestBody BusinessDTO businessDTO) {
        return executeMarketResearch(businessDTO, marketResearchService::getMarketSizeAndGrowth, ResponseMarketResearchVO::new);
    }

    /* 설명. 유사 서비스 분석 조회 */
    @PostMapping("/similar-services-analysis")
    public ResponseEntity<?> getSimilarServicesAnalysis(@RequestBody BusinessDTO businessDTO) {
        return executeMarketResearch(businessDTO, marketResearchService::analyzeSimilarServices, ResponseSimilarVO::new);
    }

    /* 설명. 사업 정보 기반 트렌드, 연령층별 고객분포, 기술 동향 조회 */
    @PostMapping("/trend-customer-technology")
    public ResponseEntity<?> getTrendCustomerTechnology(@RequestBody BusinessDTO businessDTO) {
        return executeMarketResearch(businessDTO, marketResearchService::getTrendCustomerTechnology, ResponseTrendCustomerTechnologyVO::new);
    }

    private <T, R> ResponseEntity<?> executeMarketResearch(BusinessDTO businessDTO,
                                                           Function<BusinessDTO, T> serviceMethod,
                                                           BiFunction<String, T, R> responseConstructor) {
        try {
            T result = serviceMethod.apply(businessDTO);
            R response = responseConstructor.apply("조회 성공", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            R errorResponse = responseConstructor.apply("조회 실패: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /* 설명. 시장 조회 이력 저장 */
    @PostMapping("/save-history")
    public ResponseEntity<String> saveMarketResearchHistory(@RequestBody MarketResearchHistoryDTO historyDTO) {
        try {
            marketResearchService.saveMarketResearchHistory(historyDTO);
            return ResponseEntity.ok("조회 이력 저장 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("조회 이력 저장 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 이력 저장 실패: 내부 서버 오류");
        }
    }
}