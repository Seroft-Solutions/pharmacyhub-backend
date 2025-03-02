package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokensDTO implements BaseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
}
