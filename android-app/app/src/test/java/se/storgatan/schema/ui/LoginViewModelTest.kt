package se.storgatan.schema.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import se.storgatan.schema.data.fake.FakeData
import se.storgatan.schema.data.fake.FakeRepository
import se.storgatan.schema.data.session.InMemorySessionStore
import se.storgatan.schema.ui.screens.login.LoginViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var vm: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val session = InMemorySessionStore()
        val repo = FakeRepository(session, latencyMs = 0)
        vm = LoginViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tomma fält ger felmeddelande`() = runTest {
        vm.login()
        assertEquals("Fyll i både e-post och lösenord.", vm.state.value.error)
        assertFalse(vm.state.value.loginSuccess)
    }

    @Test
    fun `fel lösenord ger felmeddelande`() = runTest {
        vm.onEmailChange("erik@storgatan.se")
        vm.onPasswordChange("fel")
        vm.login()
        assertEquals("Fel e-post eller lösenord.", vm.state.value.error)
        assertFalse(vm.state.value.loginSuccess)
    }

    @Test
    fun `rätt uppgifter ger loginSuccess`() = runTest {
        vm.onEmailChange("erik@storgatan.se")
        vm.onPasswordChange(FakeData.PASSWORD)
        vm.login()
        assertTrue(vm.state.value.loginSuccess)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `ändra email rensar felmeddelande`() = runTest {
        vm.login()
        assertTrue(vm.state.value.error != null)
        vm.onEmailChange("test")
        assertNull(vm.state.value.error)
    }
}
