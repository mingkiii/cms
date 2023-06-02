package com.zerobase.cms.user.client;

import com.zerobase.cms.user.client.mailgun.SendMailForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3")
public interface MailgunClient {

    @PostMapping("/sandbox6bb81553342140e6a988f2e9fffff930.mailgun.org/messages")
    String sendEmail(@SpringQueryMap SendMailForm form);
}
