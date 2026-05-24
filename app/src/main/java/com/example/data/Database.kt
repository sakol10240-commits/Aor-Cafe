package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "drink_items")
data class DrinkItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val stock: Int,
    val minThreshold: Int = 10,
    val category: String = "Coffee (กาแฟ)",
    val colorHex: String = "#FF7043"
)

@Entity(tableName = "client_visits")
data class ClientVisit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val drinkName: String,
    val drinkPrice: Double,
    val qty: Int,
    val totalPrice: Double,
    val orderTime: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drink_items ORDER BY name ASC")
    fun getAllDrinks(): Flow<List<DrinkItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: DrinkItem)

    @Update
    suspend fun updateDrink(drink: DrinkItem)

    @Delete
    suspend fun deleteDrink(drink: DrinkItem)

    @Query("SELECT * FROM client_visits ORDER BY orderTime DESC")
    fun getAllVisits(): Flow<List<ClientVisit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: ClientVisit)

    @Query("DELETE FROM client_visits WHERE id = :visitId")
    suspend fun deleteVisit(visitId: Int)

    @Query("UPDATE drink_items SET stock = stock - :qty WHERE name = :drinkName")
    suspend fun deductStock(drinkName: String, qty: Int)

    @Query("UPDATE drink_items SET stock = stock + :qty WHERE name = :drinkName")
    suspend fun addStock(drinkName: String, qty: Int)
}

@Database(entities = [DrinkItem::class, ClientVisit::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aorcafe_database_v6"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        try {
                            // Prepopulate 30 default drink/bakery items with raw SQL
                            // CATEGORY 1: Coffee (กาแฟ) - 11 Items
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('เอสเพรสโซ่ร้อน (Hot Espresso)', 45.00, 999, 0, 'Coffee (กาแฟ)', '#8D6E63')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('อเมริกาโน่ร้อน (Hot Americano)', 50.00, 999, 0, 'Coffee (กาแฟ)', '#90A4AE')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('อเมริกาโนเย็น (Iced Americano)', 55.00, 999, 0, 'Coffee (กาแฟ)', '#90A4AE')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('คาปูชิโน่ร้อน (Hot Cappuccino)', 60.00, 999, 0, 'Coffee (กาแฟ)', '#A1887F')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('คาปูชิโน่เย็น (Iced Cappuccino)', 65.00, 999, 0, 'Coffee (กาแฟ)', '#8D6E63')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ลาเต้ร้อน (Hot Latte)', 60.00, 999, 0, 'Coffee (กาแฟ)', '#CEB5A7')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ลาเต้เย็น (Iced Latte)', 65.00, 999, 0, 'Coffee (กาแฟ)', '#BCAAA4')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('คาราเมลมัคคิอาโต (Caramel Macchiato)', 75.00, 999, 0, 'Coffee (กาแฟ)', '#FFB74D')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('มอคค่าร้อน (Hot Mocha)', 60.00, 999, 0, 'Coffee (กาแฟ)', '#5D4037')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('มอคค่าเย็น (Iced Mocha)', 65.00, 999, 0, 'Coffee (กาแฟ)', '#4E342E')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('เดอร์ตี้คอฟฟี่ (Dirty Coffee)', 75.00, 999, 0, 'Coffee (กาแฟ)', '#D7CCC8')")

                            // CATEGORY 2: Tea (ชา) - 7 Items
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('มัทฉะลาเต้ (Matcha Latte)', 75.00, 999, 0, 'Tea (ชา)', '#81C784')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('โฮจิฉะลาเต้ (Hojicha Latte)', 70.00, 999, 0, 'Tea (ชา)', '#795548')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชาไทยพรีเมียม (Premium Thai Tea)', 55.00, 999, 0, 'Tea (ชา)', '#FF7043')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชาเขียวนม (Iced Green Tea)', 55.00, 999, 0, 'Tea (ชา)', '#66BB6A')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชามะนาวเย็น (Iced Lemon Tea)', 50.00, 999, 0, 'Tea (ชา)', '#FFEE58')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชาพีชเย็น (Iced Peach Tea)', 55.00, 999, 0, 'Tea (ชา)', '#FFAB91')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชาอูหลงเย็น (Iced Oolong Tea)', 45.00, 999, 0, 'Tea (ชา)', '#A1887F')")

                            // CATEGORY 3: Specialty (เมนูพิเศษ) - 4 Items
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ชานมเผือก (Taro Bubble Tea)', 75.00, 999, 0, 'Specialty (เมนูพิเศษ)', '#BA68C8')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('นมสดคาราเมล (Caramel Fresh Milk)', 60.00, 999, 0, 'Specialty (เมนูพิเศษ)', '#FFE082')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ดาร์กช็อกโกแลตเย็น (Iced Dark Chocolate)', 65.00, 999, 0, 'Specialty (เมนูพิเศษ)', '#3E2723')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('นมชมพูเย็น (Pink Milk)', 45.00, 999, 0, 'Specialty (เมนูพิเศษ)', '#F48FB1')")

                            // CATEGORY 4: Cold Brew (ดีคอฟ/สกัดเย็น) - 5 Items
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('โคลด์บริวออริจินัล (Original Cold Brew)', 75.00, 999, 0, 'Cold Brew (ดีคอฟ/สกัดเย็น)', '#4E342E')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('โคลด์บริวเลมอน (Honey Lemon Cold Brew)', 85.00, 999, 0, 'Cold Brew (ดีคอฟ/สกัดเย็น)', '#FDD835')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ยูซุโคลด์บริว (Yuzu Cold Brew)', 95.00, 999, 0, 'Cold Brew (ดีคอฟ/สกัดเย็น)', '#FFB300')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('นิโตรโคลด์บริว (Nitro Cold Brew)', 120.00, 999, 0, 'Cold Brew (ดีคอฟ/สกัดเย็น)', '#263238')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('สตรอว์เบอร์รี่เลมอนเนด (Strawberry Lemonade)', 65.00, 999, 0, 'Cold Brew (ดีคอฟ/สกัดเย็น)', '#F06292')")

                            // CATEGORY 5: Bakery (เบเกอรี่) - 3 Items
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ครัวซองต์เนยสด (Butter Croissant)', 65.00, 999, 0, 'Bakery (เบเกอรี่)', '#FFB74D')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('ช็อกโกแลตฟัดจ์บราวนี่ (Fudge Chocolate Brownies)', 75.00, 999, 0, 'Bakery (เบเกอรี่)', '#4E342E')")
                            db.execSQL("INSERT INTO drink_items (name, price, stock, minThreshold, category, colorHex) VALUES ('เลมอนทาร์ตครีมมี่ (Lemon Cream Tart)', 85.00, 999, 0, 'Bakery (เบเกอรี่)', '#FFF176')")

                            // Prepopulate default client visits
                            val time = System.currentTimeMillis()
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Alex Rivera', 'คาราเมลมัคคิอาโต (Caramel Macchiato)', 75.00, 2, 150.00, ${time - 3600000}, 'Extra caramel drizzle')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Sarah Chen', 'มัทฉะลาเต้ (Matcha Latte)', 75.00, 1, 75.00, ${time - 7200000}, 'Oat milk substitute')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Marcus Vance', 'อเมริกาโนเย็น (Iced Americano)', 55.00, 1, 55.00, ${time - 10800000}, 'Double shot')")

                            // Historical records
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Emily Stone', 'ชานมเผือก (Taro Bubble Tea)', 75.00, 1, 75.00, ${time - 1 * 24 * 3600000L - 1800000}, 'Less sweet')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('David Kim', 'มัทฉะลาเต้ (Matcha Latte)', 75.00, 2, 150.00, ${time - 2 * 24 * 3600000L - 3600000}, 'Sweetened with honey')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Chloe Bennett', 'สตรอว์เบอร์รี่เลมอนเนด (Strawberry Lemonade)', 65.00, 1, 65.00, ${time - 4 * 24 * 3600000L}, 'Extra ice')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('James Wilson', 'คาราเมลมัคคิอาโต (Caramel Macchiato)', 75.00, 3, 225.00, ${time - 6 * 24 * 3600000L}, 'Whipped cream on top')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Zoe Saldana', 'อเมริกาโนเย็น (Iced Americano)', 55.00, 2, 110.00, ${time - 10 * 24 * 3600000L}, 'Light ice')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Robert Downey', 'มัทฉะลาเต้ (Matcha Latte)', 75.00, 1, 75.00, ${time - 15 * 24 * 3600000L}, 'Almond milk')")
                            db.execSQL("INSERT INTO client_visits (clientName, drinkName, drinkPrice, qty, totalPrice, orderTime, notes) VALUES ('Taylor Swift', 'สตรอว์เบอร์รี่เลมอนเนด (Strawberry Lemonade)', 65.00, 2, 130.00, ${time - 25 * 24 * 3600000L}, 'More strawberry')")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
