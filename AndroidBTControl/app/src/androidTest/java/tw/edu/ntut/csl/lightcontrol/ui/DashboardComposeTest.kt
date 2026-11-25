package tw.edu.ntut.csl.lightcontrol.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun connectDemoHc06_triggerPowerOn_andInspectTerminal() {
        composeTestRule.onNodeWithTag("connection_status_banner").assertIsDisplayed()

        // Connect using the hardware-free Demo HC-06 banner button
        composeTestRule.onNodeWithTag("btn_connect_demo_banner").performClick()
        composeTestRule.waitForIdle()

        // Verify Power ON button becomes enabled and trigger it
        composeTestRule.onNodeWithTag("btn_power_on").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Terminal tab and verify log console renders
        composeTestRule.onNodeWithTag("nav_tab_terminal").performClick()
        composeTestRule.onNodeWithTag("terminal_log_list").assertIsDisplayed()
    }
}
