package com.zerobase.cms.user.application;

import static com.zerobase.cms.user.exception.ErrorCode.ALREADY_REGISTER_USER;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.service.SignUpCustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final SignUpCustomerService signUpCustomerService;

    public String customerSignUp(SignUpForm form) {
        if (signUpCustomerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            Customer customer = signUpCustomerService.signUpRequest(form);
            String code = getRandomCode();
            SendMailForm sendMailForm = SendMailForm.builder()
                .from("send@email.com")
                .to(form.getEmail())
                .subject("Verification Email!")
                .text(getVerificationEmailBody(
                    customer.getEmail(), customer.getName(), code)
                    )
                .build();

            mailgunClient.sendEmail(sendMailForm);
            signUpCustomerService.changeCustomerValidateEmail(customer.getId(), code);

            return "회원 가입에 성공하였습니다.";
        }
    }

    private String getRandomCode() {
        return RandomStringUtils.random(10, true,true);
    }

    private String getVerificationEmailBody(String email, String name, String code) {
        StringBuilder sb = new StringBuilder();
        return sb.append("Hello ").append(name)
            .append("! Please click link for verification.\n\n")
            .append("http://localhost:8081/signup/verify/customer?email=")
            .append(email)
            .append("&code=")
            .append(code).toString();
    }

    public void customerVerify(String email, String code) {
        signUpCustomerService.verifyEmail(email, code);
    }
}
