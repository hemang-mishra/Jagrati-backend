package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Formula
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
    var passwordHash: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_email_verified", nullable = false)
    var isEmailVerified: Boolean = false,

    @Column(name = "profile_picture_url", nullable = true, length = 512)
    var profilePictureUrl: String? = null,

    // Using Formula to load role names directly in a single query
    @Formula("""COALESCE(
        (SELECT array_agg(r.name)
         FROM user_roles ur 
         JOIN roles r ON ur.role_id = r.id 
         WHERE ur.user_pid = pid), '{}')::text[]
    """)
    private val roleNames: List<String>? = null
) : BaseEntity(), UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // Use the directly loaded role names instead of navigating through userRoles
        return roleNames?.map { SimpleGrantedAuthority(it.trim()) } ?: emptyList()
    }

    override fun getPassword(): String? {
        return passwordHash
    }

    override fun getUsername(): String? {
        return pid
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return isActive
    }
}