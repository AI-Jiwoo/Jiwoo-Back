package org.jiwoo.back.marketresearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimilarServicesAnalysisDTO {
    private List<String> similarServices;
    private String analysis;
}