package com.fruits.tape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TapeViewModel : ViewModel() {

    private val _state = MutableStateFlow(TapeState())
    val state: StateFlow<TapeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TapeEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TapeEffect> = _effects.asSharedFlow()

    init {
        loadNextCard()
    }

    fun onEvent(event: TapeEvent) {
        when (event) {
            TapeEvent.OnWhyClicked -> {
                _state.value.currentCard?.reasonInFeed?.let { reason ->
                    viewModelScope.launch {
                        _effects.emit(TapeEffect.ShowReasonSheet(reason))
                    }
                }
            }
            TapeEvent.OnAboutClicked -> {
                _state.value.currentCard?.about?.let { about ->
                    viewModelScope.launch {
                        _effects.emit(TapeEffect.ShowAboutSheet(about))
                    }
                }
            }
            TapeEvent.OnCardSwiped -> loadNextCard()
        }
    }

    private fun loadNextCard() {
        _state.update {
            it.copy(
                currentCard = nextMockCard(),
                isLoading = false,
            )
        }
    }
    private fun nextMockCard(): TapeCardItem {
        val index = (mockCards.indices).random()
        return mockCards[index]
    }

    companion object {
        private val mockCards = listOf(
            TapeCardItem(
                id = "1",
                name = "Анна",
                imageUrl = "https://img.freepik.com/free-vector/man-with-hand-up-icon_24877-81630.jpg?semt=ais_hybrid&w=740&q=80",
                reasonInFeed = "У вас общие интересы: путешествия и фотография. Алгоритм подобрал профиль по активностям в сообществах.",
                about = "Фотограф, люблю горы и море. Ищу единомышленников для походов и съёмок.",
            ),
            TapeCardItem(
                id = "2",
                name = "Максим",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTM-S20pRXPbYWIiIdjJXAVEjYmNgTcNy_CFA&s",
                reasonInFeed = "Находится в одном городе и в похожей возрастной группе. Часто лайкают похожий контент.",
                about = "Разработчик, гитара, настолки. Обожаю кофе и долгие разговоры.",
            ),
            TapeCardItem(
                id = "3",
                name = "София",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQctqAn2xkZlqfKyzEidI6yiOiuzv-krkHYEA&s",
                reasonInFeed = "Подписана на те же мероприятия, что и вы. Сервис учитывает пересечение интересов.",
                about = "Дизайн, искусство, йога. Верю в совпадения и честность.",
            ),
            TapeCardItem(
                id = "4",
                name = "Дмитрий",
                imageUrl = "https://profil.adu.by/pluginfile.php/4384/mod_book/chapter/12298/52.6.jpg",
                reasonInFeed = "Новый пользователь в вашем регионе. Показываем для разнообразия ленты.",
                about = "Предприниматель. Спорт, книги, путешествия. Открыт к новым знакомствам.",
            ),
            TapeCardItem(
                id = "5",
                name = "Елена",
                imageUrl = "https://cdn-icons-png.flaticon.com/512/3048/3048122.png",
                reasonInFeed = "Общие друзья и пересечение по хобби (музыка, кино). Высокий рейтинг совместимости.",
                about = "Психолог, музыкант-любитель. Ценю искренность и чувство юмора.",
            ),
        )
    }
}
