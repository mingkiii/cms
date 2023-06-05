package com.zerobase.cms.user.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.service.customer.CustomerService;
import com.zerobase.cms.user.service.seller.SellerService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserSignUpServiceTest {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SellerService sellerService;

    @Test
    @DisplayName("Success signUp customer")
    void signUpCustomer() {
        SignUpForm form = SignUpForm.builder()
            .name("홍길동")
            .birth(LocalDate.now())
            .email("qwert@gmail.com")
            .password("aaaa123!")
            .phone("01012345678")
            .build();
        Customer customer = customerService.signUpRequest(form);

        assertNotNull(customer.getId());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("Success signUp seller")
    void signUpSeller() {
        SignUpForm form = SignUpForm.builder()
            .name("홍길동")
            .birth(LocalDate.now())
            .email("zxcv@gmail.com")
            .password("aaaa123!")
            .phone("01012345678")
            .build();
        Seller seller = sellerService.signUpRequest(form);

        assertNotNull(seller.getId());
        assertNotNull(seller.getCreatedAt());
    }
}