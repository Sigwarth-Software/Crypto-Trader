package org.cryptotrader.api.library.services;
//=================================-Imports-==================================

import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.api.library.repository.ProfilePictureRepository;
import org.cryptotrader.api.library.services.entity.user.ProfilePictureEntityService;
import org.cryptotrader.api.library.services.models.ProfilePictureOperations;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfilePictureService implements ProfilePictureOperations {
    //============================-Variables-=================================
    private final ProfilePictureRepository profilePictureRepository;
    private final ProfilePictureEntityService profilePictureEntityService;
    //===========================-Constructors-===============================
    @Autowired
    public ProfilePictureService(final ProfilePictureRepository profilePictureRepository,
                                 final ProfilePictureEntityService profilePictureEntityService) {
        this.profilePictureRepository = profilePictureRepository;
        this.profilePictureEntityService = profilePictureEntityService;
    }
    //============================-Methods-===================================
    public void saveProfilePicture(final @NotNull ProfilePicture profilePicture) {
//        this.profilePictureRepository.save(profilePicture);
        this.profilePictureEntityService.save(profilePicture);
    }
    @Transactional
    public @NotNull Optional<ProfilePicture> findByUserId(final Long userId) {
        final ProfilePicture profilePicture = this.profilePictureRepository.findByUserId(userId);
        if (profilePicture == null) {
            return Optional.empty();
        } else {
            return Optional.of(profilePicture);
        }
    }
    public boolean existsByUserId(final Long userId) {
        return this.profilePictureRepository.existsByUserId(userId);
    }
}
