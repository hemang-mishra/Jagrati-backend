package org.jagrati.jagratibackend.config

import jakarta.transaction.Transactional
import org.jagrati.jagratibackend.entities.Permission
import org.jagrati.jagratibackend.entities.Role
import org.jagrati.jagratibackend.entities.RolePermission
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.entities.UserRole
import org.jagrati.jagratibackend.entities.enums.AllPermissions
import org.jagrati.jagratibackend.repository.PermissionRepository
import org.jagrati.jagratibackend.repository.RolePermissionRepository
import org.jagrati.jagratibackend.repository.RoleRepository
import org.jagrati.jagratibackend.repository.UserRepository
import org.jagrati.jagratibackend.repository.UserRoleRepository
import org.jagrati.jagratibackend.security.HashEncoder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializationConfig(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val hashEncoder: HashEncoder,
) {
    private val logger = LoggerFactory.getLogger(DataInitializationConfig::class.java)

    @Value("\${app.super-admin.email:admin@jagrati.com}")
    private lateinit var superAdminEmail: String

    @Value("\${app.super-admin.password:admin123}")
    private lateinit var superAdminPassword: String

    @Value("\${app.super-admin.first-name:Super}")
    private lateinit var superAdminFirstName: String

    @Value("\${app.super-admin.last-name:Admin}")
    private lateinit var superAdminLastName: String

    @Value("\${app.super-admin.pid:SUPER_ADMIN_001}")
    private lateinit var superAdminPid: String

    @Bean
    @Transactional
    fun initializeDatabase() = CommandLineRunner {
        initializeRolesAndPermissions()
        initializeSuperAdmin()
    }

    @Transactional
    fun initializeRolesAndPermissions() {
        logger.info("Starting initialize database with roles and permissions")

        // Initialize roles
        val roles = initializeRoles()

        // Initialize permissions
        val permissions = initializePermissions()

        // Initialize role-permission mappings
        initializeRolePermissions(roles, permissions)

        logger.info("Database initialization completed successfully")
    }

    @Transactional
    fun initializeSuperAdmin() {
        logger.info("Initializing super admin user")

        // Check if super admin already exists
        val existingSuperAdmin = userRepository.findByEmail(superAdminEmail)
        if (existingSuperAdmin != null) {
            logger.info("Super admin user already exists: ${existingSuperAdmin.email}")
            return
        }

        // Create super admin user
        val hashedPassword = hashEncoder.encode(superAdminPassword)
        val superAdminUser = User(
            pid = superAdminPid,
            firstName = superAdminFirstName,
            lastName = superAdminLastName,
            email = superAdminEmail,
            passwordHash = hashedPassword,
            isActive = true,
            isEmailVerified = true // Super admin email is pre-verified
        )

        val savedSuperAdmin = userRepository.save(superAdminUser)
        logger.info("Created super admin user: ${savedSuperAdmin.email}")

        // Assign SUPER_ADMIN role to the user
        val superAdminRole = roleRepository.findByName(InitialRoles.SUPER_ADMIN.roleString)
        if (superAdminRole != null) {
            val userRole = UserRole(
                id = 0,
                user = savedSuperAdmin,
                role = superAdminRole,
                assignedBy = savedSuperAdmin // Self-assigned for initial super admin
            )
            userRoleRepository.save(userRole)
            logger.info("Assigned SUPER_ADMIN role to super admin user")
        } else {
            logger.error("SUPER_ADMIN role not found! Cannot assign role to super admin user.")
        }

        logger.info("Super admin initialization completed")
        logger.info("Super Admin Credentials:")
        logger.info("Email: $superAdminEmail")
        logger.info("Password: $superAdminPassword")
        logger.info("PID: $superAdminPid")
    }

    private fun initializeRoles(): Map<String, Role> {
        val rolesMap = mutableMapOf<String, Role>()

        InitialRoles.values().forEach { initialRole ->
            val existingRole = roleRepository.findByName(initialRole.roleString)
            if (existingRole == null) {
                val role = Role(
                    name = initialRole.roleString,
                    description = initialRole.description
                )
                val savedRole = roleRepository.save(role)
                rolesMap[initialRole.roleString] = savedRole
                logger.info("Created role: ${initialRole.roleString}")
            } else {
                rolesMap[initialRole.roleString] = existingRole
                logger.info("Role already exists: ${initialRole.roleString}")
            }
        }

        return rolesMap
    }

    private fun initializePermissions(): Map<String, Permission> {
        val permissionsMap = mutableMapOf<String, Permission>()

        AllPermissions.values().forEach { allPermission ->
            val existingPermission = permissionRepository.findByName(allPermission.name)
            if (existingPermission == null) {
                val permission = Permission(
                    name = allPermission.name,
                    description = allPermission.getDescription(),
                    module = allPermission.module,
                    action = allPermission.action
                )
                val savedPermission = permissionRepository.save(permission)
                permissionsMap[allPermission.name] = savedPermission
                logger.info("Created permission: ${allPermission.name}")
            } else {
                permissionsMap[allPermission.name] = existingPermission
                logger.info("Permission already exists: ${allPermission.name}")
            }
        }

        return permissionsMap
    }

    private fun initializeRolePermissions(roles: Map<String, Role>, permissions: Map<String, Permission>) {
        // SUPER_ADMIN gets all permissions
        val superAdminRole = roles[InitialRoles.SUPER_ADMIN.roleString]
        if (superAdminRole != null) {
            AllPermissions.values().forEach { allPermission ->
                val permission = permissions[allPermission.name]
                if (permission != null) {
                    val existingRolePermission = rolePermissionRepository.findByRoleAndPermission(superAdminRole, permission)
                    if (existingRolePermission == null) {
                        val rolePermission = RolePermission(
                            role = superAdminRole,
                            permission = permission
                        )
                        rolePermissionRepository.save(rolePermission)
                        logger.info("Assigned permission ${allPermission.name} to SUPER_ADMIN")
                    }
                }
            }
        }

        // VOLUNTEER gets limited permissions
        val volunteerRole = roles[InitialRoles.VOLUNTEER.roleString]
        if (volunteerRole != null) {
            val volunteerPermissions = listOf(
                // Student permissions
                AllPermissions.STUDENT_READ,
                AllPermissions.STUDENT_VIEW_PROFICIENCY,
                AllPermissions.STUDENT_UPDATE_PROFICIENCY,

                // Attendance permissions
                AllPermissions.ATTENDANCE_MARK_STUDENT,
                AllPermissions.ATTENDANCE_READ,

                // Classwork permissions
                AllPermissions.CLASSWORK_CREATE,
                AllPermissions.CLASSWORK_READ,
                AllPermissions.CLASSWORK_UPDATE,
                AllPermissions.CLASSWORK_UPLOAD_IMAGES,

                // Homework permissions
                AllPermissions.HOMEWORK_ASSIGN,
                AllPermissions.HOMEWORK_READ,
                AllPermissions.HOMEWORK_CHECK,

                // Schedule permissions
                AllPermissions.SCHEDULE_READ,
                AllPermissions.SCHEDULE_CREATE,

                // Post permissions
                AllPermissions.POST_CREATE,
                AllPermissions.POST_READ,
                AllPermissions.POST_UPDATE,

                // Remarks permissions
                AllPermissions.REMARKS_CREATE,
                AllPermissions.REMARKS_READ,
                AllPermissions.REMARKS_VOTE,

                // Syllabus permissions
                AllPermissions.SYLLABUS_READ,

                // Group permissions
                AllPermissions.GROUP_READ
            )

            volunteerPermissions.forEach { allPermission ->
                val permission = permissions[allPermission.name]
                if (permission != null) {
                    val existingRolePermission = rolePermissionRepository.findByRoleAndPermission(volunteerRole, permission)
                    if (existingRolePermission == null) {
                        val rolePermission = RolePermission(
                            role = volunteerRole,
                            permission = permission
                        )
                        rolePermissionRepository.save(rolePermission)
                        logger.info("Assigned permission ${allPermission.name} to VOLUNTEER")
                    }
                }
            }
        }

        // USER gets minimal permissions (read-only access to public data)
        val userRole = roles[InitialRoles.USER.roleString]
        if (userRole != null) {
            val userPermissions = listOf(
                AllPermissions.STUDENT_READ,
                AllPermissions.ATTENDANCE_READ,
                AllPermissions.SYLLABUS_READ,
                AllPermissions.POST_READ
            )

            userPermissions.forEach { allPermission ->
                val permission = permissions[allPermission.name]
                if (permission != null) {
                    val existingRolePermission = rolePermissionRepository.findByRoleAndPermission(userRole, permission)
                    if (existingRolePermission == null) {
                        val rolePermission = RolePermission(
                            role = userRole,
                            permission = permission
                        )
                        rolePermissionRepository.save(rolePermission)
                        logger.info("Assigned permission ${allPermission.name} to USER")
                    }
                }
            }
        }
    }
}

enum class InitialRoles(val roleString: String, val description: String) {
    SUPER_ADMIN("SUPER_ADMIN", "Has all permissions."),
    VOLUNTEER("VOLUNTEER", "Has volunteer permissions."),
    USER("USER", "Has no permissions."),
}