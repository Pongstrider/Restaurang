package se.storgatan.schema.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import se.storgatan.schema.data.session.SessionStore

class AuthInterceptor(private val session: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = session.token?.let { token ->
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } ?: chain.request()
        return chain.proceed(request)
    }
}
