package com.example.mpazpaolr.data.remote

/** Token JWT de la sesión activa. Lo lee el interceptor de Retrofit en cada petición. */
object SesionToken {
    @Volatile
    var token: String? = null
}
