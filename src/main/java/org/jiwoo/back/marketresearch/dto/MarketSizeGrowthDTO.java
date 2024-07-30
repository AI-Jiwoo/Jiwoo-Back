package org.jiwoo.back.marketresearch.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarketSizeGrowthDTO {
    private String marketSize;
    private String growthRate;
}