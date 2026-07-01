package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Request model for backend API creating a Stripe PaymentIntent
data class StripeIntentRequest(
    val amount: Long,          // Amount in cents (e.g., $15.50 -> 1550)
    val currency: String = "usd",
    val description: String,
    val customerEmail: String? = null
)

// Response model containing Stripe PaymentIntent variables
data class StripeIntentResponse(
    val id: String?,
    val client_secret: String?,
    val status: String?,
    val error: String? = null
)

// Direct Stripe API model (if communicating with Stripe endpoint)
data class StripeDirectConfirmRequest(
    val payment_method_data: StripePaymentMethodData
)

data class StripePaymentMethodData(
    val type: String = "card",
    val card: StripeCardData
)

data class StripeCardData(
    val number: String,
    val exp_month: Int,
    val exp_year: Int,
    val cvc: String
)

interface StripeBackendApi {
    // Fetch chefs from custom backend
    @GET("chefs")
    suspend fun getChefs(): List<ChefEntity>

    // Fetch meals from custom backend
    @GET("meals")
    suspend fun getMeals(): List<MealEntity>

    // Create a PaymentIntent via a Custom Backend Server (e.g. hosted Node.js / Spring Boot)
    @POST("payment-intents")
    suspend fun createPaymentIntent(
        @Body request: StripeIntentRequest
    ): StripeIntentResponse

    // Directly confirm with Stripe (if client secret is known or communicating directly with client billing proxies)
    @POST
    suspend fun confirmPaymentIntentDirectly(
        @Url url: String,
        @Header("Authorization") apiKeyHeader: String,
        @Body request: StripeDirectConfirmRequest
    ): StripeIntentResponse
}

sealed class StripePaymentResult {
    data class Success(val transactionId: String, val last4: String) : StripePaymentResult()
    data class Failure(val errorCode: String, val errorMessage: String) : StripePaymentResult()
}

object StripeClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Dynamically retrieve API client based on provided backend URL
    fun getApi(baseUrl: String): StripeBackendApi {
        // Enforce valid HTTP schema for Retrofit
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(StripeBackendApi::class.java)
    }

    // Direct Stripe endpoint URL for directly processing payment intents if backend secret matches
    const val STRIPE_CONFIRM_API = "https://api.stripe.com/v1/payment_intents"
}
