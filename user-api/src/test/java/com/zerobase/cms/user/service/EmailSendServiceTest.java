package com.zerobase.cms.user.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailSendServiceTest {
    @Autowired
    private MailgunClient mailgunClient;

    @Test
    public void EmailSendTest() {
        //given
        SendMailForm form = SendMailForm.builder()
            .from("send@email")
            .to("receive@email")
            .subject("Test email from zero base")
            .text("test")
            .build();

        //when
        String result = mailgunClient.sendEmail(form);

        //then
        assertTrue(result.contains("Queued"));
    }
}