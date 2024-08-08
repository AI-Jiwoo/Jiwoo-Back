package org.jiwoo.back.taxaxtion.service;

import org.jiwoo.back.taxation.service.IncomeTaxServiceImpl;
import org.jiwoo.back.taxation.service.VATServiceImpl;
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
    private VATServiceImpl vatService;

    @InjectMocks
    private IncomeTaxServiceImpl incomeTaxService;

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

        // 주기적 업데이트 실행
        vatService.updateVATRates();

        // given
        // when
        Map<String, List<Map<String, String>>> vatInfo = vatService.getVATInfo();

        // then
//        assertNotNull(vatInfo, "vatInfo is null");
//        assertTrue(vatInfo.containsKey("regularVat"), "일반 과세자 정보가 없습니다.");
//        assertTrue(vatInfo.containsKey("simplifiedVat"), "간이 과세자 정보가 없습니다.");

        List<Map<String, String>> regularVatRates = vatInfo.get("regularVat");
        List<Map<String, String>> simplifiedVatRates = vatInfo.get("simplifiedVat");

        System.out.println("일반 과세자 정보: " + regularVatRates);
        System.out.println("간이 과세자 정보: " + simplifiedVatRates);

        // 로그 추가
        System.out.println("테스트 - 일반 과세자 정보 크기: " + regularVatRates.size());
        System.out.println("테스트 - 간이 과세자 정보 크기: " + simplifiedVatRates.size());

        assertFalse(regularVatRates.isEmpty(), "일반 과세자 정보가 비어있습니다.");
        assertFalse(simplifiedVatRates.isEmpty(), "간이 과세자 정보가 비어있습니다.");

        // 일반 과세자 정보 검증
        regularVatRates.forEach(rate -> {
            assertNotNull(rate.get("category"), "category가 null입니다.");
            assertNotNull(rate.get("description"), "description이 null입니다.");
            assertNotNull(rate.get("tax_rate"), "tax_rate가 null입니다.");
            assertEquals("일반과세자", rate.get("vatType"), "vatType이 일반과세자가 아닙니다.");
        });

        // 간이 과세자 정보 검증
        simplifiedVatRates.forEach(rate -> {
            assertNotNull(rate.get("category"), "category가 null입니다.");
            assertNotNull(rate.get("description"), "description이 null입니다.");
            assertNotNull(rate.get("tax_rate"), "tax_rate가 null입니다.");
            assertEquals("간이과세자", rate.get("vatType"), "vatType이 간이과세자가 아닙니다.");
        });

        // 포맷된 세율 정보 출력
        String formattedTaxRates = vatService.getFormattedTaxRates();
        System.out.println(formattedTaxRates);
    }


    @DisplayName("종합소득세 정보 크롤링 테스트")
    @Test
    void testGetIncomeRates(){
        //given
        //when
        //주기적 업데이트 실행
        incomeTaxService.updateIncomeTaxRates();

        List<Map<String, String>> incomeTaxRates = incomeTaxService.getIncomeTaxRates();

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
        String formattedTaxRates = incomeTaxService.getFormattedTaxRates("종합소득세");
        System.out.println(formattedTaxRates);

    }
}
