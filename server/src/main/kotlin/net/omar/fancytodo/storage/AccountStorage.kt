package net.omar.fancytodo.storage

import at.favre.lib.crypto.bcrypt.BCrypt
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.days
import kotlinx.serialization.builtins.ListSerializer
import net.omar.fancytodo.auth.Session
import net.omar.fancytodo.auth.UserAccount

private const val BCRYPT_COST = 12
private val SESSION_LENGTH = 30.days

private val accounts = ConcurrentHashMap<String, UserAccount>() // by id
private val sessions = ConcurrentHashMap<String, Session>() // by token hash
private val random = SecureRandom()

// Checked against when the email doesn't exist, so a failed login takes the same time either way.
private val dummyHash = BCrypt.withDefaults().hashToString(BCRYPT_COST, "not-a-real-password".toCharArray())

private fun accountsFile() = File(dataDir(), "accounts.json")
private fun sessionsFile() = File(dataDir(), "sessions.json")

fun loadAccounts() {
    readJson(accountsFile(), ListSerializer(UserAccount.serializer()))?.forEach { accounts[it.id] = it }
    readJson(sessionsFile(), ListSerializer(Session.serializer()))?.forEach { sessions[it.tokenHash] = it }
}

fun saveAccounts() {
    writeJson(accountsFile(), ListSerializer(UserAccount.serializer()), accounts.values.sortedBy { it.email })
    writeJson(sessionsFile(), ListSerializer(Session.serializer()), sessions.values.toList())
}

fun normalizeEmail(email: String) = email.trim().lowercase()

/** Creates an account, or returns null when the email is already taken. */
@Synchronized
fun createAccount(name: String, email: String, password: String): UserAccount? {
    val normalized = normalizeEmail(email)
    if (accounts.values.any { it.email == normalized }) return null
    val account = UserAccount(
        id = UUID.randomUUID().toString(),
        email = normalized,
        name = name.trim(),
        passwordHash = BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    )
    accounts[account.id] = account
    return account
}

fun accountCount() = accounts.size

fun accountById(id: String) = accounts[id]

/** The account matching these credentials, or null. */
fun checkCredentials(email: String, password: String): UserAccount? {
    val account = accounts.values.firstOrNull { it.email == normalizeEmail(email) }
    val matches = BCrypt.verifyer().verify(password.toCharArray(), account?.passwordHash ?: dummyHash).verified
    return account?.takeIf { matches }
}

/** Starts a session for [userId] and returns its token (only its hash is kept). */
fun createSession(userId: String): String {
    val token = ByteArray(32).also { random.nextBytes(it) }.let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
    sessions[hash(token)] = Session(hash(token), userId, System.currentTimeMillis() + SESSION_LENGTH.inWholeMilliseconds)
    return token
}

/** The user a token belongs to, or null if it's unknown or expired. */
fun userIdForToken(token: String): String? {
    val session = sessions[hash(token)] ?: return null
    if (session.expiresAtMillis < System.currentTimeMillis()) {
        sessions.remove(session.tokenHash)
        return null
    }
    return session.userId
}

fun deleteSession(token: String) {
    sessions.remove(hash(token))
}

private fun hash(token: String) =
    MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
