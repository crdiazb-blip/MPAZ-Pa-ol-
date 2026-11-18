package com.example.mpazpaolr.data.remote

import com.example.mpazpaolr.BuildConfig
import com.example.mpazpaolr.data.remote.api.AuthApi
import com.example.mpazpaolr.data.remote.api.BitacoraApi
import com.example.mpazpaolr.data.remote.api.EquipoApi
import com.example.mpazpaolr.data.remote.api.NotificacionApi
import com.example.mpazpaolr.data.remote.api.SolicitudApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Cliente único de Retrofit: todas las peticiones pasan por el api-gateway. */
object RetrofitClient {

    private val autenticacion = Interceptor { chain ->
        val original = chain.request()
        val token = SesionToken.token
        val peticion = if (token != null && !original.url.encodedPath.endsWith("/auth/login")) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        chain.proceed(peticion)
    }

    private val http: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(autenticacion)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val equipoApi: EquipoApi by lazy { retrofit.create(EquipoApi::class.java) }
    val solicitudApi: SolicitudApi by lazy { retrofit.create(SolicitudApi::class.java) }
    val bitacoraApi: BitacoraApi by lazy { retrofit.create(BitacoraApi::class.java) }
    val notificacionApi: NotificacionApi by lazy { retrofit.create(NotificacionApi::class.java) }
}
