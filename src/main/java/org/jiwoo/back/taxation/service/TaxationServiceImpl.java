package org.jiwoo.back.taxation.service;

import lombok.extern.slf4j.Slf4j;
import org.jiwoo.back.business.aggregate.entity.Business;
import org.jiwoo.back.business.dto.BusinessDTO;
import org.jiwoo.back.business.repository.BusinessRepository;
import org.jiwoo.back.user.aggregate.entity.User;
import org.jiwoo.back.user.dto.AuthDTO;
import org.jiwoo.back.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
public class TaxationServiceImpl implements TaxationService{

    private UserRepository userRepository;
    private BusinessRepository businessRepository;

    public TaxationServiceImpl(UserRepository userRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    // business code로 회원 정보 조회
    @Override
    public AuthDTO findByBusinessCode(BusinessDTO businessDTO) {
        int businessId = businessDTO.getId();
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NoSuchElementException("사업을 찾을 수 없습니다: " + businessId));

        User user = business.getUser();

        AuthDTO authDTO = new AuthDTO(user.getName(), user.getEmail(), user.getPassword(), user.getProvider(), user.getSnsId(), user.getBirthDate(), user.getGender(), user.getPhoneNo());

        return authDTO;
    }

    @Override
    public String getBusinessType(BusinessDTO businessDTO) {
        return "";
    }

    //



}
