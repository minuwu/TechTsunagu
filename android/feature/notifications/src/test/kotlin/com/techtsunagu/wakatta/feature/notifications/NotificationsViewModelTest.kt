package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.common.LanguageManagerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val registrar = FakeDeviceRegistrar()
    private val repository = FakeNotificationsRepository()
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = NotificationsViewModel(repository, registrar, LanguageManagerImpl("en"))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun listShowsGenericTextsOnly() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("New message from Tanaka", "New message from Rahim"), state.items.map { it.text })
    }

    @Test
    fun disabledSystemNotificationsAsksThenRemembersNotNow() {
        viewModel.onEvent(NotificationsEvent.SystemStatus(enabled = false))
        assertEquals(PermissionPrompt.Ask, viewModel.state.value.permission)
        viewModel.onEvent(NotificationsEvent.NotNow)
        viewModel.onEvent(NotificationsEvent.SystemStatus(enabled = false))
        assertEquals(PermissionPrompt.Denied, viewModel.state.value.permission)
        viewModel.onEvent(NotificationsEvent.SystemStatus(enabled = true))
        assertEquals(PermissionPrompt.Hidden, viewModel.state.value.permission)
    }

    @Test
    fun permissionResultRegistersTheDevice() {
        viewModel.onEvent(NotificationsEvent.PermissionResult(granted = true))
        assertEquals(true, registrar.lastEnabled)
        assertEquals(PermissionPrompt.Hidden, viewModel.state.value.permission)
        viewModel.onEvent(NotificationsEvent.PermissionResult(granted = false))
        assertEquals(false, registrar.lastEnabled)
        assertEquals(PermissionPrompt.Denied, viewModel.state.value.permission)
    }

    @Test
    fun markAsReadUpdatesState() {
        assertEquals(true, viewModel.state.value.items.first().isUnread)
        viewModel.onEvent(NotificationsEvent.MarkAsRead("n1"))
        assertEquals(false, viewModel.state.value.items.first().isUnread)
    }
}
