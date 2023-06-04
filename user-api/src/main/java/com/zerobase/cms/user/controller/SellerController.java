package com.zerobase.cms.user.controller;

import static com.zerobase.cms.user.exception.ErrorCode.NOT_FOUND_USER;

import com.zerobase.cms.domain.config.JwtAuthenticationProvider;
import com.zerobase.cms.domain.domain.common.UserVo;
import com.zerobase.cms.user.domain.dto.SellerDto;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final JwtAuthenticationProvider provider;
    private final SellerService sellerService;

    @GetMapping("/getInfo")
    public ResponseEntity<SellerDto> getInfo(
        @RequestHeader(name = "X-AUTH-TOKEN")
        String token
    ) {

        UserVo user = provider.getUserVo(token);
        Seller seller = sellerService.findByIdAndEmail(user.getId(),
                user.getEmail())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return ResponseEntity.ok(SellerDto.from(seller));
    }
}
