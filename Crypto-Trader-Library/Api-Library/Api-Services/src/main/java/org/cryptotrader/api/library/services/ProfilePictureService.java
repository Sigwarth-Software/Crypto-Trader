package org.cryptotrader.api.library.services;
//=================================-Imports-==================================

import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.api.library.repository.ProfilePictureRepository;
import org.cryptotrader.api.library.services.entity.user.ProfilePictureEntityService;
import org.cryptotrader.api.library.services.models.ProfilePictureOperations;
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
    public ProfilePictureService(ProfilePictureRepository profilePictureRepository,
                                 ProfilePictureEntityService profilePictureEntityService) {
        this.profilePictureRepository = profilePictureRepository;
        this.profilePictureEntityService = profilePictureEntityService;
    }
    //============================-Methods-===================================
    public void saveProfilePicture(ProfilePicture profilePicture) {
//        this.profilePictureRepository.save(profilePicture);
        this.profilePictureEntityService.save(profilePicture);
    }
    @Transactional
    public Optional<ProfilePicture> findByUserId(Long userId) {
        ProfilePicture profilePicture = this.profilePictureRepository.findByUserId(userId);
        if (profilePicture == null) {
            return Optional.empty();
        } else {
            return Optional.of(profilePicture);
        }
    }
    public boolean existsByUserId(Long userId) {
        return this.profilePictureRepository.existsByUserId(userId);
    }
}
