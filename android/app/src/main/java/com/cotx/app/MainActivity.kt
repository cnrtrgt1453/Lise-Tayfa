package com.cotx.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.cotx.app.ui.navigation.CotxNavGraph
import com.cotx.app.ui.theme.CotxTheme
import com.cotx.app.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled
        }

    private var targetQuestionId by androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val qId = intent.getStringExtra(NotificationHelper.EXTRA_QUESTION_ID)
        if (!qId.isNullOrEmpty()) {
            targetQuestionId = qId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract question ID from notification intent if launched from notification
        targetQuestionId = intent?.getStringExtra(NotificationHelper.EXTRA_QUESTION_ID)

        // Create system notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        checkAndRequestNotificationPermission()

        // Sync FCM device token with Firestore
        syncFcmToken()

        setContent {
            CotxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CotxNavGraph(navController = navController, initialQuestionId = targetQuestionId)
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                if (!currentUid.isNullOrEmpty() && !token.isNullOrEmpty()) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(currentUid)
                        .update("fcmToken", token)
                }
            }
        }
    }
}
