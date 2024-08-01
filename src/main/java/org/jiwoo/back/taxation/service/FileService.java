package org.jiwoo.back.taxation.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    //파일 변환(텍스트화)
    String preprocessFile (MultipartFile file) throws Exception;


}
