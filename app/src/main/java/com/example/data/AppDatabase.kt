package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface HomeChefDao {
    @Query("SELECT * FROM chefs")
    fun getAllChefs(): Flow<List<ChefEntity>>

    @Query("SELECT * FROM chefs WHERE id = :chefId")
    fun getChefById(chefId: Int): Flow<ChefEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChef(chef: ChefEntity): Long

    @Query("SELECT * FROM meals")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE chefId = :chefId")
    fun getMealsByChef(chefId: Int): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Int): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM reviews WHERE chefId = :chefId ORDER BY timestamp DESC")
    fun getReviewsForChef(chefId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()
}

@Database(
    entities = [
        ChefEntity::class,
        MealEntity::class,
        OrderEntity::class,
        ReviewEntity::class,
        AlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): HomeChefDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dkitchen_database_v3"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Populate database on creation in IO thread
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.dao())
                }
            }
        }

        private suspend fun populateInitialData(dao: HomeChefDao) {
            // Pre-populate 4 different professional Home Chefs with realistic details and geographic offsets
            val chef1Id = dao.insertChef(
                ChefEntity(
                    id = 1,
                    name = "Chef Elena Rostova",
                    rating = 4.9f,
                    address = "Downtown Kitchen - 124 Pine St",
                    cuisineType = "Gourmet Italian & Pastas",
                    phone = "+1 (555) 349-2091",
                    bio = "Elena studied culinary arts in Florence and specializes in slow-baked organic lasagnas and fresh hand-rolled truffle pastas using locally sourced ingredients.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk",
                    youtubeChannelName = "Elena's Italian Classics",
                    avatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                    latitude = 37.7812,
                    longitude = -122.4111,
                    followersCount = 284
                )
            ).toInt()

            val chef2Id = dao.insertChef(
                ChefEntity(
                    id = 2,
                    name = "Chef Kenji Sato",
                    rating = 4.8f,
                    address = "Soma Culinary Loft - 650 Brannan St",
                    cuisineType = "Artisanal Ramen & Sushi",
                    phone = "+1 (555) 980-1283",
                    bio = "Tokyo-trained Ramen professional passionate about delivering authentic rich pork tonkotsu and fresh tori paitan to our local neighborhood.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=P_mG69_PshQ",
                    youtubeChannelName = "Kenji's Ramen Craft",
                    avatarUrl = "https://images.unsplash.com/photo-1581092921461-eab62e97a780?w=150",
                    latitude = 37.7712,
                    longitude = -122.4015,
                    followersCount = 390
                )
            ).toInt()

            val chef3Id = dao.insertChef(
                ChefEntity(
                    id = 3,
                    name = "Chef Maria Hernandez",
                    rating = 4.7f,
                    address = "Castro Cozy Kitchens - 4100 18th St",
                    cuisineType = "Authentic Mexican Street Food",
                    phone = "+1 (555) 761-0922",
                    bio = "Maria's cooking carries secrets from five generations of family Oaxacan recipes. Famous for her rich multi-day slow simmered mole sauce.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0",
                    youtubeChannelName = "Maria's Mole Secrets",
                    avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
                    latitude = 37.7612,
                    longitude = -122.4350,
                    followersCount = 195
                )
            ).toInt()

            val chef4Id = dao.insertChef(
                ChefEntity(
                    id = 4,
                    name = "Chef Dev Patel",
                    rating = 4.95f,
                    address = "Mission Spiced Kitchen - 2100 Valencia St",
                    cuisineType = "Authentic Indian Curries",
                    phone = "+1 (555) 438-9901",
                    bio = "Dev translates Indian heritage classics into vibrant food experiences. Specialize in freshly baked garlic naan and authentic butter chicken.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0",
                    youtubeChannelName = "Saffron & Spice with Dev",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    latitude = 37.7580,
                    longitude = -122.4220,
                    followersCount = 512
                )
            ).toInt()

            val chef5Id = dao.insertChef(
                ChefEntity(
                    id = 5,
                    name = "Chef Chinelo Obi",
                    rating = 4.95f,
                    address = "Fillmore African Oasis - 1520 Eddy St",
                    cuisineType = "Authentic Nigerian & West African",
                    phone = "+1 (555) 512-8924",
                    bio = "Chinelo brings the vibrant flavors of Lagos and Abuja to SF. Famous for her smokey Party Jollof Rice, perfectly seasoned Suya, and rich leafy Egusi soup.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk",
                    youtubeChannelName = "Chinelo's Kitchen Secrets",
                    avatarUrl = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=150",
                    latitude = 37.7794,
                    longitude = -122.4294,
                    followersCount = 310
                )
            ).toInt()

            // Pre-populate Meals
            dao.insertMeal(
                MealEntity(
                    id = 1,
                    chefId = chef1Id,
                    name = "Rustic Lasagna Bolognese",
                    description = "Freshly rolled pasta sheets layered with dynamic 12-hour slow-cooked grass-fed beef ragu, rich bechamel, and melted bufala mozzarella.",
                    price = 16.50,
                    imageUrl = "https://images.unsplash.com/photo-1574894709920-11b28e7367e3?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 2,
                    chefId = chef1Id,
                    name = "Truffle Mushroom Fettuccine",
                    description = "Housemade egg fettuccine tossed in white truffle oil, caramelized wild chanterelle mushrooms, parmigiano reggiano, and fresh flat parsley.",
                    price = 18.00,
                    imageUrl = "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 3,
                    chefId = chef1Id,
                    name = "Classic Italian Tiramisu",
                    description = "Espresso-soaked savoiardi biscuits layered with fluffy raw egg-free sweet mascarpone whip and thoroughly dusted with fine dark cocoa powder.",
                    price = 8.50,
                    imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=300",
                    category = "Desserts",
                    isAvailable = true
                )
            )

            dao.insertMeal(
                MealEntity(
                    id = 4,
                    chefId = chef2Id,
                    name = "Black Garlic Tonkotsu Ramen",
                    description = "Creamy 24-hour pork bone broth, soft wheat noodles, tender aburi pork chashu, soft-boiled ajitama marinaded egg, wood-ear mushrooms, scallions, and black garlic oil.",
                    price = 17.50,
                    imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 5,
                    chefId = chef2Id,
                    name = "Premium Dragon Salmon Roll",
                    description = "Glazed eel and fresh cucumber inside, wrapped elegantly with buttery thin-sliced avocado, spicy salmon, unagi eel sauce, and flying fish roe.",
                    price = 15.00,
                    imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=300",
                    category = "Starters",
                    isAvailable = true
                )
            )

            dao.insertMeal(
                MealEntity(
                    id = 6,
                    chefId = chef3Id,
                    name = "Oaxacan Mole Negro Chicken Tacos",
                    description = "Handmade corn tortillas stuffed with juicy shredded chicken smothered in rich, complex Oaxacan Mole Negro. Topped with crumbled cotija cheese, pickled red onions, and fresh cilantro.",
                    price = 14.00,
                    imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 7,
                    chefId = chef3Id,
                    name = "Tres Leches Caramel Cake",
                    description = "Fluffy vanilla sponge cake soaked perfectly in three milk nectars, iced with fresh cream, and finished with ribbons of dulce de leche.",
                    price = 7.50,
                    imageUrl = "https://images.unsplash.com/photo-1586788680438-ac4e8705305a?w=300",
                    category = "Desserts",
                    isAvailable = true
                )
            )

            dao.insertMeal(
                MealEntity(
                    id = 8,
                    chefId = chef4Id,
                    name = "Slow Cooked Saffron Butter Chicken",
                    description = "Spiced tandoori chicken thighs simmered in an indulgent gravy of sweet tomatoes, cashews, aromatic spices, finished with butter and full milk cream.",
                    price = 16.00,
                    imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 9,
                    chefId = chef4Id,
                    name = "Clay Oven Garlic Naan Bread",
                    description = "Flacid, pillowy traditional flatbread brushed with organic fresh garlic, green chopped herbs, and melted pure clarified butter (ghee). Set of 3.",
                    price = 5.50,
                    imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?w=300",
                    category = "Starters",
                    isAvailable = true
                )
            )

            dao.insertMeal(
                MealEntity(
                    id = 10,
                    chefId = chef5Id,
                    name = "Smoky Lagos Party Jollof Rice",
                    description = "Parboiled long-grain rice slow-cooked in a rich, smokey tomato, pepper, and onion reduction. Served with grilled spiced chicken and sweet fried plantain (Dodo).",
                    price = 18.50,
                    imageUrl = "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 11,
                    chefId = chef5Id,
                    name = "Rich Egusi Soup & Pounded Yam",
                    description = "Vibrant soup prepared from ground melon seeds, leafy spinach, crayfish, and tender slow-braised beef chunks. Served with warm, fluffy pounded yam.",
                    price = 20.00,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 12,
                    chefId = chef5Id,
                    name = "Spicy Flame-Grilled Beef Suya",
                    description = "Thinly sliced beef tenderloin marinated in authentic Northern Nigerian Yaji spice rub (ground peanuts, ginger, chili, and garlic), flame-grilled to perfection.",
                    price = 12.00,
                    imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=300",
                    category = "Starters",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 13,
                    chefId = chef5Id,
                    name = "Traditional Amala & Abula Stew",
                    description = "Smooth yam flour paste cooked slowly to retain authenticity. Served with Ewedu (jute leaves), Gbegiri (honey beans stew), and succulent assorted slow-simmered beef in spicy bell pepper sauce.",
                    price = 19.00,
                    imageUrl = "https://images.unsplash.com/photo-1604329760661-e71dc83f8f26?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 14,
                    chefId = chef5Id,
                    name = "Egusi Soup with Pounded Yam",
                    description = "Richly cooked thick melon seed soup mixed with organic spinach, locust beans (Iru), smoked fish, stockfish, and seasoned stewed meats. Paired with beautifully hand-sculpted smooth, warm Pounded Yam.",
                    price = 21.00,
                    imageUrl = "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=300",
                    category = "Mains",
                    isAvailable = true
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 15,
                    chefId = chef5Id,
                    name = "Asun Grilled Goat Meat Peppered",
                    description = "Delectable chunks of goat meat slow-roasted and sauteed in a fiery, hot habanero pepper and onion mix, infused with traditional West African local spices.",
                    price = 14.50,
                    imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300",
                    category = "Starters",
                    isAvailable = true
                )
            )

            // Pre-populate Reviews to establish trust immediately
            dao.insertReview(
                ReviewEntity(
                    id = 1,
                    chefId = chef1Id,
                    mealId = 1,
                    reviewerName = "Samantha Miller",
                    rating = 5,
                    comment = "This lasagna is heavenly! Better than what I had in Rome last summer. Still piping hot when it arrived."
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 2,
                    chefId = chef1Id,
                    mealId = 2,
                    reviewerName = "Jordan K.",
                    rating = 4,
                    comment = "Incredibly fragrant truffle pasta. Portion size was great, would highly recommend ordering."
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 3,
                    chefId = chef2Id,
                    mealId = 4,
                    reviewerName = "William Mercer",
                    rating = 5,
                    comment = "The black garlic broth is purely out of this world. Spot-on authentic, Kenji is amazing!"
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 4,
                    chefId = chef3Id,
                    mealId = 6,
                    reviewerName = "Clara Gomez",
                    rating = 5,
                    comment = "The mole flavor complexity is extraordinary. Will absolute buy again next week."
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 5,
                    chefId = chef4Id,
                    mealId = 8,
                    reviewerName = "Amir H.",
                    rating = 5,
                    comment = "Best butter chicken in the city! Fluffy garlic naan was out of this oven fresh!"
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 6,
                    chefId = chef5Id,
                    mealId = 10,
                    reviewerName = "Tunde Adelaja",
                    rating = 5,
                    comment = "Finally, authentic Jollof rice in the Bay Area! That smoky flavor is 100% spot-on Lagos party style."
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 7,
                    chefId = chef5Id,
                    mealId = 11,
                    reviewerName = "Nneka Okafor",
                    rating = 5,
                    comment = "The Egusi soup had the perfect texture and seasoning, and the pounded yam was so fresh and soft!"
                )
            )

            // Pre-populate a baseline alert to show in the system
            dao.insertAlert(
                AlertEntity(
                    id = 1,
                    title = "Welcome to D-KITCN!",
                    message = "Discover authentic, gourmet home-prepared meals prepared passionately by master food enthusiasts right in your neighborhood. Enjoy direct tracking, tutorial videos, and trust reviews!",
                    isRead = false
                )
            )
        }
    }
}
