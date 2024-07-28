package org.jiwoo.back.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.common.exception.UserEmailDuplicateException;
import org.jiwoo.back.user.aggregate.vo.SignupRequestVO;
import org.jiwoo.back.user.aggregate.vo.MessageResponseVO;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Slf4j
@Validated
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/exist/email")
    public ResponseEntity<Boolean> existEmail(@RequestBody SignupRequestVO request) {

        if (authService.existEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(true);
        }
        return ResponseEntity.status(HttpStatus.OK).body(false);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponseVO> signup(@Valid @RequestBody SignupRequestVO request) {

        try {
            AuthDTO userInfo = AuthDTO.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .birthDate(LocalDate.parse(request.getBirthDate()))
                    .gender(request.getGender())
                    .phoneNo(request.getPhoneNo())
                    .build();
            authService.signUp(userInfo);
        } catch (UserEmailDuplicateException e) {
            return ResponseEntity.badRequest().body(new MessageResponseVO(e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponseVO("[ERROR] 잘못된 요청입니다."));
        }

        return ResponseEntity.ok().body(new MessageResponseVO(request.getName() + "님 회원 가입을 축하합니다."));
    }
}
