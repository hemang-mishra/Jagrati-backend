package org.jagrati.jagratibackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Formula
import org.jagrati.jagratibackend.entities.enums.DeletionSource
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

/**
 * A login account.
 *
 * Deletion is handled by [org.jagrati.jagratibackend.services.AccountDeletionService],
 * not by `repository.delete()`. There is deliberately no `@SQLDelete` here: an
 * annotation that silently turns a delete into a scrub is easy to trigger by
 * accident and impossible to audit. Rows are retained so that attendance, audit
 * trails and "marked by" references stay resolvable; the personal data on them is
 * scrubbed at deletion time.
 *
 * Note there is also no `@SQLRestriction`. It would apply to association fetches
 * as well as queries, which would break every retained reference to a deleted
 * person — exactly the rows soft deletion exists to preserve. Listings filter
 * explicitly via `...AndDeletedAtIsNull` repository methods instead.
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(columnList = "email", name = "idx_user_email"),
    ]
)
data class User(
    @Id
    @Column(name = "pid", length = 64)
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

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "deleted_by_pid", length = 64)
    var deletedByPid: String? = null,

    @Column(name = "deletion_source", length = 20)
    @Enumerated(EnumType.STRING)
    var deletionSource: DeletionSource? = null,

    // Using Formula to load role names directly in a single query
    @Formula("""COALESCE(
        (SELECT array_agg(r.name)
         FROM user_roles ur
         JOIN roles r ON ur.role_id = r.id
         WHERE ur.user_pid = pid), '{}')::text[]
    """)
    private val roleNames: List<String>? = null
) : BaseEntity(), UserDetails {

    val isDeleted: Boolean get() = deletedAt != null

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
        return isActive && !isDeleted
    }
}
