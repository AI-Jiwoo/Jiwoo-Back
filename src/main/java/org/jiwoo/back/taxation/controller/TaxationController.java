package org.jiwoo.back.taxation.controller;

import org.jiwoo.back.taxation.service.HomeTaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping
public class TaxationController {

    private final HomeTaxService homeTaxService;

    @Autowired
    public TaxationController(HomeTaxService homeTaxService) {
        this.homeTaxService = homeTaxService;
    }

}
