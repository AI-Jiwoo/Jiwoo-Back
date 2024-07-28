package org.jiwoo.back.user.aggregate.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.jiwoo.back.common.util.RegularExpression;

@Getter
public class AuthRequestVO {

    @NotBlank(message = "[ERROR] 이름은 필수 기입 정보입니다.")
    @Pattern(regexp = RegularExpression.NAME,
            message = "[ERROR] 이름은 한글, 영어로만 조합해야 합니다.")
    private String name;

    @Email(message = "[ERROR] 유효하지 않은 이메일입니다.")
    private String email;
    private String password;
    private String provider;
    private String snsId;

    @NotBlank(message = "[ERROR] 생년월일은 필수 기입 정보입니다.")
    @Pattern(regexp = RegularExpression.LOCAL_DATE,
            message = "[ERROR] 형식이 올바르지 않습니다.(yyyy-mm-dd)")
    private String birthDate;

    private String gender;

    @Pattern(regexp = RegularExpression.PHONE_NUMBER,
            message = "[ERROR] 유효하지 않은 전화번호입니다.")
    private String phoneNo;
}
