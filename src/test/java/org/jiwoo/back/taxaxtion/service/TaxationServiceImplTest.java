package org.jiwoo.back.taxaxtion.service;

import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.service.BusinessService;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.jiwoo.back.taxation.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = "python.server.url.taxation=http://localhost:5000/taxation")
public class TaxationServiceImplTest {
    //python 서버로 전송 및 응답 테스트

    @InjectMocks
    private TaxationServiceImpl taxationService;

    @Mock
    private FileService fileService;

    @Mock
    private HomeTaxAPIService homeTaxAPIService;

    @Mock
    private IncomeTaxService incomeTaxService;

    @Mock
    private VATService vatService;

    @Mock
    private BusinessService businessService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(taxationService, "pythonServerUrl", "http://localhost:5000/taxation");
    }



    public TaxationServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testeDataToDTO() throws Exception {
        // Mock MultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "transactionFile", "test.txt", "text/plain", "Sample transaction data".getBytes());

        List<MultipartFile> transactionFiles = Arrays.asList(mockFile);
        MultipartFile incomeTaxProof = new MockMultipartFile(
                "incomeTaxProof", "taxProof.txt", "text/plain", "Sample tax proof data".getBytes());

        BusinessDTO mockBusinessDTO = new BusinessDTO();
        mockBusinessDTO.setBusinessNumber("1");

        // Mock FileService의 동작 정의
        when(businessService.findBusinessById(anyInt())).thenReturn(mockBusinessDTO);
        when(fileService.preprocessTransactionFiles(transactionFiles))
                .thenReturn(Arrays.asList("Sample transaction data"));
//        when(fileService.preprocessTransactionFiles(anyList())).thenReturn(mockTransactionFiles());
        when(homeTaxAPIService.getBusinessType(anyString())).thenReturn("부가가치세 일반과세자");
        when(incomeTaxService.getFormattedTaxRates()).thenReturn("10%");
        when(vatService.getFormattedTaxRates()).thenReturn("5%");


        TaxationDTO taxationDTO = taxationService.dataToDTO(
                transactionFiles,
                incomeTaxProof,
                1,
                "기업은행"
        );
        assertEquals("부가가치세 일반과세자", taxationDTO.getBusinessType());
        assertEquals("10%", taxationDTO.getIncomeRates());
        assertEquals("5%", taxationDTO.getVatInfo());

        verify(fileService, times(1)).preprocessTransactionFiles(anyList());
        verify(homeTaxAPIService, times(1)).getBusinessType(anyString());
        verify(incomeTaxService, times(1)).getFormattedTaxRates();
        verify(vatService, times(1)).getFormattedTaxRates();
        verify(businessService, times(1)).findBusinessById(anyInt());
    }


    @Test
    public void testSendToPythonServer() throws Exception {


        TaxationDTO taxationDTO = mock(TaxationDTO.class);

//        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
//        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
//
//        BufferedReader mockReader = mock(BufferedReader.class);
//        when(mockReader.readLine()).thenReturn("Mock Python Response");
//
//        URL mockURL = mock(URL.class);
//        when(mockURL.openConnection()).thenReturn(mockConnection);

        String response = taxationService.sendToPythonServer(taxationDTO);

        assertEquals("{\"response\": \"Mock Python Response\"}", response);
//        verify(mockConnection).getOutputStream();
    }

}
