package org.jiwoo.back.taxation.service;

import java.util.List;
import java.util.Map;

public interface IncomeTaxService {

    void updateIncomeTaxRates();

    List<Map<String, String>> getIncomeTaxRates();

    String getFormattedTaxRates();
}
