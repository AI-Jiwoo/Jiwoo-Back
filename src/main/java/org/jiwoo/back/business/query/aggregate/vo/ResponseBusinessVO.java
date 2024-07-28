package org.jiwoo.back.business.query.aggregate.vo;

import lombok.*;
import org.jiwoo.back.business.query.dto.BusinessDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseBusinessVO {
    private String message;
    List<BusinessDTO> business;
}
