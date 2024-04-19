package fr.nextu.licha_ilan

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.gson.Gson
import fr.nextu.licha_ilan.databinding.ActivityMainBinding
import fr.nextu.licha_ilan.entity.Movies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var json: TextView
    lateinit var movies_recycler: RecyclerView
    val db: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        findViewById<Button>(R.id.button_first)
//            finish()
//        }

//        json = findViewById(R.id.json)

        movies_recycler = findViewById<RecyclerView>(R.id.movies_recycler).apply {
            adapter = MovieAdapter(Movies(emptyList()))
            layoutManager = LinearLayoutManager(this@MainActivity2)
        }

//        db = AppDatabase.getInstance(applicationContext)

        createNotificationChannel()
    }

    override fun onStart() {
        super.onStart()
//        test()
        updateViewFromDB()
        requestPictureList(::moviesFromJson)
//        getPictureList()
    }

    fun test() {
//        BeersService.startAction(this)
    }

//    fun getPictureList() = runBlocking {
//        val ret = withContext(Dispatchers.IO){
//            requestPictureList()
//        }
//
//        json.text = ret
//    }

    fun moviesFromJson(json: String) {
        val gson = Gson()
        val om = gson.fromJson(json, Movies::class.java)
        db.movieDao().insertAll(*om.movies.toTypedArray())
    }

    fun getPictureList() {
        CoroutineScope(Dispatchers.IO).launch {
            requestPictureList {
                val gson = Gson();
                val om = gson.fromJson(it, Movies::class.java)
//                Log.d("TEEEEST", om.toString())
//                json.text = it
//                movies_recycler.adapter = MovieAdapter(om)
                db.movieDao().insertAll(*om.movies.toTypedArray())
//                movies_recycler.adapter = MovieAdapter(Movies(db.movieDao().getAll()))
            }
        }
    }

    fun requestPictureList(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            val client = OkHttpClient()

            val request: Request = Request.Builder()
                .url("https://api.betaseries.com/movies/list")
                .get()
                .addHeader("X-BetaSeries-Key", "470d2afc452f")
                .build()

            val response: Response = client.newCall(request).execute()
//        val body = response.body?.string() ?: ""

//        CoroutineScope(Dispatchers.Main).launch {
//            notifyNewData(body)
    //            callback(body)
//        }
            callback(response.body?.string() ?: "")
        }
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Movie upadte"
            val descriptionText = "A update notification when movies come"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    //    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notifyNewData(response: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Movies list update")
            .setContentText(response)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity2,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
//                this@MainActivity2.registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
//                    if (isGranted){
//                        Log.d("MainActivity", "Permission granted")
//                        notify(1, builder.build())
//                    }
//                    else {
//                        Log.d("MainActivity", "Permission denied")
//                    }
//                }.launch(Manifest.permission.POST_NOTIFICATIONS)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                return@with
            }
            notify(1, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "fr_nextu_licha_ilan_movie_update"
    }

    fun updateViewFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            val flow = db.movieDao().getFlowData()
            flow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    movies_recycler.adapter = MovieAdapter(Movies(it))
                    notifyNewData(it.toString())
                }
            }
        }
    }
}