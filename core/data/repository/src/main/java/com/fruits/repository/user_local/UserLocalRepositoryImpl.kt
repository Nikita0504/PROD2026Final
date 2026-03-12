package com.fruits.repository.user_local

import com.fruits.database.user.dao.UserDao
import com.fruits.domain.model.user.User
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.repository.user_local.mapper.UserDbMapper.toDomain
import com.fruits.repository.user_local.mapper.UserDbMapper.toEntity

class UserLocalRepositoryImpl(
    private val userDao: UserDao
) : UserLocalRepository {

    override suspend fun getCachedUser(): User? =
        userDao.getCurrent()?.toDomain()

    override suspend fun upsertUser(user: User) =
        userDao.upsert(user.toEntity())

    override suspend fun clearCache() =
        userDao.clear()
}