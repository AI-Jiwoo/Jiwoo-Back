package org.jiwoo.back.user.service;

import org.jiwoo.back.common.exception.UserEmailDuplicateException;
import org.jiwoo.back.user.dto.AuthDTO;

public interface AuthService {
    boolean existEmail(String email);
    void signUp(AuthDTO request) throws UserEmailDuplicateException;
}
