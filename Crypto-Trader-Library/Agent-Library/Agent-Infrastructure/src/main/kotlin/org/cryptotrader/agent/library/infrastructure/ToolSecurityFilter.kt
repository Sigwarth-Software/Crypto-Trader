package org.cryptotrader.agent.library.infrastructure

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.cryptotrader.security.library.infrastructure.annotation.AdminRestricted
import org.cryptotrader.security.library.infrastructure.annotation.SuperAdminRestricted
import org.cryptotrader.security.library.infrastructure.annotation.UserRestricted
import org.cryptotrader.api.library.entity.user.UserRoleTier
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Aspect
@Component
class ToolSecurityFilter {

    @Before("@annotation(org.springframework.ai.tool.annotation.Tool) && (" +
        "@annotation(org.cryptotrader.security.library.infrastructure.annotation.SuperAdminRestricted) || " +
        "@annotation(org.cryptotrader.security.library.infrastructure.annotation.AdminRestricted) || " +
        "@annotation(org.cryptotrader.security.library.infrastructure.annotation.UserRestricted))"
    )
    fun checkSecurity(joinPoint: JoinPoint) {
        // TODO: Remove once better auth is implemented.
        if (System.getProperty("cryptotrader.local-development") == "true") {
            return
        }
        val method: Method = (joinPoint.signature as MethodSignature).method
        val requiredRole: UserRoleTier? = when {
            method.isAnnotationPresent(SuperAdminRestricted::class.java) -> UserRoleTier.SUPER_ADMIN
            method.isAnnotationPresent(AdminRestricted::class.java) -> UserRoleTier.ADMIN
            method.isAnnotationPresent(UserRestricted::class.java) -> UserRoleTier.USER
            else -> null
        }

        if (requiredRole != null) {
            val authentication: Authentication = SecurityContextHolder.getContext().authentication
            val userRole: UserRoleTier = authentication?.authorities
                ?.mapNotNull { UserRoleTier.fromAuthority(it.authority) }
                ?.maxByOrNull { it.level } ?: throw SecurityException("No valid role found for user.")

            if (userRole.level < requiredRole.level) {
                throw SecurityException("Access denied: Required role ${requiredRole.name}, but user has ${userRole.name}.")
            }
        }
    }
}
