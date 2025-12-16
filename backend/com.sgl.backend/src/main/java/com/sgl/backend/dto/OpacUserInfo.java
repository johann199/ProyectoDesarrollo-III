package com.sgl.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpacUserInfo {
    private String name;
    private String program;
    private String code;
    private String email;
    private String document;
}
