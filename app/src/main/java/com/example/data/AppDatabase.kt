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
    @Query("SELECT COUNT(*) FROM chefs")
    suspend fun getChefCount(): Int

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

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("SELECT * FROM chat_messages WHERE chefId = :chefId ORDER BY timestamp ASC")
    fun getChatMessagesForChef(chefId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long
}

@Database(
    entities = [
        ChefEntity::class,
        MealEntity::class,
        OrderEntity::class,
        ReviewEntity::class,
        AlertEntity::class,
        ChatMessageEntity::class
    ],
    version = 8,
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

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (database.dao().getChefCount() == 0) {
                            populateInitialData(database.dao())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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

            val chef6Id = dao.insertChef(
                ChefEntity(
                    id = 6,
                    name = "Chef Kofi Mensah",
                    rating = 4.88f,
                    address = "Haight-Ashbury Ghanaian Hub - 1600 Haight St",
                    cuisineType = "Authentic Ghanaian Cuisine",
                    phone = "+1 (555) 724-8190",
                    bio = "Kofi brings the authentic taste of Accra's famous street markets to your plate. Specializes in multi-layered smoky Waakye, perfectly spiced Ghanaian Jollof, and sweet ginger-infused Kelewele.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk",
                    youtubeChannelName = "Kofi's Accra Kitchen",
                    avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
                    latitude = 37.7699,
                    longitude = -122.4468,
                    followersCount = 245
                )
            ).toInt()

            val chef7Id = dao.insertChef(
                ChefEntity(
                    id = 7,
                    name = "Chef Layla Haddad",
                    rating = 4.92f,
                    address = "Richmond Middle Eastern Eats - 320 Clement St",
                    cuisineType = "Gourmet Arab & Middle Eastern",
                    phone = "+1 (555) 492-7104",
                    bio = "Layla has been sharing the warmth of Middle Eastern hospitality for over 15 years. Famous for her hand-marinated Shish Tawook, gourmet hummus, and authentic pistachio-loaded crispy Baklava.",
                    youtubeChannelUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0",
                    youtubeChannelName = "Layla's Levantine Table",
                    avatarUrl = "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?w=150",
                    latitude = 37.7829,
                    longitude = -122.4612,
                    followersCount = 375
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 3,
                    chefId = chef1Id,
                    name = "Classic Italian Tiramisu",
                    description = "Espresso-soaked java biscuits layered with fluffy sweet mascarpone whip and dusted with dark cocoa powder.",
                    price = 8.50,
                    imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=P_mG69_PshQ"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=P_mG69_PshQ"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
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
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 16,
                    chefId = chef5Id,
                    name = "Fiery Nigerian Ewa Agoyin & Bread",
                    description = "Creamy, slow-cooked, buttery mashed honey beans topped with a legendary dark, rich, deeply caramelized palm oil palm-chili pepper sauce. Served with fresh, soft and fluffy Agege bread.",
                    price = 13.50,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300",
                    category = "Mains",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )

            // Chef 1 (Italian) New Desserts and Drinks
            dao.insertMeal(
                MealEntity(
                    id = 16,
                    chefId = chef1Id,
                    name = "Pistachio Panna Cotta",
                    description = "Creamy, silky-smooth Madagascar vanilla bean panna cotta topped with freshly crushed roasted Sicilian green pistachios and a hint of mint.",
                    price = 7.50,
                    imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 17,
                    chefId = chef1Id,
                    name = "Non-Alcoholic Aperol Spritz",
                    description = "A sophisticated, bubbly, and refreshing mocktail featuring bitter orange herbal notes, sparkling water, and finished with a fresh slice of orange.",
                    price = 6.00,
                    imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 18,
                    chefId = chef1Id,
                    name = "San Pellegrino Blood Orange",
                    description = "Premium chilled Italian sparkling fruit beverage made with juice from sun-ripened Mediterranean blood oranges.",
                    price = 3.50,
                    imageUrl = "https://images.unsplash.com/photo-1556881286-fc6915169721?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )

            // Chef 2 (Japanese) New Desserts and Drinks
            dao.insertMeal(
                MealEntity(
                    id = 19,
                    chefId = chef2Id,
                    name = "Matcha Mille Crepe Cake",
                    description = "Twenty layers of paper-thin green tea crepes hand-stacked and spread with fresh premium Japanese matcha sweet whipped cream.",
                    price = 8.50,
                    imageUrl = "https://images.unsplash.com/photo-1536680465769-2365207b035e?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=P_mG69_PshQ"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 20,
                    chefId = chef2Id,
                    name = "Matcha Green Tea Latte",
                    description = "Pure stone-ground organic Japanese matcha whisked with velvety steamed oat milk and sweetened with organic agave nectar.",
                    price = 5.50,
                    imageUrl = "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=P_mG69_PshQ"
                )
            )

            // Chef 3 (Mexican) New Desserts and Drinks
            dao.insertMeal(
                MealEntity(
                    id = 21,
                    chefId = chef3Id,
                    name = "Cinnamon Sugar Churros",
                    description = "Golden-fried crispy pastry dough rods coated in aromatic cinnamon sugar, served warm with a side of authentic cajeta (goat milk caramel) dip.",
                    price = 6.00,
                    imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 22,
                    chefId = chef3Id,
                    name = "Horchata de Arroz",
                    description = "Traditional creamy Mexican beverage made of rice milk, ground almonds, sweet cinnamon, vanilla, and chilled over crushed ice.",
                    price = 4.50,
                    imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 23,
                    chefId = chef3Id,
                    name = "Hibiscus Agua Fresca (Jamaica)",
                    description = "Chilled, sweet-tart refreshing tea brewed from real organic dried hibiscus blossoms and served with a fresh lime wheel.",
                    price = 4.00,
                    imageUrl = "https://images.unsplash.com/photo-1497534446932-c925b458314e?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=Q73uWbAArI0"
                )
            )

            // Chef 4 (Indian) New Desserts and Drinks
            dao.insertMeal(
                MealEntity(
                    id = 24,
                    chefId = chef4Id,
                    name = "Warm Gulab Jamun Sweet",
                    description = "Golden fried paneer and milk-solid dumplings steeped in a warm, aromatic syrup of organic rosewater and green cardamom seeds.",
                    price = 6.50,
                    imageUrl = "https://images.unsplash.com/photo-1589135304601-cd298b3f1e68?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 25,
                    chefId = chef4Id,
                    name = "Creamy Mango Lassi",
                    description = "Classic smooth yogurt drink blended with sweet ripe Alphonso mango pulp, a touch of milk, and a pinch of ground cardamom.",
                    price = 5.00,
                    imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 26,
                    chefId = chef4Id,
                    name = "Steaming Masala Chai",
                    description = "Rich Assam black tea slow-brewed on the stove with crushed fresh ginger, green cardamom, cloves, cinnamon, and creamy whole milk.",
                    price = 4.00,
                    imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )

            // Chef 5 (Nigerian / West African) New Desserts and Drinks
            dao.insertMeal(
                MealEntity(
                    id = 27,
                    chefId = chef5Id,
                    name = "Authentic Nigerian Puff Puff",
                    description = "Golden-brown, deep-fried yeasted dough balls that are sweet, crispy on the outside, and pillowy soft inside. An iconic Nigerian party treat.",
                    price = 6.00,
                    imageUrl = "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 28,
                    chefId = chef5Id,
                    name = "Zobo Hibiscus Ginger Drink",
                    description = "Traditional tangy crimson beverage brewed from dried hibiscus petals, crushed fresh ginger, sweet cloves, and organic pineapple juice.",
                    price = 4.50,
                    imageUrl = "https://images.unsplash.com/photo-1497534446932-c925b458314e?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 38,
                    chefId = chef5Id,
                    name = "Savory Nigerian Moi Moi",
                    description = "Delicious, rich, steamed bean pudding made from a blend of peeled black-eyed peas, red bell peppers, onions, ginger, and garlic, stuffed with boiled egg and fish flakes.",
                    price = 8.50,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300",
                    category = "Starters",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 39,
                    chefId = chef5Id,
                    name = "Crunchy Nigerian Chin Chin",
                    description = "Crispy, sweet, bite-sized deep-fried snack made from wheat flour, sugar, butter, and a hint of warm, aromatic nutmeg.",
                    price = 5.50,
                    imageUrl = "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )

            // Chef 6 (Ghanaian) Cuisine
            dao.insertMeal(
                MealEntity(
                    id = 29,
                    chefId = chef6Id,
                    name = "Ghanaian Waakye Feast",
                    description = "Nutritious local dish of rice and black-eyed beans cooked with sorghum leaves. Served with deep, spicy shito sauce, boiled egg, cassava gari, spaghetti, and fried sweet plantain.",
                    price = 19.50,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300",
                    category = "Mains",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 30,
                    chefId = chef6Id,
                    name = "Spicy Ghanaian Jollof Rice",
                    description = "Vibrant, richly spiced long-grain rice slow-cooked in a robust seasoned tomato-pepper-onion stew, served with tender grilled chicken and sweet fried dodo.",
                    price = 18.00,
                    imageUrl = "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?w=300",
                    category = "Mains",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 31,
                    chefId = chef6Id,
                    name = "Kelewele (Spicy Fried Plantains)",
                    description = "Ripe, sweet plantain cubes marinated in a fiery blend of ginger, garlic, cloves, and chili, then fried until caramelized and golden.",
                    price = 7.50,
                    imageUrl = "https://images.unsplash.com/photo-1564329760661-e71dc83f8f26?w=300",
                    category = "Starters",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 32,
                    chefId = chef6Id,
                    name = "Buttery Ghana Meat Pie",
                    description = "Flaky, rich pastry dough filled with perfectly seasoned minced lean beef, diced carrots, potatoes, and sweet white onions.",
                    price = 6.50,
                    imageUrl = "https://images.unsplash.com/photo-1608897013039-887f21d8c804?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 33,
                    chefId = chef6Id,
                    name = "Sobolo Hibiscus Spiced Drink",
                    description = "Ghanaian cold-pressed hibiscus beverage infused with ginger, whole cloves, alligator pepper (grains of paradise), and sweetened with ripe pineapple essence.",
                    price = 4.50,
                    imageUrl = "https://images.unsplash.com/photo-1497534446932-c925b458314e?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=FLeSREbZ7Rk"
                )
            )

            // Chef 7 (Arab / Middle Eastern) Cuisine
            dao.insertMeal(
                MealEntity(
                    id = 34,
                    chefId = chef7Id,
                    name = "Shish Tawook Chicken Plate",
                    description = "Skewers of chicken breast marinated in yogurt, lemon juice, garlic, and wild Lebanese spices, char-grilled to juicy tenderness. Served with yellow rice, house garlic toum sauce, and warm pita.",
                    price = 17.50,
                    imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=300",
                    category = "Mains",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 35,
                    chefId = chef7Id,
                    name = "Gourmet Hummus & Warm Pita",
                    description = "Creamy puréed chickpeas with sesame tahini, fresh lemon juice, garlic, drizzled with premium organic cold-pressed olive oil and a dash of sumac. Served with fluffy clay-oven pita.",
                    price = 8.00,
                    imageUrl = "https://images.unsplash.com/photo-1577906096429-f73ae2789700?w=300",
                    category = "Starters",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 36,
                    chefId = chef7Id,
                    name = "Crispy Pistachio Baklava Duo",
                    description = "Paper-thin, layered golden phyllo pastry sheets loaded with chopped premium green pistachios, baked crisp and drizzled with sweet orange blossom honey syrup.",
                    price = 7.50,
                    imageUrl = "https://images.unsplash.com/photo-1519869325930-281384150729?w=300",
                    category = "Desserts",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
                )
            )
            dao.insertMeal(
                MealEntity(
                    id = 37,
                    chefId = chef7Id,
                    name = "Mint Lemonade (Limonana)",
                    description = "Traditional blended frozen beverage crafted from freshly squeezed lemon juice, sweet simple sugar syrup, and fresh green spearmint leaves. Ultra refreshing.",
                    price = 5.00,
                    imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=300",
                    category = "Drinks",
                    isAvailable = true,
                    tutorialVideoUrl = "https://www.youtube.com/watch?v=A2gR4K-tRE0"
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
            dao.insertReview(
                ReviewEntity(
                    id = 8,
                    chefId = chef6Id,
                    mealId = 29,
                    reviewerName = "Akua Boateng",
                    rating = 5,
                    comment = "The Waakye is absolutely flawless! The shito has that deep, smoky, authentic flavor I've been craving."
                )
            )
            dao.insertReview(
                ReviewEntity(
                    id = 9,
                    chefId = chef7Id,
                    mealId = 34,
                    reviewerName = "Yousef A.",
                    rating = 5,
                    comment = "Shish tawook is incredibly juicy and the garlic toum is exceptionally rich. A taste of home!"
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
