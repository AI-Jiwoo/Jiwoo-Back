package org.jiwoo.back.taxation.service;

import java.util.List;
import java.util.Map;

public interface IncomeTaxService {

    List<Map<String, String>> getIncomeTaxRates();

    String getFormattedTaxRates(String category);
}
