package fuzzy.cozy.com

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Langsung ke SplashScreen
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}