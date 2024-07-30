package org.jiwoo.back.marketresearch.aggregate.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jiwoo.back.marketresearch.dto.SimilarServicesAnalysisDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseSimilarVO {
    private String message;
    private SimilarServicesAnalysisDTO data;
}