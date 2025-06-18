package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users",
    indexes = [
        Index(columnList = "email", name = "idx_user_email"),
    ])
data class User(
    @Id
    @Column(name = "pid", length = 50)
    val pid: String,

    @Column(name = "first_name", nullable = false, length = 50)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    val lastName: String,

    @Column(name = "email", nullable = false, unique = true, length = 255)
    val email: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    val userRoles: Set<UserRole> = emptySet()
) : BaseEntity(),UserDetails{
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return userRoles.map { SimpleGrantedAuthority(it.role.name) }
    }

    override fun getPassword(): String? {
        return passwordHash
    }

    override fun getUsername(): String? {
        return pid
    }
}