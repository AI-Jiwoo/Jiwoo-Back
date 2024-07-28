package org.jiwoo.back.business.controller;

import org.jiwoo.back.business.aggregate.vo.ResponseBusinessVO;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/business")
public class BusinessController {
    private final BusinessService businessService;

    @Autowired
    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /* 설명. 사업 내용 조회 */
    @GetMapping("/id/{id}")
    public ResponseEntity<ResponseBusinessVO> getBusinessById(@PathVariable("id") int id) {
        BusinessDTO business = businessService.findBusinessById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseBusinessVO("조회 성공", List.of(business)));
    }

}
