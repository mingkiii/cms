package com.zerobase.cms.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.service.customer.SignUpCustomerService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SignUpCustomerServiceTest {
    @Autowired
    private SignUpCustomerService service;

    @Test
    void signUp() {
        SignUpForm form = SignUpForm.builder()
            .name("홍길동")
            .birth(LocalDate.now())
            .email("qwert@gmail.com")
            .password("aaaa123!")
            .phone("01012345678")
            .build();
        Customer customer = service.signUpRequest(form);

        assertNotNull(customer.getId());
        assertNotNull(customer.getCreatedAt());
    }
}