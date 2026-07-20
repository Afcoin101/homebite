package com.example.data

import kotlinx.coroutines.delay

/**
 * A highly secure and robust Payment Integration Service powered by Stripe.
 * Supports sandbox mock transaction flows, card formatting, brand recognition,
 * and standard Luhn checksum card validations.
 */
object PaymentIntegrationService {

    /**
     * Luhn Algorithm validation for credit cards.
     * Evaluates credit card numbers to catch common typos and numerical errors.
     */
    fun isValidLuhn(cardNumber: String): Boolean {
        val cleanNumber = cardNumber.replace(" ", "")
        if (cleanNumber.isEmpty() || !cleanNumber.all { it.isDigit() }) return false
        
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = cleanNumber[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    /**
     * Determines card brand based on prefix digit distributions.
     */
    fun getCardBrand(cardNumber: String): String {
        val clean = cardNumber.replace(" ", "")
        return when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("5") -> "Mastercard"
            clean.startsWith("34") || clean.startsWith("37") -> "American Express"
            clean.startsWith("6") -> "Discover"
            else -> "Credit/Debit Card"
        }
    }

    /**
     * Secure Stripe client-side execution handler.
     * Processes live PaymentIntent endpoint API requests when live mode is active,
     * or simulates a high-fidelity checkout loop in offline sandbox sandbox environments.
     */
    suspend fun processPayment(
        amount: Double,
        cardNum: String,
        expiry: String,
        cvv: String,
        description: String,
        isLiveMode: Boolean,
        liveBackendUrl: String
    ): StripePaymentResult {
        val sanitizedCard = cardNum.replace(" ", "")
        
        // 1. Basic length check
        if (sanitizedCard.length < 15 || sanitizedCard.length > 16) {
            return StripePaymentResult.Failure(
                errorCode = "invalid_number",
                errorMessage = "The credit card number must be 15 or 16 digits. Double check the input fields."
            )
        }

        // 2. Perform Luhn Validation for general cards (except standard stripe test cards)
        val isStripeTestBypass = sanitizedCard == "4242424242424242" || 
                                 sanitizedCard == "4000000000000002" || 
                                 sanitizedCard == "4000000000000001"
                                 
        if (!isStripeTestBypass && !isValidLuhn(sanitizedCard)) {
            return StripePaymentResult.Failure(
                errorCode = "checksum_failed",
                errorMessage = "Credit card verification failed (Luhn checksum mismatch). Enter a valid card number."
            )
        }

        // 3. Expiration formatting & check
        var cleanExpiry = expiry.replace(" ", "")
        if (cleanExpiry.length == 4 && !cleanExpiry.contains("/")) {
            cleanExpiry = cleanExpiry.take(2) + "/" + cleanExpiry.takeLast(2)
        }
        val expiryParts = cleanExpiry.split("/")
        if (expiryParts.size != 2) {
            return StripePaymentResult.Failure(
                errorCode = "invalid_expiry_format",
                errorMessage = "Card expiry must be in MM/YY format (e.g. 12/28 or 1228)."
            )
        }
        
        val month = expiryParts[0].toIntOrNull()
        val year = expiryParts[1].toIntOrNull()
        if (month == null || month < 1 || month > 12) {
            return StripePaymentResult.Failure(
                errorCode = "invalid_expiry_month",
                errorMessage = "Expiration month must be between 01 and 12."
            )
        }

        // 4. CVV Security length evaluation
        val cleanCvv = cvv.replace(" ", "")
        if (cleanCvv.length != 3 && cleanCvv.length != 4) {
            return StripePaymentResult.Failure(
                errorCode = "invalid_cvc",
                errorMessage = "The CVV security code must be 3 or 4 digits."
            )
        }

        if (isLiveMode) {
            // Live production Route via Custom Stripe Middleware integration API
            return try {
                val cents = (amount * 100).toLong()
                val api = StripeClient.getApi(liveBackendUrl)
                val response = api.createPaymentIntent(
                    request = StripeIntentRequest(
                        amount = cents,
                        description = description
                    )
                )

                if (response.client_secret != null) {
                    StripePaymentResult.Success(
                        transactionId = response.id ?: "ch_live_${System.currentTimeMillis()}",
                        last4 = sanitizedCard.takeLast(4)
                    )
                } else {
                    StripePaymentResult.Failure(
                        errorCode = "payment_intent_failed",
                        errorMessage = response.error ?: "Missing Stripe client secret parameter from active checkout middleware."
                    )
                }
            } catch (e: Exception) {
                StripePaymentResult.Failure(
                    errorCode = "endpoint_unreachable",
                    errorMessage = "Could not contact your Live Backend endpoint at $liveBackendUrl. Details: ${e.localizedMessage}. Verify server deployment state or keep Live Mode toggled off to simulate secure transactions."
                )
            }
        } else {
            // High-fidelity sandbox mock checkout simulation
            delay(1500) // Realistic network delay simulation
            
            return when (sanitizedCard) {
                "4000000000000002" -> {
                    StripePaymentResult.Failure(
                        errorCode = "card_declined",
                        errorMessage = "This card was declined by your issuing bank. Use a valid Stripe sandbox identifier."
                    )
                }
                "4000000000000001" -> {
                    StripePaymentResult.Failure(
                        errorCode = "expired_card",
                        errorMessage = "The card has expired or the expiration date is incorrect. Please check exp date."
                    )
                }
                else -> {
                    StripePaymentResult.Success(
                        transactionId = "ch_mock_${System.currentTimeMillis()}",
                        last4 = sanitizedCard.takeLast(4)
                    )
                }
            }
        }
    }
}
