package se.storgatan.schema.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import se.storgatan.schema.data.fake.FakeData
import se.storgatan.schema.data.fake.FakeRepository
import se.storgatan.schema.data.session.InMemorySessionStore
import se.storgatan.schema.data.session.SessionState
import se.storgatan.schema.domain.InvalidCredentialsException
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.ShiftConflictException
import se.storgatan.schema.domain.ShiftStatus
import se.storgatan.schema.domain.UnauthorizedException
import java.time.Clock
import java.time.ZoneOffset

class FakeRepositoryTest {

    private lateinit var session: InMemorySessionStore
    private lateinit var repo: FakeRepository

    @Before
    fun setUp() {
        session = InMemorySessionStore()
        repo = FakeRepository(session, Clock.fixed(FakeData.at(26, 11, 12, 0), ZoneOffset.UTC))
    }

    private suspend fun loginAsErik() = repo.login("erik@storgatan.se", FakeData.PASSWORD)

    private suspend inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
        try {
            block()
            fail("Förväntade ${T::class.simpleName}")
        } catch (e: Throwable) {
            if (e !is T) throw e
        }
    }

    @Test
    fun `inloggning sparar sessionen och fel lösenord ger fel`() = runTest {
        assertThrows<InvalidCredentialsException> { repo.login("erik@storgatan.se", "fel") }
        val erik = repo.login("  Erik@Storgatan.se ", FakeData.PASSWORD)
        assertEquals("Erik", erik.name)
        assertTrue(session.state.value is SessionState.LoggedIn)
    }

    @Test
    fun `utan inloggning blir det 401`() = runTest {
        assertThrows<UnauthorizedException> { repo.myShifts() }
    }

    @Test
    fun `Eriks pass är fyra och sorterade med kollegor`() = runTest {
        loginAsErik()
        val shifts = repo.myShifts()
        assertEquals(listOf("p-101", "p-102", "p-103", "p-104"), shifts.map { it.id })
        assertEquals(setOf("Maja", "Omar", "Elin", "Johan"), shifts.first().coworkers.map { it.name }.toSet())
    }

    @Test
    fun `lediga pass visar utlagda till alla och till mig men inte mina egna`() = runTest {
        loginAsErik()
        assertEquals(listOf("p-403", "p-402", "p-401", "p-404"), repo.releasedShifts().map { it.id })
    }

    @Test
    fun `lägga ut och ångra syns direkt i mina pass`() = runTest {
        loginAsErik()
        val before = repo.changes.value
        repo.release("p-101", "  Sjuk  ", ReleaseTarget.AllSameRole)
        val released = repo.myShifts().first { it.id == "p-101" }
        assertEquals(ShiftStatus.RELEASED, released.status)
        assertEquals("Sjuk", released.releaseReason)
        assertEquals(before + 1, repo.changes.value)

        repo.unrelease("p-101")
        val back = repo.myShifts().first { it.id == "p-101" }
        assertEquals(ShiftStatus.ASSIGNED, back.status)
        assertNull(back.releaseReason)
    }

    @Test
    fun `ta ett pass som inte krockar flyttar det till mig`() = runTest {
        loginAsErik()
        val taken = repo.claim("p-403")
        assertEquals(FakeData.erik.id, taken.employeeId)
        assertEquals(ShiftStatus.SWAPPED, taken.status)
        assertTrue(repo.myShifts().any { it.id == "p-403" })
        assertFalse(repo.releasedShifts().any { it.id == "p-403" })
    }

    @Test
    fun `ta ett pass som krockar ger 409`() = runTest {
        loginAsErik()
        assertThrows<ShiftConflictException> { repo.claim("p-402") }
        assertTrue(repo.releasedShifts().any { it.id == "p-402" })
    }

    @Test
    fun `samma pass kan inte tas två gånger`() = runTest {
        loginAsErik()
        repo.claim("p-403")
        repo.logout()
        repo.login("lina@storgatan.se", FakeData.PASSWORD)
        assertThrows<ShiftConflictException> { repo.claim("p-403") }
    }

    @Test
    fun `acceptera bytesförfrågan ger mig passet och tar bort förfrågan`() = runTest {
        loginAsErik()
        val swap = repo.incomingSwaps().single()
        assertEquals("Maja", swap.from.name)
        repo.acceptSwap(swap.id)
        assertTrue(repo.myShifts().any { it.id == "p-404" })
        assertTrue(repo.incomingSwaps().isEmpty())
    }

    @Test
    fun `avböja bytesförfrågan lämnar passet hos kollegan`() = runTest {
        loginAsErik()
        repo.declineSwap(repo.incomingSwaps().single().id)
        assertFalse(repo.myShifts().any { it.id == "p-404" })
        assertTrue(repo.incomingSwaps().isEmpty())
        assertFalse(repo.releasedShifts().any { it.id == "p-404" })
    }

    @Test
    fun `utlagt till en viss kollega blir bytesförfrågan hos henne`() = runTest {
        loginAsErik()
        repo.release("p-103", null, ReleaseTarget.Colleague(FakeData.maja.id))
        repo.logout()
        repo.login("maja@storgatan.se", FakeData.PASSWORD)
        assertEquals("p-103", repo.incomingSwaps().single().shift.id)
        assertTrue(repo.notifications().any { it.swapId == FakeData.swapIdFor("p-103") && !it.read })
    }

    @Test
    fun `olästa notiser räknas och kan markeras som lästa`() = runTest {
        loginAsErik()
        assertEquals(2, repo.unreadCount.value)
        val unread = repo.notifications().filter { !it.read }.map { it.id }
        repo.markNotificationsRead(unread)
        assertEquals(0, repo.unreadCount.value)
    }
}
