package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "pid", length = 50)
    val pid: String,
    val email: String,
    val hashedPassword: String
): UserDetails{
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String? {
        return hashedPassword
    }

    override fun getUsername(): String? {
        return email
    }
}