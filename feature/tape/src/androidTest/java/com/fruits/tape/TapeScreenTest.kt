package com.fruits.tape

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class TapeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Recommendations Tab Tests ---

    @Test
    fun recommendations_loadingState_showsProgress() {
        val state = TapeState(
            selectedTab = TapeTab.Recommendations,
            isLoading = true
        )

        setContent(state)

        composeTestRule.onNodeWithTag("recommendations_progress").assertIsDisplayed()
    }

    @Test
    fun recommendations_errorState_showsErrorAndRetry() {
        val state = TapeState(
            selectedTab = TapeTab.Recommendations,
            error = "Failed to load"
        )

        setContent(state)

        composeTestRule.onNodeWithTag("recommendations_error_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendations_retry_button").assertIsDisplayed()
    }

    @Test
    fun recommendations_emptyState_showsEmptyMessage() {
        val state = TapeState(
            selectedTab = TapeTab.Recommendations,
            isEmpty = true
        )

        setContent(state)

        composeTestRule.onNodeWithTag("recommendations_empty_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendations_retry_button_empty").assertIsDisplayed()
    }

    @Test
    fun recommendations_cardState_showsUserInformation() {
        val card = TapeCardItem(
            userId = "rec_1",
            name = "Alex",
            age = 28,
            city = "Berlin",
            imageUrl = "",
            reasonInFeed = listOf("Tech enthusiast"),
            about = "Kotlin developer"
        )
        val state = TapeState(
            selectedTab = TapeTab.Recommendations,
            currentCard = card
        )

        setContent(state)

        composeTestRule.onNodeWithTag("recommendations_swipe_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_user_name").assertTextContains("Alex, 28")
        composeTestRule.onNodeWithTag("card_user_city").assertTextContains("Berlin")
        composeTestRule.onNodeWithTag("btn_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_why").assertIsDisplayed()
    }

    @Test
    fun recommendations_whySheet_displaysReasons() {
        val reasons = listOf("Mutual friend", "Similar location")
        
        composeTestRule.setContent {
            TapeScreen(
                state = TapeState(selectedTab = TapeTab.Recommendations),
                onEvent = {},
                reasonSheet = reasons,
                aboutSheet = null,
                onDismissReasonSheet = {},
                onDismissAboutSheet = {}
            )
        }

        composeTestRule.onNodeWithTag("reason_bottom_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reason_sheet_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reason_item_0").assertTextContains("Mutual friend")
        composeTestRule.onNodeWithTag("reason_item_1").assertTextContains("Similar location")
    }

    // --- Liked Tab Tests ---

    @Test
    fun liked_tab_switching_works() {
        var selectedTab: TapeTab = TapeTab.Recommendations
        
        composeTestRule.setContent {
            TapeScreen(
                state = TapeState(selectedTab = TapeTab.Recommendations),
                onEvent = { event ->
                    if (event is TapeEvent.OnTabSelected) {
                        selectedTab = event.tab
                    }
                },
                reasonSheet = null,
                aboutSheet = null,
                onDismissReasonSheet = {},
                onDismissAboutSheet = {}
            )
        }

        composeTestRule.onNodeWithTag("tab_liked").performClick()
        assert(selectedTab == TapeTab.Liked)
    }

    @Test
    fun liked_loadingState_showsProgress() {
        val state = TapeState(
            selectedTab = TapeTab.Liked,
            likedIsLoading = true
        )

        setContent(state)

        composeTestRule.onNodeWithTag("liked_progress").assertIsDisplayed()
    }

    @Test
    fun liked_emptyState_showsEmptyMessage() {
        val state = TapeState(
            selectedTab = TapeTab.Liked,
            likedIsEmpty = true
        )

        setContent(state)

        composeTestRule.onNodeWithTag("liked_empty_title").assertIsDisplayed()
    }

    @Test
    fun liked_cardState_showsUserInformation_withoutWhyButton() {
        val card = TapeCardItem(
            userId = "liked_1",
            name = "Maria",
            age = 24,
            city = "Paris",
            imageUrl = "",
            reasonInFeed = emptyList(),
            about = "Artist"
        )
        val state = TapeState(
            selectedTab = TapeTab.Liked,
            likedCurrentCard = card
        )

        setContent(state)

        composeTestRule.onNodeWithTag("liked_swipe_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_user_name").assertTextContains("Maria, 24")
        composeTestRule.onNodeWithTag("btn_about").assertIsDisplayed()
        
        // Corner Case: Why button should NOT be present on Liked tab cards
        composeTestRule.onNodeWithTag("btn_why").assertDoesNotExist()
    }

    @Test
    fun aboutSheet_displaysContent() {
        composeTestRule.setContent {
            TapeScreen(
                state = TapeState(),
                onEvent = {},
                reasonSheet = null,
                aboutSheet = "This is a detailed bio about the user.",
                onDismissReasonSheet = {},
                onDismissAboutSheet = {}
            )
        }

        composeTestRule.onNodeWithTag("about_bottom_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_sheet_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_sheet_content").assertTextContains("This is a detailed bio about the user.")
    }

    private fun setContent(state: TapeState) {
        composeTestRule.setContent {
            TapeScreen(
                state = state,
                onEvent = {},
                reasonSheet = null,
                aboutSheet = null,
                onDismissReasonSheet = {},
                onDismissAboutSheet = {}
            )
        }
    }
}
