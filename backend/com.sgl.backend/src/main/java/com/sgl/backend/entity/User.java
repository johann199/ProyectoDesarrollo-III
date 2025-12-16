package com.sgl.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "code")
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String code;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String document;

    @Column
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
