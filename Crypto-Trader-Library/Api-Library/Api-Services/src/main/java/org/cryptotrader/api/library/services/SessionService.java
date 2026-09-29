package org.cryptotrader.api.library.services;

import jakarta.servlet.http.HttpSession;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.User;
import org.cryptotrader.api.library.entity.user.admin.AdminUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionService {
    //=============================-Methods-==================================

    //--------------------------User-In-Session-------------------------------
    public boolean userInSession(final @NotNull HttpSession session) {
        return session.getAttribute("user") != null;
    }
    //-----------------------Get-User-From-Session----------------------------
    public @NotNull Optional<ProductUser> getUserFromSession(final @NotNull HttpSession session) {
        final ProductUser user = (ProductUser) session.getAttribute("product-user");
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(user);
        }
    }
    public void setSessionUser(final @NotNull HttpSession session, final User user) {
        if (user instanceof ProductUser) {
            session.setAttribute("product-user", user);
        } else if (user instanceof AdminUser) {
            session.setAttribute("admin-user", user);
        } else {
            throw new IllegalArgumentException("User is not a ProductUser or AdminUser");
        }
    }
    public void removeSessionUser(final @NotNull HttpSession session) {
        session.removeAttribute("user");
        session.removeAttribute("product-user");
        session.removeAttribute("admin-user");
    }
}
