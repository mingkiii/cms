package com.zerobase.cms.order.client.mailgun;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value(value = "${mailgunApiKey}")
    private String mailgunKey;

    @Bean
    @Qualifier(value = "mailgun")
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("api", mailgunKey);
    }
}
