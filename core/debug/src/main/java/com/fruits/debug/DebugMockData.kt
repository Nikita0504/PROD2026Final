package com.fruits.debug

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.model.chat.ChatDetail
import com.fruits.domain.model.chat.SentMessage
import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate

/**
 * Общий интерфейс для доступа к мок‑данным, используемый в репозиториях и debug‑панели.
 * Реализация по умолчанию — [MockDataService].
 */
interface DebugMockData {

    var userMock: User
    var tokensMock: Tokens
    var userProfileUpdateMock: UserProfileUpdate
    var uploadingDataMock: UploadingData
    var incomingLikes: List<IncomingLike>
    var recommendations: List<Recommendations>

    var chats: List<Chat>
    val chatDetails: Map<String, ChatDetail>

    fun updateRecommendations(block: MutableList<Recommendations>.() -> Unit)

    fun updateUser(block: User.() -> User)

    fun updateTokens(block: Tokens.() -> Tokens)

    fun updateUserProfileUpdate(block: UserProfileUpdate.() -> UserProfileUpdate)

    fun uploadingDataMockUpdate(block: UploadingData.() -> UploadingData)

    /**
     * Добавляет новое сообщение в детализацию чата и возвращает отправленное сообщение.
     * Используется мок‑реализацией отправки сообщений.
     */
    fun appendChatMessage(chatId: String, text: String): SentMessage

    /**
     * Удаляет чат и его детализацию из коллекций мок‑данных.
     */
    fun deleteChat(chatId: String)
}

