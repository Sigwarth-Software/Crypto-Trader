package org.cryptotrader.admin.service

import jakarta.servlet.http.HttpSession
import org.cryptotrader.api.library.entity.user.admin.AdminUser
import org.springframework.stereotype.Service

@Service
class AuthGuardService {
    fun isAuthenticated(session: HttpSession): Boolean {
        // TODO: Outdated. JWT with DPoP is now used instead.
        val adminUser: AdminUser? = session.getAttribute("adminUser") as AdminUser?
        return adminUser != null
    }
}