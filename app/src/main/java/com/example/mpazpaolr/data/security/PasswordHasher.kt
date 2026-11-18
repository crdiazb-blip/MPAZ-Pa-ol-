package com.example.mpazpaolr.data.security

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hash con sal (SHA-256) para guardar localmente la contraseña de quien ya inició sesión,
 * de modo que pueda volver a entrar sin conexión. Nunca se guarda la contraseña en texto plano.
 */
object PasswordHasher {

    private const val LARGO_SAL = 16

    fun nuevaSal(): String {
        val bytes = ByteArray(LARGO_SAL)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, sal: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest((sal + password).toByteArray(Charsets.UTF_8)).toHex()
    }

    fun coincide(password: String, sal: String, hashEsperado: String): Boolean =
        MessageDigest.isEqual(hash(password, sal).toByteArray(), hashEsperado.toByteArray())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
