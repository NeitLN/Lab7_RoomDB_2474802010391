# Debug Report: Lab 07 Room Database

## 1. Part 4: Main thread database access (ProductRoomApp)

### What I did

In commit `1e38bd2` I added a plain (non-suspend) DAO method and called it straight from `MainActivity.onCreate()`, without `lifecycleScope.launch` or `viewModelScope.launch`:

```kotlin
// ProductDao.kt
@Insert(onConflict = OnConflictStrategy.REPLACE)
fun insertProductBlocking(product: ProductEntity)

// MainActivity.kt, end of onCreate()
AppDatabase.getDatabase(this).productDao()
    .insertProductBlocking(ProductEntity(name = "Main thread test", price = 1.0, quantity = 1))
```

A `suspend` DAO method would not crash here, because Room runs suspend queries on its own background executor. The crash only shows up when a blocking DAO call runs on the main thread, so a non-suspend method was needed to reproduce it. The app crashed as soon as it opened. The next commit (`62fdef4`) reverts the change so the final app runs normally.

### Logcat

```
E AndroidRuntime: FATAL EXCEPTION: main
E AndroidRuntime: Process: edu.vlu.week07.productroom, PID: 23934
E AndroidRuntime: java.lang.RuntimeException: Unable to start activity ComponentInfo{edu.vlu.week07.productroom/edu.vlu.week07.productroom.ui.MainActivity}: java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
E AndroidRuntime: 	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3782)
E AndroidRuntime: 	...
E AndroidRuntime: Caused by: java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
E AndroidRuntime: 	at androidx.room.RoomDatabase.assertNotMainThread(RoomDatabase.android.kt:592)
E AndroidRuntime: 	at androidx.room.util.DBUtil__DBUtil_androidKt.performBlocking(DBUtil.android.kt:68)
E AndroidRuntime: 	at androidx.room.util.DBUtil.performBlocking(Unknown Source:1)
E AndroidRuntime: 	at edu.vlu.week07.productroom.data.local.ProductDao_Impl.insertProductBlocking(ProductDao_Impl.kt:78)
E AndroidRuntime: 	at edu.vlu.week07.productroom.ui.MainActivity.onCreate(MainActivity.kt:67)
```

Exact error line:

```
java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
```

### Why Room enforces this rule

Reading or writing the SQLite file is disk I/O, and it can take from a few milliseconds to hundreds of milliseconds, while the main thread has to draw a new frame about every 16 ms and handle every touch event. If the main thread is blocked for around 5 seconds, Android shows an "Application Not Responding" (ANR) dialog, so Room calls `assertNotMainThread()` and fails fast with an `IllegalStateException` instead of letting the app freeze. Coroutines fix this without callbacks: the repository wraps each DAO call in `withContext(Dispatchers.IO)`, which moves the query to a thread pool made for blocking I/O and keeps the main thread free. The result comes back to the UI through `LiveData`, which Room refreshes after every write.

## 2. Notes debugging challenge (section 5 of the handout)

### Error 1: missing `@PrimaryKey` (build error)

Buggy code:

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    val id: Long = 0L,      // no @PrimaryKey
    val content: String
)
```

Error from KSP when I compiled it with Room 2.8.5:

```
e: [ksp] .../NoteBuggy.kt:10: An entity must have at least 1 property annotated with @PrimaryKey
```

(The handout quotes the older wording "An entity must have at least 1 field annotated with @PrimaryKey"; newer Room says "property".)

Why: every Room table needs a primary key so Room can generate `CREATE TABLE ... PRIMARY KEY` and find a row again for `@Update` and `@Delete`. The Room annotation processor checks this at compile time, so the build stops before any code runs.

Fix:

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String
)
```

### Error 2: database call on the main thread (runtime crash)

Buggy code:

```kotlin
class NoteRepository(private val dao: NoteDao) {
    fun add(note: NoteEntity) {
        dao.insert(note)    // called from the UI thread
    }
}
```

Note: as written in the handout, `dao.insert()` is a `suspend` function, so calling it from a normal `fun` is actually a compile error ("Suspend function should be called only from a coroutine or another suspend function"). The runtime crash appears when the DAO method is not `suspend` (or when it is wrapped in `runBlocking` with a blocking DAO), which is exactly what Part 4 above reproduces. Either way the cause is the same: the repository tries to touch the database on the caller's thread.

Fix: make the repository function `suspend`, move the work to `Dispatchers.IO`, and call it from a coroutine scope in the ViewModel.

```kotlin
class NoteRepository(
    private val dao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun add(note: NoteEntity) = withContext(ioDispatcher) {
        dao.insert(note)
    }
}

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    fun addNote(content: String) {
        viewModelScope.launch {
            repository.add(NoteEntity(content = content))
        }
    }
}
```

This is the same pattern `ProductRepository` and `ProductViewModel` use in this project.
