package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.taxation.dto.TaxationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AsyncDataSenderServiceImpl implements  AsyncDataSenderService{

    @Value("${python.server.url.taxation}")
    private String pythonServerUrl;



    @Override
    public CompletableFuture<Void> sendDataToPythonServer(TaxationDTO taxationDTO) {

        return null;
    }
}
