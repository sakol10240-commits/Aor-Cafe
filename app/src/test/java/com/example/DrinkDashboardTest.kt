package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.ui.DrinkDashboard
import com.example.viewmodel.DrinkViewModel

@RunWith(RobolectricTestRunner::class)
class DrinkDashboardTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testRenderAndInteractWithDashboard() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = DrinkViewModel(application)
        
        composeTestRule.setContent {
            DrinkDashboard(viewModel = viewModel)
        }
        
        composeTestRule.waitForIdle()
        
        // Add a drink
        composeTestRule.onNodeWithTag("tab_inventory").performClick()
        composeTestRule.onNodeWithTag("fab_add_action").performClick()
        
        // Fill form
        composeTestRule.onNodeWithTag("input_drink_name").performTextInput("Latte")
        composeTestRule.onNodeWithTag("input_drink_price").performTextInput("45")
        composeTestRule.onNodeWithTag("btn_save_drink").performClick()
        
        composeTestRule.waitForIdle()
        
        // Switch to Visits and click log
        composeTestRule.onNodeWithTag("tab_visits").performClick()
        composeTestRule.onNodeWithTag("fab_add_action").performClick()
        
        // Wait for modal
        composeTestRule.waitForIdle()
        
        // Select dropdown and pick drink
        // No need, first drink defaults.
        
        composeTestRule.onNodeWithTag("input_client_name").performTextInput("John Doe")
        
        // Save
        composeTestRule.onNodeWithTag("btn_save_visit").performClick()
        
        composeTestRule.waitForIdle()
    }
}
