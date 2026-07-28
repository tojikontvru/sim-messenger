package tj.safaraligroup.sim

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class IMOApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore (offline persistence + caching)
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings
    }

    companion object {
        lateinit var instance: IMOApplication
            private set
    }
}
