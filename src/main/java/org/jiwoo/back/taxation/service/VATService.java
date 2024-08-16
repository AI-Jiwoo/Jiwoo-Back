package org.jiwoo.back.taxation.service;

import java.util.List;
import java.util.Map;

public interface VATService {

    void updateVATRates();

    Map<String, List<Map<String, String>>> getVATInfo();

    String getFormattedTaxRates();

}