package org.jiwoo.back.marketresearch.aggregate.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jiwoo.back.marketresearch.dto.TrendCustomerTechnologyDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTrendCustomerTechnologyVO {
    private String message;
    private TrendCustomerTechnologyDTO data;
}
