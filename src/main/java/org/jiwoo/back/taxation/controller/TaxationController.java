package org.jiwoo.back.taxation.controller;

import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.taxation.service.HomeTaxAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/taxation")
public class TaxationController {

    private final HomeTaxAPIService homeTaxAPIService;

    @Autowired
    public TaxationController(HomeTaxAPIService homeTaxAPIService) {
        this.homeTaxAPIService = homeTaxAPIService;

    }

    @PostMapping("/")
    public ResponseEntity<TaxationDTO> getTaxation(@RequestParam("businessId") int businessId) {

        try{
            TaxationDTO taxationDTO = homeTaxAPIService.getTaxationInfo(businessId);
            return ResponseEntity.ok(taxationDTO);
        }catch(Exception e){
            return ResponseEntity.status(500).body(null);
        }

    }


}
