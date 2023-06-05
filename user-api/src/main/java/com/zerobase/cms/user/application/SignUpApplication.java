package com.zerobase.cms.user.application;

import static com.zerobase.cms.user.exception.ErrorCode.ALREADY_REGISTER_USER;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.service.customer.CustomerService;
import com.zerobase.cms.user.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final CustomerService customerService;
    private final SellerService sellerService;

    public String customerSignUp(SignUpForm form) {
        if (customerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            Customer customer = customerService.signUpRequest(form);
            String code = getRandomCode();
            SendMailForm sendMailForm = SendMailForm.builder()
                .from("zerotest@sandbox6bb81553342140e6a988f2e9fffff930.mailgun.org")
                .to(form.getEmail())
                .subject("Verification Email!")
                .text(getVerificationEmailBody(
                    customer.getEmail(), customer.getName(), "customer", code)
                    )
                .build();

            mailgunClient.sendEmail(sendMailForm);
            customerService.changeCustomerValidateEmail(customer.getId(), code);

            return "회원 가입에 성공하였습니다.";
        }
    }
    public void customerVerify(String email, String code) {
        customerService.verifyEmail(email, code);
    }

    public String sellerSignUp(SignUpForm form) {
        if (sellerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            Seller seller = sellerService.signUpRequest(form);
            String code = getRandomCode();
            SendMailForm sendMailForm = SendMailForm.builder()
                .from("zerotest@sandbox6bb81553342140e6a988f2e9fffff930.mailgun.org")
                .to(form.getEmail())
                .subject("Verification Email!")
                .text(getVerificationEmailBody(
                    seller.getEmail(), seller.getName(), "seller", code)
                )
                .build();

            mailgunClient.sendEmail(sendMailForm);
            sellerService.changeSellerValidateEmail(seller.getId(), code);

            return "회원 가입에 성공하였습니다.";
        }
    }
    public void sellerVerify(String email, String code) {
        sellerService.verifyEmail(email, code);
    }

    private String getRandomCode() {
        return RandomStringUtils.random(10, true,true);
    }

    private String getVerificationEmailBody(String email, String name, String type, String code) {
        StringBuilder sb = new StringBuilder();
        return sb.append("Hello ").append(name)
            .append("! Please click link for verification.\n\n")
            .append("http://localhost:8081/signup/")
            .append(type)
            .append("/verify/?email=")
            .append(email)
            .append("&code=")
            .append(code).toString();
    }
}
