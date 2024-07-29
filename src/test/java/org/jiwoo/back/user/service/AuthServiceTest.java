package org.jiwoo.back.user.service;

import org.jiwoo.back.common.exception.UserEmailDuplicateException;
import org.jiwoo.back.user.dto.AuthDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @DisplayName("회원가입 테스트")
    @Test
    void signUp() throws UserEmailDuplicateException {

        // given
        AuthDTO authDTO = createAuthDTO();

        // when
        authService.signUp(authDTO);

        // then
        assertTrue(authService.existEmail(authDTO.getEmail()));
    }

    @DisplayName("존재하는 이메일 회원가입 예외 테스트")
    @Test
    void duplicateEmail() throws UserEmailDuplicateException {

        // given
        AuthDTO authDTO = createAuthDTO();

        // when
        authService.signUp(authDTO);

        // then
        assertThrows(UserEmailDuplicateException.class, () -> authService.signUp(authDTO));
    }

    @DisplayName("존재하는 이메일 조회 테스트")
    @Test
    void existEmail() throws UserEmailDuplicateException {

        // given
        AuthDTO authDTO = createAuthDTO();

        // when
        authService.signUp(authDTO);

        // then
        assertTrue(authService.existEmail(authDTO.getEmail()));
    }

    @DisplayName("존재하지 않는 이메일 조회 테스트")
    @Test
    void notExistEmail() {

        // given
        String email = "존재하지않는이메일@notExist.aaa";

        // when, then
        assertFalse(authService.existEmail(email));
    }

    private AuthDTO createAuthDTO() {
        return AuthDTO.builder()
                .name("TestName")
                .email("test@test.com")
                .password("testPassword")
                .birthDate(LocalDate.now())
                .gender("Male")
                .phoneNo("010-1234-1234")
                .build();
    }
}