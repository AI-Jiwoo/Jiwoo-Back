package org.jiwoo.back.user.service;

import org.jiwoo.back.common.exception.UserEmailDuplicateException;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.aggregate.enums.UserRole;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public boolean existEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void signUp(AuthDTO request) throws UserEmailDuplicateException {

        if (existEmail(request.getEmail())) {
            throw new UserEmailDuplicateException();
        }

        User user = authDtoToUser(request);

        userRepository.save(user);
    }

    private User authDtoToUser(AuthDTO request) {
        String email = "";
        String password = "";
        String provider = "LOCAL";
        String snsId = "";
        String gender = "";
        String phoneNo = "";

        if (request.getEmail() != null) {
            email = request.getEmail();
            password = bCryptPasswordEncoder.encode(request.getPassword());
        }
        if (request.getProvider() != null) {
            provider = request.getProvider();
            snsId = request.getSnsId();
        }
        if (request.getGender() != null) {
            gender = request.getGender();
        }
        if (request.getPhoneNo() != null) {
            phoneNo = request.getPhoneNo();
        }

        return User.builder()
                .name(request.getName())
                .email(email)
                .password(password)
                .provider(provider)
                .snsId(snsId)
                .userRole(UserRole.ROLE_NORMAL)
                .birthDate(request.getBirthDate())
                .gender(gender)
                .phoneNo(phoneNo)
                .build();
    }
}
