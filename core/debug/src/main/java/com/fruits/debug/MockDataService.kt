package com.fruits.debug

import com.fruits.domain.model.image.UploadingData
import com.fruits.domain.model.recommendations.Recommendations
import com.fruits.domain.model.user.Tokens
import com.fruits.domain.model.user.User
import com.fruits.domain.model.user.UserProfileUpdate

class MockDataService {

    var userMock: User = User(
        id = "test",
        firstName = "Test",
        secondName = "User",
        email = "test@test.com",
        avatarFileKey = "test",
        readyToGive = true
    )

    var tokensMock: Tokens = Tokens(
        accessToken = "test_access_token",
        refreshToken = "test_refresh_token"
    )

    var userProfileUpdateMock: UserProfileUpdate = UserProfileUpdate(
        description = "Test description",
        photoFilesKeys = emptyList()
    )

    var uploadingDataMock: UploadingData = UploadingData(
        url = "https://mock-upload.test/image",
        key = "mock_file_key",
    )

    var recommendations: List<Recommendations> = listOf(
        Recommendations(
            id = "rec_001",
            authorId = "user_101",
            authorName = "Алексей Смирнов",
            authorAvatarUrl = "https://s13.stc.yc.kpcdn.net/share/i/12/7888060/wr-960.webp",
            imageUrl = "https://s13.stc.yc.kpcdn.net/share/i/12/7888060/wr-960.webp",
            description = "Свежие яблоки прямо с дачи 🍎 Сорт Антоновка, очень сочные. Отдам соседям или всем желающим!",
            likesCount = 47,
            isLiked = false
        ),
        Recommendations(
            id = "rec_002",
            authorId = "user_102",
            authorName = "Мария Иванова",
            authorAvatarUrl = "https://i.pravatar.cc/150?img=5",
            imageUrl = "https://i.stena.ee/21/2022-02-06_080725.jpg",
            description = "Урожай клубники — больше, чем ожидала 🍓 Вся сладкая, без нитратов. Забирайте бесплатно!",
            likesCount = 134,
            isLiked = true
        ),
        Recommendations(
            id = "rec_003",
            authorId = "user_103",
            authorName = "Дмитрий Козлов",
            authorAvatarUrl = "https://news.store.rambler.ru/img/2f026387a8b128a22c0490bf57b2cf68?img-format=auto&img-1-resize=height:400,fit:max&img-2-filter=sharpen",
            imageUrl = "https://news.store.rambler.ru/img/2f026387a8b128a22c0490bf57b2cf68?img-format=auto&img-1-resize=height:400,fit:max&img-2-filter=sharpen",
            description = "Кабачки и огурцы — некуда девать 🥒 Выращено без химии на своём огороде в Подмосковье.",
            likesCount = 89,
            isLiked = false
        ),
        Recommendations(
            id = "rec_004",
            authorId = "user_104",
            authorName = "Ольга Петрова",
            authorAvatarUrl = "https://cdn.fishki.net/upload/post/2020/05/02/3306035/tn/risunok1.jpg",
            imageUrl = "https://cdn.fishki.net/upload/post/2020/05/02/3306035/tn/risunok1.jpg",
            description = "Домашнее варенье из черники 🫐 Сварила с запасом, готова поделиться парой баночек.",
            likesCount = 212,
            isLiked = true
        ),
        Recommendations(
            id = "rec_005",
            authorId = "user_105",
            authorName = "Игорь Новиков",
            authorAvatarUrl = "https://www.menslife.com/upload/iblock/0d8/realnye_lyudi_obladayushchie_supersposobnostyami.jpg",
            imageUrl = "https://www.menslife.com/upload/iblock/0d8/realnye_lyudi_obladayushchie_supersposobnostyami.jpg",
            description = "Груши сорта Конференц — упали с дерева, но целые 🍐 Нужно быстро забрать, не будут долго лежать.",
            likesCount = 63,
            isLiked = false
        ),
        Recommendations(
            id = "rec_006",
            authorId = "user_106",
            authorName = "Екатерина Белова",
            authorAvatarUrl = "https://www.film.ru/sites/default/files/styles/thumb_1024x450/public/trailers_frame/x-men.jpg",
            imageUrl = "https://www.film.ru/sites/default/files/styles/thumb_1024x450/public/trailers_frame/x-men.jpg",
            description = "Зелень: укроп, петрушка, базилик 🌿 Свежесрезанная, пучками. Самовывоз из Щёлково.",
            likesCount = 31,
            isLiked = false
        ),
        Recommendations(
            id = "rec_008",
            authorId = "user_108",
            authorName = "Наталья Орлова",
            authorAvatarUrl = "https://ir.ozone.ru/s3/multimedia-p/6651744109.jpg4",
            imageUrl = "https://ir.ozone.ru/s3/multimedia-p/6651744109.jpg",
            description = "Персики из Краснодара 🍑 Привезли родственники, не осилим. Очень спелые, нужно забрать сегодня.",
            likesCount = 178,
            isLiked = false
        )
    )

    fun updateRecommendations(block: MutableList<Recommendations>.() -> Unit) {
        recommendations = recommendations.toMutableList().apply(block)
    }

    fun updateUser(block: User.() -> User) {
        userMock = userMock.block()
    }

    fun updateTokens(block: Tokens.() -> Tokens) {
        tokensMock = tokensMock.block()
    }

    fun updateUserProfileUpdate(block: UserProfileUpdate.() -> UserProfileUpdate) {
        userProfileUpdateMock = userProfileUpdateMock.block()
    }


    fun uploadingDataMockUpdate(block: UploadingData.() -> UploadingData) {
        uploadingDataMock = uploadingDataMock.block()
    }
}
