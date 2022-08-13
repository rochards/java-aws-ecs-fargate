package br.com.rochards.aws_projeto01.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {
    private String url;
    private long expirationTime;
}
