package org.jiwoo.back.taxation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileDTO {

    private String fileName;            // 파일 제목
    private String content;             // 파일 텍스트화
}
