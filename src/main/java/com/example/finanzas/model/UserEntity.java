package com.example.finanzas.model;

import com.example.finanzas.model.enums.MonedaEnum;
import com.example.finanzas.model.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    /** Foto de perfil almacenada como data URL en base64 (p.ej. "data:image/png;base64,..."). */
    @Column(columnDefinition = "LONGTEXT")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String fotoPerfil;

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    /** Moneda preferida. Default a nivel de columna para no romper ALTER sobre filas existentes. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(3) DEFAULT 'EUR'")
    @Builder.Default
    private MonedaEnum moneda = MonedaEnum.EUR;

    /** Idioma preferido (código ISO: "es", "en"...). Las traducciones viven en el front. */
    @Column(nullable = false, columnDefinition = "VARCHAR(8) DEFAULT 'es'")
    @Builder.Default
    private String idioma = "es";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}