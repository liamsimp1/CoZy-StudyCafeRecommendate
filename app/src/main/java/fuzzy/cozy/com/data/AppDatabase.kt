package fuzzy.cozy.com.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Cafe::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cafeDao(): CafeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafe_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed data
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.cafeDao()
                                dao.insert(Cafe(name = "Coffeeholic Space", address = "Jl. Melati No. 5, Mataram", wifiSpeed = 90, noiseLevel = 15, comfort = 9, price = 35, outlets = 10, latitude = -8.5833, longitude = 116.1167))
                                dao.insert(Cafe(name = "Study & Chill", address = "Kawasan Pendidikan, Mataram", wifiSpeed = 75, noiseLevel = 30, comfort = 8, price = 25, outlets = 7, latitude = -8.5900, longitude = 116.1200))
                                dao.insert(Cafe(name = "Kopi Senja", address = "Jl. Pejanggik, Mataram", wifiSpeed = 45, noiseLevel = 60, comfort = 7, price = 18, outlets = 4, latitude = -8.5840, longitude = 116.1050))
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
