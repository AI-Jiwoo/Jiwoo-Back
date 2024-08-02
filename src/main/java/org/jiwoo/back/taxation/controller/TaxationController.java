package org.jiwoo.back.taxation.controller;

import org.jiwoo.back.taxation.service.HomeTaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
@RequestMapping
public class TaxationController {

    private final HomeTaxService homeTaxService;

    @Autowired
    public TaxationController(HomeTaxService homeTaxService) {
        this.homeTaxService = homeTaxService;
    }

}
