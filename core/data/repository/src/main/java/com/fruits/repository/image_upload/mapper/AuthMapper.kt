package com.fruits.repository.image_upload.mapper

import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.model.user.AuthResult
import com.fruits.network.images.schema.UploadImageUrlScheme
import com.fruits.network.user.schema.UserRegisterSchema
import com.fruits.repository.user_network.mapper.TokensMapper.toDomain
import com.fruits.repository.user_network.mapper.UserMapper.toDomain

object UploadingDataMapper {
    fun UploadImageUrlScheme.toDomain(): UploadingData =
        UploadingData(
            key = key,
            url = url
        )
}