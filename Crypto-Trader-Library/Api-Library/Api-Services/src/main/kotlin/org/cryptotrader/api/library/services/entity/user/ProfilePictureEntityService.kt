package org.cryptotrader.api.library.services.entity.user

import org.cryptotrader.api.library.entity.user.ProfilePicture
import org.cryptotrader.api.library.repository.ProfilePictureRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProfilePictureEntityService @Autowired constructor(
    repository: ProfilePictureRepository
) : BaseEntityService<ProfilePicture, Long, ProfilePictureRepository>(repository) {

}