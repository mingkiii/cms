package com.zerobase.cms.order.client.mailgun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Data
public class SendMailForm {
    private String from;
    private String to;
    private String subject;
    private String text;
}
