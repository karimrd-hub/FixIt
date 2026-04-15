package com.example.fixit.module.user.entity;

import com.example.fixit.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
}
