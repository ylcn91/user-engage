package com.userengage.models;

import com.userengage.utils.PasswordUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@RequiredArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String passwordSalt;

    public void setPassword(String password) {
        this.passwordSalt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hashPassword(password, this.passwordSalt);
    }

    public boolean checkPassword(String password) {
        String hash = PasswordUtils.hashPassword(password, this.passwordSalt);
        return this.passwordHash.equals(hash);
    }
}
