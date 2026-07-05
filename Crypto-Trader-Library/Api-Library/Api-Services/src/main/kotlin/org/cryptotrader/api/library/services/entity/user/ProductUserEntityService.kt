package org.cryptotrader.api.library.services.entity.user

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.repository.ProductUserRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductUserEntityService @Autowired constructor(
    repository: ProductUserRepository
) : BaseEntityService<ProductUser, Long, ProductUserRepository>(repository) {

}