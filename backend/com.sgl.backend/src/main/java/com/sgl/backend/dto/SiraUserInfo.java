package com.sgl.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiraUserInfo {
    private String program;
    private String name;
    private String code;
    private String email;
    private String document;
}
