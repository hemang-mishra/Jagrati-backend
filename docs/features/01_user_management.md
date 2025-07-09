# Feature 1: User Management & Role System

## Database Schema

### Tables

#### users
```sql
CREATE TABLE users (
    pid VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    profile_picture_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### roles
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### permissions
```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### role_permissions
```sql
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### user_roles
```sql
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY,
    user_pid VARCHAR(50) NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_by_pid VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### volunteer_requests
```sql
CREATE TABLE volunteer_requests (
    id BIGINT PRIMARY KEY,
    roll_number VARCHAR(20) UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    alternate_email VARCHAR(255),
    batch VARCHAR(20),
    programme VARCHAR(100),
    street_address_1 TEXT,
    street_address_2 TEXT,
    pincode VARCHAR(10),
    city VARCHAR(100),
    state VARCHAR(100),
    date_of_birth DATE NOT NULL,
    contact_number VARCHAR(20),
    profile_image_url VARCHAR(255),
    college VARCHAR(100),
    branch VARCHAR(100),
    year_of_study INT,
    status VARCHAR(20) NOT NULL,
    requested_by_pid VARCHAR(50) NOT NULL,
    reviewed_by_pid VARCHAR(50),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (requested_by_pid) REFERENCES users(pid),
    FOREIGN KEY (reviewed_by_pid) REFERENCES users(pid)
);
```

#### role_transitions
```sql
CREATE TABLE role_transitions (
    id BIGINT PRIMARY KEY,
    user_pid VARCHAR(50) NOT NULL,
    from_role_id BIGINT NOT NULL,
    to_role_id BIGINT NOT NULL,
    transition_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_by_pid VARCHAR(50) NOT NULL,
    approved_by_pid VARCHAR(50),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Kotlin Data Classes

### Base Entity
```kotlin
@MappedSuperclass
abstract class BaseEntity {
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
```

### User
```kotlin
@Entity
@Table(name = "users")
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

    @Column(name = "is_email_verified", nullable = false)
    var isEmailVerified: Boolean = false,

    @Column(name = "profile_picture_url", nullable = true, length = 512)
    var profilePictureUrl: String? = null
) : BaseEntity()
```

### Role
```kotlin
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "description")
    val description: String?
) : BaseEntity()
```

### Permission
```kotlin
@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "module", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val module: Module,

    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val action: Action
) : BaseEntity()

enum class Module {
    STUDENT,
    VOLUNTEER,
    ATTENDANCE,
    CLASSWORK,
    HOMEWORK,
    SCHEDULE,
    POST,
    RANKING
}

enum class Action {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    APPROVE,
    REJECT
}
```

### RolePermission
```kotlin
@Entity
@Table(name = "role_permissions")
data class RolePermission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    val permission: Permission
) : BaseEntity()
```

### UserRole
```kotlin
@Entity
@Table(name = "user_roles")
data class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_pid", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,

    @ManyToOne
    @JoinColumn(name = "assigned_by_pid", nullable = false)
    val assignedBy: User
) : BaseEntity()
```

### VolunteerRequest
```kotlin
@Entity
@Table(name = "volunteer_requests")
data class VolunteerRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "roll_number", length = 20, unique = true)
    val rollNumber: String?,

    @Column(name = "first_name", nullable = false, length = 50)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    val lastName: String,

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(name = "alternate_email", length = 255)
    val alternateEmail: String?,

    @Column(name = "batch", length = 20)
    val batch: String?,

    @Column(name = "programme", length = 100)
    val programme: String?,

    @Column(name = "street_address_1")
    val streetAddress1: String?,

    @Column(name = "street_address_2")
    val streetAddress2: String?,

    @Column(name = "pincode", length = 10)
    val pincode: String?,

    @Column(name = "city", length = 100)
    val city: String?,

    @Column(name = "state", length = 100)
    val state: String?,

    @Column(name = "date_of_birth", nullable = false)
    val dateOfBirth: LocalDate,

    @Column(name = "contact_number", length = 20)
    val contactNumber: String?,

    @Column(name = "profile_image_url", length = 255)
    val profileImageUrl: String?,

    @Column(name = "college", length = 100)
    val college: String?,

    @Column(name = "branch", length = 100)
    val branch: String?,

    @Column(name = "year_of_study")
    val yearOfStudy: Int?,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: RequestStatus,

    @ManyToOne
    @JoinColumn(name = "requested_by_pid", nullable = false)
    val requestedBy: User,

    @ManyToOne
    @JoinColumn(name = "reviewed_by_pid")
    val reviewedBy: User?,

    @Column(name = "reviewed_at")
    val reviewedAt: LocalDateTime?
) : BaseEntity()

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class RequestStatus {
    PENDING, APPROVED, REJECTED
}
```

### RoleTransition
```kotlin
@Entity
@Table(name = "role_transitions")
data class RoleTransition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_pid", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "from_role_id", nullable = false)
    val fromRole: Role,

    @ManyToOne
    @JoinColumn(name = "to_role_id", nullable = false)
    val toRole: Role,

    @Column(name = "transition_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val transitionType: TransitionType,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: TransitionStatus,

    @ManyToOne
    @JoinColumn(name = "requested_by_pid", nullable = false)
    val requestedBy: User,

    @ManyToOne
    @JoinColumn(name = "approved_by_pid")
    val approvedBy: User?,

    @Column(name = "approved_at")
    val approvedAt: LocalDateTime?
) : BaseEntity()

enum class TransitionType {
    UPGRADE, DOWNGRADE
}

enum class TransitionStatus {
    PENDING, APPROVED, REJECTED
}
```

## Dependencies Required

Add these dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // For UUID generation
    implementation("com.fasterxml.uuid:java-uuid-generator:4.2.0")

    // For password hashing
    implementation("org.springframework.security:spring-security-crypto")
}
```