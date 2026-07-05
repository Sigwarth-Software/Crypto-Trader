package org.cryptotrader.api.library.services;
//=================================-Imports-==================================

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.User;
import org.cryptotrader.api.library.repository.ProductUserRepository;
import org.cryptotrader.api.library.services.entity.user.ProductUserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple user service for reading and writing ProductUser data.
 *
 * This service is intentionally thin and delegates to ProductUserRepository. It is used by
 * authentication components (e.g. JWT filters and auth controllers) to look up users by id/email/username
 * and to persist new registrations.
 */
@Service
@Slf4j
public class ProductUserService {
    //============================-Variables-=================================
    private final ProductUserRepository productUserRepository;
    private final ProductUserEntityService productUserEntityService;
    //===========================-Constructors-===============================
    @Autowired
    public ProductUserService(ProductUserRepository productUserRepository,
                              ProductUserEntityService productUserEntityService) {
        this.productUserRepository = productUserRepository;
        this.productUserEntityService = productUserEntityService;
    }
    //============================-Methods-===================================

    //----------------------User-Exists-By-Username---------------------------
    /**
     * Check whether a user exists with the given username (case sensitivity depends on repository).
     * @param username the username to check
     * @return true if a user record exists
     */
    public boolean userExistsByUsername(String username) {
        return this.productUserRepository.existsByUsername(username);
    }

    /**
     * Find a ProductUser by email.
     * @param email email address
     * @return matching ProductUser or null if none found
     */
    public ProductUser getUserByEmail(String email) {
        return this.productUserRepository.getUserByEmail(email);
    }

    /**
     * Check whether a user exists with the given email.
     * @param email email address to test
     * @return true if a user with that email exists
     */
    public boolean userExistsByEmail(String email) {
        return this.productUserRepository.existsByEmail(email);
    }
    //------------------------Get-User-By-Username----------------------------
    /**
     * Fetch a ProductUser by username.
     * @param username username value
     * @return ProductUser or null if not found
     */
    public ProductUser getUserByUsername(String username) {
        return this.productUserRepository.getUserByUsername(username);
    }
    //--------------------------Compare-Password------------------------------
    /**
     * Safely compare a plaintext password against the user's stored password.
     * @param user the user whose password hash to compare
     * @param password the plaintext password to verify
     * @return true when the password matches
     */
    public boolean comparePassword(User user, String password) {
        boolean passwordsMatch = user.getSafePassword().compareUnencodedPassword(password);
        return passwordsMatch;
    }
    //-----------------------------Save-User----------------------------------
    /**
     * Persist a ProductUser.
     * @param user the entity to save
     */
    public void saveUser(ProductUser user) {
        log.info("Saving user: {}", user.getUsername());
//        this.productUserRepository.save(user);
        this.productUserEntityService.save(user);
    }
    //---------------------------Get-User-By-Id-------------------------------
    /**
     * Fetch a ProductUser by database id.
     * @param id numeric id
     * @return ProductUser or null if none
     */
    public ProductUser getUserById(Long id) {
//        return this.productUserRepository.getUserById(id);
        return this.productUserEntityService.findById(id).orElse(null);
    }
}
