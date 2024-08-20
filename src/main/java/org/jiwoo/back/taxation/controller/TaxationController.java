package org.jiwoo.back.taxation.controller;

import org.jiwoo.back.taxation.aggregate.vo.ResponseTaxationVO;
import org.jiwoo.back.taxation.dto.TaxationResponseDTO;
import org.jiwoo.back.taxation.service.FileService;
import org.jiwoo.back.taxation.service.HomeTaxAPIService;
import org.jiwoo.back.taxation.service.TaxationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/taxation")
public class TaxationController {

    private final HomeTaxAPIService homeTaxAPIService;
    private final FileService fileService;
    private final TaxationService taxationService;

    @Autowired
    public TaxationController(HomeTaxAPIService homeTaxAPIService, FileService fileService, @Qualifier("taxationService") TaxationService taxationService) {
        this.homeTaxAPIService = homeTaxAPIService;
        this.fileService = fileService;
        this.taxationService = taxationService;
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> preprocessFile(@RequestParam("transactionFiles") List<MultipartFile> transactionFiles,
                                                 @RequestParam("incomeTaxProof") MultipartFile incomeTaxProof) throws Exception {

        //파일 전처리
        List<String> transactionFileTexts = fileService.preprocessTransactionFiles(transactionFiles);
        String incomeTaxProofText = fileService.preprocessIncomeTaxProofFiles(incomeTaxProof);

        //결과
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("transactionFiles", transactionFileTexts);
        responseData.put("incomeTaxProof", incomeTaxProofText);

        return ResponseEntity.ok().body(responseData);
    }


    @PostMapping("/")
    public ResponseEntity<TaxationResponseDTO> getTaxation(@RequestParam("transactionFiles") List<MultipartFile> transactionFiles,
                                                           @RequestParam("incomeTaxProof") MultipartFile incomeTaxProof,
                                                           @RequestParam("question1") String question1,
                                                           @RequestParam("question2") String question2,
                                                           @RequestParam("question3") String question3,
                                                           @RequestParam("question4") String question4,
                                                           @RequestParam("question5") String question5,
                                                           @RequestParam("businessId") int businessId,
                                                           @RequestParam("bank") String bank) {

        try{

            TaxationResponseDTO taxationResponseDTO = taxationService.getTaxation(transactionFiles, incomeTaxProof, question1, question2, question3, question4, question5, businessId, bank);
            return ResponseEntity.ok(taxationResponseDTO);
        }catch(Exception e){
            return ResponseEntity.status(500).body(null);
        }

    }


}
