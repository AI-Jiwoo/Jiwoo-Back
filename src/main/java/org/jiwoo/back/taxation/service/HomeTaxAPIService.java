package org.jiwoo.back.taxation.service;

public interface HomeTaxAPIService {

    // 홈택스 api로 사업자 유형 가져오기
    String getBusinessType(String businessCode);
}
