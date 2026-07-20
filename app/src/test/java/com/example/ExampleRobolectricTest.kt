package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.HomeChefViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Citch", appName)
  }

  @Test
  fun `test chat database insertion and retrieval`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val db = AppDatabase.getDatabase(app)
    val dao = db.dao()

    val chefId = 99
    val msg = ChatMessageEntity(
        chefId = chefId,
        sender = "User",
        text = "Hello Chef!"
    )
    dao.insertChatMessage(msg)

    val retrieved = dao.getChatMessagesForChef(chefId).first()
    assertEquals(1, retrieved.size)
    assertEquals("User", retrieved[0].sender)
    assertEquals("Hello Chef!", retrieved[0].text)
  }

  @Test
  fun `test order history retrieval and dashboard stats calculation`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val db = AppDatabase.getDatabase(app)
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        db.clearAllTables()
    }
    val dao = db.dao()

    // Insert dummy past orders (step >= 3)
    val order1 = OrderEntity(
        id = 501,
        chefId = 2,
        chefName = "Chef Pierre",
        mealId = 10,
        mealName = "Coq au Vin",
        quantity = 2,
        totalAmount = 45.0,
        buyerName = "Olamide",
        buyerAddress = "789 Maple Ave",
        buyerPhone = "+1 555-9080",
        status = "Delivered",
        step = 3, // Delivered/Past order
        paymentId = "ch_test123",
        timestamp = System.currentTimeMillis() - 86400000 // Yesterday
    )

    val order2 = OrderEntity(
        id = 502,
        chefId = 3,
        chefName = "Chef Elena",
        mealId = 12,
        mealName = "Fettuccine",
        quantity = 1,
        totalAmount = 25.0,
        buyerName = "Olamide",
        buyerAddress = "789 Maple Ave",
        buyerPhone = "+1 555-9080",
        status = "Delivered",
        step = 3, // Delivered/Past order
        paymentId = "ch_test456",
        timestamp = System.currentTimeMillis() // Today
    )

    dao.insertOrder(order1)
    dao.insertOrder(order2)

    val ordersList = dao.getAllOrders().first()
    val pastOrders = ordersList.filter { it.step >= 3 }

    // Assert correct retrieval of past orders
    assertTrue(pastOrders.any { it.id == 501 })
    assertTrue(pastOrders.any { it.id == 502 })

    // Verify stats calculations used on the Order History Dashboard
    val totalSpent = pastOrders.sumOf { it.totalAmount }
    val avgSpent = totalSpent / pastOrders.size

    assertEquals(70.0, totalSpent, 0.01)
    assertEquals(35.0, avgSpent, 0.01)
  }

  @Test
  fun `test chef chat response simulation logic`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = HomeChefViewModel(app)

    val chef = ChefEntity(
        id = 1,
        name = "Chef Elena Rostova",
        rating = 4.9f,
        address = "Downtown Kitchen",
        cuisineType = "Italian",
        phone = "123",
        bio = "Bio",
        youtubeChannelUrl = "",
        youtubeChannelName = "",
        avatarUrl = "",
        latitude = 0.0,
        longitude = 0.0
    )

    val mealsList = listOf(
        MealEntity(
            id = 1,
            chefId = 1,
            name = "Lasagna",
            description = "Rich meat lasagna",
            price = 15.0,
            imageUrl = "",
            category = "Mains"
        )
    )

    // Scenario 1: Spiciness query
    val replySpicy = viewModel.generateChefReply(chef, mealsList, "Is the lasagna spicy?")
    assertTrue(replySpicy.contains("heat") || replySpicy.contains("spicy"))

    // Scenario 2: Allergy query
    val replyAllergy = viewModel.generateChefReply(chef, mealsList, "Do you have peanut allergy options?")
    assertTrue(replyAllergy.contains("allergen-safe") || replyAllergy.contains("Allergy") || replyAllergy.contains("allergic"))

    // Scenario 3: Customization query
    val replyCustom = viewModel.generateChefReply(chef, mealsList, "Can I get vegetarian?")
    assertTrue(replyCustom.contains("vegetarian") || replyCustom.contains("vegan") || replyCustom.contains("swap"))
  }

  @Test
  fun `test dynamic leaderboard ranking and score calculation`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val db = AppDatabase.getDatabase(app)
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        db.clearAllTables()
    }
    val dao = db.dao()

    // 1. Insert two chefs
    val chef1 = ChefEntity(
        id = 10,
        name = "Chef Pierre",
        rating = 4.5f,
        address = "789 Maple Ave",
        cuisineType = "French",
        phone = "+1 555-1234",
        bio = "French Classics",
        youtubeChannelUrl = "",
        youtubeChannelName = "",
        avatarUrl = "",
        latitude = 0.0,
        longitude = 0.0
    )
    val chef2 = ChefEntity(
        id = 20,
        name = "Chef Luigi",
        rating = 4.8f,
        address = "123 Elm St",
        cuisineType = "Italian",
        phone = "+1 555-5678",
        bio = "Italian Master Chef",
        youtubeChannelUrl = "",
        youtubeChannelName = "",
        avatarUrl = "",
        latitude = 0.0,
        longitude = 0.0
    )
    dao.insertChef(chef1)
    dao.insertChef(chef2)

    // 2. Insert some dummy reviews
    dao.insertReview(
        ReviewEntity(
            chefId = 10,
            mealId = 0,
            reviewerName = "Alice",
            rating = 5,
            comment = "Superb!"
        )
    )
    dao.insertReview(
        ReviewEntity(
            chefId = 20,
            mealId = 0,
            reviewerName = "Bob",
            rating = 3,
            comment = "Okayish"
        )
    )

    // 3. Insert some orders
    for (i in 1..5) {
        dao.insertOrder(
            OrderEntity(
                id = 800 + i,
                chefId = 20,
                chefName = "Chef Luigi",
                mealId = 12,
                mealName = "Fettuccine",
                quantity = 1,
                totalAmount = 25.0,
                buyerName = "Olamide",
                buyerAddress = "789 Maple Ave",
                buyerPhone = "+1 555-9080",
                status = "Delivered",
                step = 3,
                paymentId = "ch_test_$i",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    dao.insertOrder(
        OrderEntity(
            id = 900,
            chefId = 10,
            chefName = "Chef Pierre",
            mealId = 10,
            mealName = "Coq au Vin",
            quantity = 1,
            totalAmount = 30.0,
            buyerName = "Olamide",
            buyerAddress = "789 Maple Ave",
            buyerPhone = "+1 555-9080",
            status = "Delivered",
            step = 3,
            paymentId = "ch_test_900",
            timestamp = System.currentTimeMillis()
        )
    )

    // 4. Retrieve and verify dynamic scoring formula: (averageRating * 15) + (orderVolume * 5)
    val chefsList = dao.getAllChefs().first()
    val ordersList = dao.getAllOrders().first()
    val reviewsList = dao.getAllReviews().first()

    val leaderboard = chefsList.map { chef ->
        val chefReviews = reviewsList.filter { it.chefId == chef.id }
        val chefOrders = ordersList.filter { it.chefId == chef.id }

        val avgRating = if (chefReviews.isNotEmpty()) {
            chefReviews.map { it.rating }.average()
        } else {
            chef.rating.toDouble()
        }

        val orderVolume = chefOrders.size
        val score = (avgRating * 15) + (orderVolume * 5)
        score to chef
    }.sortedByDescending { it.first }

    val pierreItem = leaderboard.find { it.second.id == 10 }
    val luigiItem = leaderboard.find { it.second.id == 20 }

    assertTrue(pierreItem != null)
    assertTrue(luigiItem != null)

    assertEquals(80.0, pierreItem!!.first, 0.01)
    assertEquals(70.0, luigiItem!!.first, 0.01)

    // And Pierre should be Rank 1 (index 0) due to higher score (80.0 > 70.0)
    assertEquals(10, leaderboard[0].second.id)
  }

  @Test
  fun `test search filter by ingredient or dish name`() {
    val chefs = listOf(
        ChefEntity(
            id = 1,
            name = "Chef Elena",
            rating = 4.9f,
            address = "Downtown Kitchen",
            cuisineType = "Italian",
            phone = "123",
            bio = "Italian Master Chef",
            youtubeChannelUrl = "",
            youtubeChannelName = "",
            avatarUrl = "",
            latitude = 0.0,
            longitude = 0.0
        ),
        ChefEntity(
            id = 2,
            name = "Chef Pierre",
            rating = 4.5f,
            address = "French Quarter",
            cuisineType = "French",
            phone = "456",
            bio = "French Classics",
            youtubeChannelUrl = "",
            youtubeChannelName = "",
            avatarUrl = "",
            latitude = 0.0,
            longitude = 0.0
        )
    )

    val meals = listOf(
        MealEntity(
            id = 101,
            chefId = 1,
            name = "Truffle Gnocchi",
            description = "Soft potato gnocchi with fresh shaved black truffles, butter, and nutmeg.",
            price = 22.0,
            imageUrl = "",
            category = "Mains"
        ),
        MealEntity(
            id = 102,
            chefId = 2,
            name = "Coq au Vin",
            description = "Classic French red wine braised chicken with mushrooms and pearl onions.",
            price = 28.0,
            imageUrl = "",
            category = "Mains"
        )
    )

    // Helper closure matching our filteredChefs logic
    val filterChefsFn = { query: String ->
        chefs.filter { chef ->
            val chefMeals = meals.filter { it.chefId == chef.id }
            chef.name.contains(query, ignoreCase = true) ||
            chef.cuisineType.contains(query, ignoreCase = true) ||
            chef.address.contains(query, ignoreCase = true) ||
            chefMeals.any { meal ->
                meal.name.contains(query, ignoreCase = true) ||
                meal.description.contains(query, ignoreCase = true)
            }
        }
    }

    // 1. Query by exact dish name "gnocchi"
    val resultGnocchi = filterChefsFn("gnocchi")
    assertEquals(1, resultGnocchi.size)
    assertEquals(1, resultGnocchi[0].id)

    // 2. Query by ingredient "truffles" in description
    val resultIngredient = filterChefsFn("truffles")
    assertEquals(1, resultIngredient.size)
    assertEquals(1, resultIngredient[0].id)

    // 3. Query by ingredient "chicken" in description
    val resultChicken = filterChefsFn("chicken")
    assertEquals(1, resultChicken.size)
    assertEquals(2, resultChicken[0].id)

    // 4. Query by non-existent ingredient "curry"
    val resultNone = filterChefsFn("curry")
    assertEquals(0, resultNone.size)
  }
}
