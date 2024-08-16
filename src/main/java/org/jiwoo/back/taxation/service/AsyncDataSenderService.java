package org.jiwoo.back.taxation.service;

import org.jiwoo.back.taxation.dto.TaxationDTO;

import java.util.concurrent.CompletableFuture;

public interface AsyncDataSenderService {

    CompletableFuture<Void> sendDataToPythonServer(TaxationDTO taxationDTO);
}
