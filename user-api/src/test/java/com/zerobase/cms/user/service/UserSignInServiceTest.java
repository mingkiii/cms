package com.zerobase.cms.user.service;

import static com.zerobase.cms.user.exception.ErrorCode.LOGIN_CHECK_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.zerobase.cms.domain.config.JwtAuthenticationProvider;
import com.zerobase.cms.domain.domain.common.UserType;
import com.zerobase.cms.user.application.SignInApplication;
import com.zerobase.cms.user.domain.SignInForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.service.customer.CustomerService;
import com.zerobase.cms.user.service.seller.SellerService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserSignInServiceTest {

    private SignInApplication signInApplication;
    @Mock
    private CustomerService customerService;
    @Mock
    private SellerService sellerService;
    @Mock
    private JwtAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        signInApplication = new SignInApplication(customerService, sellerService, provider);
    }

    // -------customer
    @Test
    @DisplayName("Success signIn customer")
    void customerLoginToken_ValidCustomer() {
        // Given
        Customer customer = Customer.builder()
            .id(1L)
            .email("test@example.com")
            .verify(true)
            .password("password!")
            .build();

        SignInForm form = SignInForm.builder()
            .email(customer.getEmail())
            .password(customer.getPassword())
            .build();

        when(customerService.findValidCustomer(form.getEmail(), form.getPassword()))
            .thenReturn(Optional.of(customer));

        String expectedToken = "generated_token";
        when(provider.createToken(customer.getEmail(), customer.getId(), UserType.CUSTOMER))
            .thenReturn(expectedToken);

        // When
        String resultToken = signInApplication.customerLoginToken(form);

        // Then
        assertEquals(expectedToken, resultToken);
    }

    @Test
    @DisplayName("Fail signIn customer")
    void customerLoginToken_InvalidCustomer() {
        // Given
        Customer customer = Customer.builder()
            .id(1L)
            .email("test2@example.com")
            .verify(false)
            .password("password!")
            .build();

        SignInForm form = SignInForm.builder()
            .email(customer.getEmail())
            .password(customer.getPassword())
            .build();

        when(customerService.findValidCustomer(form.getEmail(), form.getPassword()))
            .thenReturn(Optional.empty());

        // When/Then
        CustomException exception = assertThrows(CustomException.class,
            () -> signInApplication.customerLoginToken(form));

        // 예외 처리 결과를 확인
        assertEquals(LOGIN_CHECK_FAIL, exception.getErrorCode());
    }

    // -------seller
    @Test
    @DisplayName("Success signIn seller")
    void sellerLoginToken_ValidSeller() {
        // Given
        Seller seller = Seller.builder()
            .id(1L)
            .email("test@example.com")
            .verify(true)
            .password("password!")
            .build();

        SignInForm form = SignInForm.builder()
            .email(seller.getEmail())
            .password(seller.getPassword())
            .build();

        when(sellerService.findValidSeller(form.getEmail(), form.getPassword()))
            .thenReturn(Optional.of(seller));

        String expectedToken = "generated_token";
        when(provider.createToken(seller.getEmail(), seller.getId(), UserType.SELLER))
            .thenReturn(expectedToken);

        // When
        String resultToken = signInApplication.sellerLoginToken(form);

        // Then
        assertEquals(expectedToken, resultToken);
    }

    @Test
    @DisplayName("Fail signIn seller")
    void sellerLoginToken_InvalidSeller() {
        // Given
        Seller seller = Seller.builder()
            .id(1L)
            .email("test2@example.com")
            .verify(false)
            .password("password!")
            .build();

        SignInForm form = SignInForm.builder()
            .email(seller.getEmail())
            .password(seller.getPassword())
            .build();

        when(sellerService.findValidSeller(form.getEmail(), form.getPassword()))
            .thenReturn(Optional.empty());

        // When/Then
        CustomException exception = assertThrows(CustomException.class,
            () -> signInApplication.sellerLoginToken(form));

        // 예외 처리 결과를 확인
        assertEquals(LOGIN_CHECK_FAIL, exception.getErrorCode());
    }
}
