package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeChefRepository(private val dao: HomeChefDao) {
    val chefs: Flow<List<ChefEntity>> = dao.getAllChefs()
    val meals: Flow<List<MealEntity>> = dao.getAllMeals()
    val orders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val reviews: Flow<List<ReviewEntity>> = dao.getAllReviews()
    val alerts: Flow<List<AlertEntity>> = dao.getAllAlerts()

    fun getChef(id: Int): Flow<ChefEntity?> = dao.getChefById(id)
    fun getMealsForChef(chefId: Int): Flow<List<MealEntity>> = dao.getMealsByChef(chefId)
    fun getReviewsForChef(chefId: Int): Flow<List<ReviewEntity>> = dao.getReviewsForChef(chefId)
    fun getOrder(id: Int): Flow<OrderEntity?> = dao.getOrderById(id)

    suspend fun addChef(chef: ChefEntity): Int {
        return dao.insertChef(chef).toInt()
    }

    suspend fun addMeal(meal: MealEntity): Int {
        return dao.insertMeal(meal).toInt()
    }

    suspend fun addReview(review: ReviewEntity, isLiveMode: Boolean = false, backendUrl: String = "") {
        if (isLiveMode && backendUrl.isNotEmpty()) {
            try {
                val api = StripeClient.getApi(backendUrl)
                api.submitReview(review)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dao.insertReview(review)
        
        // Also add local alert when a new review is added to build trust
        dao.insertAlert(
            AlertEntity(
                title = "New Review Added ⭐",
                message = "${review.reviewerName} rated chef/meal and left a feedback: \"${review.comment}\""
            )
        )
    }

    suspend fun addAlert(alert: AlertEntity) {
        dao.insertAlert(alert)
    }

    suspend fun markAlertsAsRead() {
        dao.markAllAlertsAsRead()
    }

    fun getChatMessagesForChef(chefId: Int): Flow<List<ChatMessageEntity>> {
        return dao.getChatMessagesForChef(chefId)
    }

    suspend fun sendChatMessage(chefId: Int, sender: String, text: String) {
        dao.insertChatMessage(
            ChatMessageEntity(
                chefId = chefId,
                sender = sender,
                text = text
            )
        )
    }

    // Trigger secure payment simulation and start order tracking lifecycle
    suspend fun placeOrder(
        meal: MealEntity,
        chefName: String,
        quantity: Int,
        buyerName: String,
        buyerAddress: String,
        buyerPhone: String,
        scope: CoroutineScope
    ): Int {
        val amount = meal.price * quantity
        val paymentId = "CHEF-PAY-${System.currentTimeMillis().toString().takeLast(6)}"
        
        val order = OrderEntity(
            mealId = meal.id,
            mealName = meal.name,
            chefId = meal.chefId,
            chefName = chefName,
            quantity = quantity,
            totalAmount = amount,
            buyerName = buyerName,
            buyerAddress = buyerAddress,
            buyerPhone = buyerPhone,
            status = "Pending",
            step = 0,
            paymentId = paymentId
        )

        val orderId = dao.insertOrder(order).toInt()

        // Create initial notification alert
        dao.insertAlert(
            AlertEntity(
                title = "Order Cashier Paid Securely ✓",
                message = "Your payment of $${String.format("%.2f", amount)} was processed securely via $paymentId. Order #${orderId} is now pending chef confirmation."
            )
        )

        // Simulate real-time order tracking steps asynchronously in a coroutine
        scope.launch(Dispatchers.IO) {
            simulateOrderLifeCycle(orderId, meal.name, chefName)
        }

        return orderId
    }

    private suspend fun simulateOrderLifeCycle(orderId: Int, mealName: String, chefName: String) {
        // Step 1: Preparing (after 10 seconds)
        delay(10_000)
        updateOrderStatus(orderId, "Preparing", 1, "Kitchen Preparing 🍳", "$chefName is now master-crafting your fresh $mealName.")

        // Step 2: Out for Delivery (after 12 seconds)
        delay(12_000)
        updateOrderStatus(orderId, "Out for Delivery", 2, "Out for Delivery 🚴", "Special local courier picked up your food and is on the way!")

        // Step 3: Delivered (after 12 seconds)
        delay(12_000)
        updateOrderStatus(orderId, "Delivered", 3, "Arrived & Served 🎉", "Order #${orderId} of $mealName has arrived safely. Bon appétit!")
    }

    private suspend fun updateOrderStatus(
        orderId: Int,
        status: String,
        step: Int,
        alertTitle: String,
        alertMsg: String
    ) {
        // Fetch current order state
        val order = dao.getOrderById(orderId).first()
        if (order != null) {
            val updated = order.copy(status = status, step = step)
            dao.updateOrder(updated)
            // Save notification alert
            dao.insertAlert(
                AlertEntity(
                    title = alertTitle,
                    message = alertMsg
                )
            )
        }
    }

    suspend fun syncWithBackend(backendUrl: String) {
        val api = StripeClient.getApi(backendUrl)
        try {
            val chefsList = api.getChefs()
            val mealsList = api.getMeals()
            val reviewsList = api.getReviews()
            for (chef in chefsList) {
                dao.insertChef(chef)
            }
            for (meal in mealsList) {
                dao.insertMeal(meal)
            }
            for (review in reviewsList) {
                dao.insertReview(review)
            }
            dao.insertAlert(
                AlertEntity(
                    title = "Database Sync Succeeded ✓",
                    message = "Successfully synced ${chefsList.size} chefs, ${mealsList.size} meals, and ${reviewsList.size} reviews from production backend API."
                )
            )
        } catch (e: Exception) {
            val errorMsg = e.message ?: ""
            val friendlyMsg = if (errorMsg.contains("Use JsonReader.setLenient") || errorMsg.contains("malformed") || errorMsg.contains("Expected BEGIN_ARRAY") || errorMsg.contains("Expected BEGIN_OBJECT")) {
                "The server returned an HTML error page or unexpected plain-text instead of valid JSON. This usually means the server is down or the backend URL is incorrect (currently: $backendUrl)."
            } else {
                e.localizedMessage ?: "Unknown network error"
            }
            dao.insertAlert(
                AlertEntity(
                    title = "Database Sync Failed ❌",
                    message = "Failed to sync: $friendlyMsg"
                )
            )
            throw Exception(friendlyMsg, e)
        }
    }
}
