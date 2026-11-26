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
import se.storgatan.schema.ui.screens.available.AvailableShiftsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class AvailableShiftsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val session = InMemorySessionStore()
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
    fun `lediga pass visar krock-flagga korrekt`() = runTest {
        loginAsErik()
        val vm = AvailableShiftsViewModel(repo)
        val items = vm.state.value.items
        // p-403 (ons 3 dec) krockar inte med Erik, p-402 (fre 4 dec) och p-401 (lör 5 dec) krockar
        val nonConflicting = items.filter { !it.conflictsWithMine }
        val conflicting = items.filter { it.conflictsWithMine }
        assertTrue(nonConflicting.any { it.shift.id == "p-403" })
        assertTrue(conflicting.any { it.shift.id == "p-402" })
        assertTrue(conflicting.any { it.shift.id == "p-401" })
    }

    @Test
    fun `claim ger claimedShiftId`() = runTest {
        loginAsErik()
        val vm = AvailableShiftsViewModel(repo)
        vm.claim("p-403")
        assertEquals("p-403", vm.state.value.claimedShiftId)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `claim med krock ger felmeddelande`() = runTest {
        loginAsErik()
        val vm = AvailableShiftsViewModel(repo)
        vm.claim("p-402")
        assertNull(vm.state.value.claimedShiftId)
        assertTrue(vm.state.value.error != null)
    }
}
