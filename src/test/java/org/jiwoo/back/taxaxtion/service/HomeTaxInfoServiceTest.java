package org.jiwoo.back.taxaxtion.service;

import org.jiwoo.back.taxation.service.HomeTaxInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HomeTaxInfoServiceTest {

    @InjectMocks
    private HomeTaxInfoServiceImpl homeTaxInfoService;

    @Qualifier("defaultTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("부가가치세 정보 크롤링 테스트")
    @Test
    void testGetVATInfo() {

        //주기적 업데이트 실행
        homeTaxInfoService.updateTaxRates();

        //given
        //when
        List<Map<String, String>> taxRates = homeTaxInfoService.getVATInfo();

        // then
        assertNotNull(taxRates);
        assertTrue(!taxRates.isEmpty(), "부가가치세 정보가 비어있습니다.");
        taxRates.forEach(rate -> {
            assertNotNull(rate.get("category"), "category가 null입니다.");
            assertNotNull(rate.get("description"), "description이 null입니다.");
            assertNotNull(rate.get("tax_rate"), "tax_rate가 null입니다.");
        });

        // 포맷된 세율 정보 출력
        String formattedTaxRates = homeTaxInfoService.getFormattedTaxRates("부가가치세");
        System.out.println(formattedTaxRates);
    }

    @DisplayName("종합소득세 정보 크롤링 테스트")
    @Test
    void testGetIncomeRates(){
        //given
        //when
        //주기적 업데이트 실행
        homeTaxInfoService.updateTaxRates();

        List<Map<String, String>> incomeTaxRates = homeTaxInfoService.getIncomeTaxRates();

        //then
        assertNotNull(incomeTaxRates);
        assertTrue(!incomeTaxRates.isEmpty(), "종합소득세 정보가 비어있습니다.");
        incomeTaxRates.forEach(rate -> {
            assertNotNull(rate.get("category"), "category가 null입니다.");
            assertNotNull(rate.get("description"), "description이 null입니다.");
            assertNotNull(rate.get("tax_rate"), "tax_rate가 null입니다.");
            if (rate.containsKey("additional_info")) {
                assertNotNull(rate.get("additional_info"), "additional_info가 null입니다.");
            }
        });

        //포맷된 세율 정보 출력
        String formattedTaxRates = homeTaxInfoService.getFormattedTaxRates("종합소득세");
        System.out.println(formattedTaxRates);

    }
}
