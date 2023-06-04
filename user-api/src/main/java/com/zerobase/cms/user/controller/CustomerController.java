package com.zerobase.cms.user.controller;

import static com.zerobase.cms.user.exception.ErrorCode.NOT_FOUND_USER;

import com.zerobase.cms.domain.config.JwtAuthenticationProvider;
import com.zerobase.cms.domain.domain.common.UserVo;
import com.zerobase.cms.user.domain.customer.CustomerDto;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final JwtAuthenticationProvider provider;
    private final CustomerService customerService;

    @GetMapping("/getInfo")
    public ResponseEntity<CustomerDto> getInfo(
        @RequestHeader(name = "X-AUTH-TOKEN")
        String token
    ) {

        UserVo user = provider.getUserVo(token);
        Customer customer = customerService.findByIdAndEmail(user.getId(),
                user.getEmail())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return ResponseEntity.ok(CustomerDto.from(customer));
    }

}
