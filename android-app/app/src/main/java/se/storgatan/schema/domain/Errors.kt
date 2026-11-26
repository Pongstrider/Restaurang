package se.storgatan.schema.domain

/** Fel som repositories kastar. Meddelandet visas direkt för användaren. */
sealed class AppException(message: String) : Exception(message)

class InvalidCredentialsException : AppException("Fel e-post eller lösenord.")

/** 401 – sessionen är ogiltig. Användaren skickas tillbaka till inloggningen. */
class UnauthorizedException : AppException("Du har loggats ut. Logga in igen.")

/** 409 – passet krockar med ett av mina pass eller har redan tagits av någon annan. */
class ShiftConflictException(
    message: String = "Passet gick inte att ta. Det krockar med ditt schema eller har redan tagits.",
) : AppException(message)

class NotFoundException(message: String = "Hittade inte passet. Det kan ha ändrats.") : AppException(message)

class NetworkException : AppException("Kunde inte nå servern. Kontrollera uppkopplingen och försök igen.")

class ServerException(code: Int) : AppException("Något gick fel på servern ($code). Försök igen om en stund.")

fun Throwable.userMessage(): String =
    (this as? AppException)?.message ?: "Något gick fel. Försök igen."
