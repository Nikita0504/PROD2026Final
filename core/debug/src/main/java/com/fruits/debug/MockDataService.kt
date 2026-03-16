package com.fruits.debug

import com.fruits.domain.model.chat.Chat
import com.fruits.domain.model.chat.ChatDetail
import com.fruits.domain.model.chat.ChatMessage
import com.fruits.domain.model.chat.SentMessage
import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.model.interactions.IncomingLike
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate

class MockDataService : DebugMockData {

    override var userMock: User = User(
        id = "test",
        firstName = "Test",
        secondName = "User",
        email = "test@test.com",
        avatarFileKey = "test",
        readyToGive = true
    )

    override var tokensMock: Tokens = Tokens(
        accessToken = "test_access_token",
        refreshToken = "test_refresh_token"
    )

    override var userProfileUpdateMock: UserProfileUpdate = UserProfileUpdate(
        description = "Test description",
        photoFilesKeys = emptyList()
    )

    override var uploadingDataMock: UploadingData = UploadingData(
        url = "https://mock-upload.test/image",
        key = "mock_file_key",
    )

    override var incomingLikes: List<IncomingLike> = listOf(
        IncomingLike(
            likedByUserId = "user_201",
            firstName = "Анна",
            secondName = "Кузнецова",
            age = 28,
            city = "Москва",
            description = "Очень понравились ваши яблоки, хотела бы забрать немного на выходных.",
            photoFileKeys = listOf("https://example.com/mock/incoming_like_1.jpg"),
            createdAt = "2025-03-01T10:15:00Z",
        ),
        IncomingLike(
            likedByUserId = "user_202",
            firstName = "Сергей",
            secondName = "Михайлов",
            age = 35,
            city = "Санкт-Петербург",
            description = "Интересует ваша клубника, могу подъехать сегодня вечером.",
            photoFileKeys = listOf("https://example.com/mock/incoming_like_2.jpg"),
            createdAt = "2025-03-02T14:30:00Z",
        ),
    )

    override var recommendations: List<Recommendations> = listOf(
        Recommendations(
            userId = "user_101",
            firstName = "Алексей",
            secondName = "Смирнов",
            age = 32,
            city = "Москва",
            photoFileKeys = listOf("https://s13.stc.yc.kpcdn.net/share/i/12/7888060/wr-960.webp"),
            description = "Свежие яблоки прямо с дачи. Сорт Антоновка, очень сочные. Отдам соседям или всем желающим!",
            explanation = listOf("Похожие интересы", "Рядом с вами")
        ),
        Recommendations(
            userId = "user_102",
            firstName = "Мария",
            secondName = "Иванова",
            age = 27,
            city = "Санкт-Петербург",
            photoFileKeys = listOf("https://i.stena.ee/21/2022-02-06_080725.jpg"),
            description = "Урожай клубники — больше, чем ожидала. Вся сладкая, без нитратов. Забирайте бесплатно!",
            explanation = listOf("Активный пользователь")
        ),
        Recommendations(
            userId = "user_103",
            firstName = "Дмитрий",
            secondName = "Козлов",
            age = 45,
            city = "Подмосковье",
            photoFileKeys = listOf("https://news.store.rambler.ru/img/2f026387a8b128a22c0490bf57b2cf68?img-format=auto&img-1-resize=height:400,fit:max&img-2-filter=sharpen"),
            description = "Кабачки и огурцы — некуда девать. Выращено без химии на своём огороде.",
            explanation = listOf("Рядом с вами")
        ),
        Recommendations(
            userId = "user_104",
            firstName = "Ольга",
            secondName = "Петрова",
            age = 38,
            city = "Казань",
            photoFileKeys = listOf("https://cdn.fishki.net/upload/post/2020/05/02/3306035/tn/risunok1.jpg"),
            description = "Домашнее варенье из черники. Сварила с запасом, готова поделиться парой баночек.",
            explanation = listOf("Похожие интересы", "Популярный пользователь")
        ),
        Recommendations(
            userId = "user_105",
            firstName = "Игорь",
            secondName = "Новиков",
            age = 51,
            city = "Новосибирск",
            photoFileKeys = listOf("https://www.menslife.com/upload/iblock/0d8/realnye_lyudi_obladayushchie_supersposobnostyami.jpg"),
            description = "Груши сорта Конференц — упали с дерева, но целые. Нужно быстро забрать, не будут долго лежать.",
            explanation = listOf("Рядом с вами")
        ),
        Recommendations(
            userId = "user_106",
            firstName = "Екатерина",
            secondName = "Белова",
            age = 29,
            city = "Щёлково",
            photoFileKeys = listOf("https://www.film.ru/sites/default/files/styles/thumb_1024x450/public/trailers_frame/x-men.jpg"),
            description = "Зелень: укроп, петрушка, базилик. Свежесрезанная, пучками. Самовывоз.",
            explanation = listOf("Активный пользователь")
        ),
        Recommendations(
            userId = "user_108",
            firstName = "Наталья",
            secondName = "Орлова",
            age = 34,
            city = "Краснодар",
            photoFileKeys = listOf("https://ir.ozone.ru/s3/multimedia-p/6651744109.jpg"),
            description = "Персики из Краснодара. Привезли родственники, не осилим. Очень спелые, нужно забрать сегодня.",
            explanation = listOf("Рядом с вами", "Похожие интересы")
        )
    )

    override fun updateRecommendations(block: MutableList<Recommendations>.() -> Unit) {
        recommendations = recommendations.toMutableList().apply(block)
    }

    override fun updateUser(block: User.() -> User) {
        userMock = userMock.block()
    }

    override fun updateTokens(block: Tokens.() -> Tokens) {
        tokensMock = tokensMock.block()
    }

    override fun updateUserProfileUpdate(block: UserProfileUpdate.() -> UserProfileUpdate) {
        userProfileUpdateMock = userProfileUpdateMock.block()
    }

    override fun uploadingDataMockUpdate(block: UploadingData.() -> UploadingData) {
        uploadingDataMock = uploadingDataMock.block()
    }

    // region Chat mocks

    override var chats: List<Chat> = listOf(
        Chat(
            id = "chat_101",
            name = "Алексей Смирнов",
            lastMessage = "Привет! Как урожай?",
            timestamp = 1_742_000_000_000L,
            unreadCount = 2,
            avatarUrl = null,
        ),
        Chat(
            id = "chat_102",
            name = "Мария Иванова",
            lastMessage = "Заберу яблоки вечером",
            timestamp = 1_742_000_500_000L,
            unreadCount = 0,
            avatarUrl = null,
        ),
    )

    private val _chatDetails: MutableMap<String, ChatDetail> = mutableMapOf(
        "chat_101" to ChatDetail(
            id = "chat_101",
            title = "Алексей Смирнов",
            status = "active",
            messages = listOf(
                ChatMessage(
                    id = "msg_1",
                    senderUserId = "user_101",
                    text = "Привет! Как урожай?",
                    createdAt = "2025-03-01T10:15:00Z",
                ),
                ChatMessage(
                    id = "msg_2",
                    senderUserId = "me",
                    text = "Привет, отличный! Есть много яблок.",
                    createdAt = "2025-03-01T10:16:00Z",
                ),
            ),
            createdAt = "2025-03-01T10:00:00Z",
            updatedAt = "2025-03-01T10:16:00Z",
        ),
        "chat_102" to ChatDetail(
            id = "chat_102",
            title = "Мария Иванова",
            status = "active",
            messages = listOf(
                ChatMessage(
                    id = "msg_3",
                    senderUserId = "user_102",
                    text = "Заберу яблоки вечером",
                    createdAt = "2025-03-02T18:30:00Z",
                ),
            ),
            createdAt = "2025-03-02T18:00:00Z",
            updatedAt = "2025-03-02T18:30:00Z",
        ),
    )

    override val chatDetails: Map<String, ChatDetail>
        get() = _chatDetails

    override fun appendChatMessage(chatId: String, text: String): SentMessage {
        val createdAt = "2025-03-03T12:00:00Z"
        val newId = "msg_${System.currentTimeMillis()}"
        val sent = SentMessage(
            id = newId,
            createdAt = createdAt,
        )

        val detail = _chatDetails[chatId]
        if (detail != null) {
            val newMessage = ChatMessage(
                id = newId,
                senderUserId = "me",
                text = text,
                createdAt = createdAt,
            )
            _chatDetails[chatId] = detail.copy(
                messages = detail.messages + newMessage,
                updatedAt = createdAt,
            )
        }

        // Обновляем lastMessage в списке чатов
        chats = chats.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    lastMessage = text,
                    timestamp = System.currentTimeMillis(),
                    unreadCount = 0,
                )
            } else chat
        }

        return sent
    }

    override fun deleteChat(chatId: String) {
        chats = chats.filterNot { it.id == chatId }
        _chatDetails.remove(chatId)
    }

    // endregion
}
