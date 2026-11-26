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
import se.storgatan.schema.data.session.SessionState
import se.storgatan.schema.ui.screens.myshifts.MyShiftsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class MyShiftsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var session: InMemorySessionStore
    private lateinit var repo: FakeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        session = InMemorySessionStore()
        repo = FakeRepository(session, latencyMs = 0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loginAsErik() {
        repo.login("erik@storgatan.se", FakeData.PASSWORD)
    }

    @Test
    fun `efter inloggning visas Eriks pass`() = runTest {
        loginAsErik()
        val vm = MyShiftsViewModel(repo, repo)
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(4, state.shifts.size)
        assertNull(state.error)
    }

    @Test
    fun `utan inloggning visas felmeddelande`() = runTest {
        val vm = MyShiftsViewModel(repo, repo)
        assertTrue(vm.state.value.error != null)
    }

    @Test
    fun `utloggning tömmer sessionen`() = runTest {
        loginAsErik()
        val vm = MyShiftsViewModel(repo, repo)
        vm.logout()
        assertTrue(session.state.value is SessionState.LoggedOut)
    }
}
