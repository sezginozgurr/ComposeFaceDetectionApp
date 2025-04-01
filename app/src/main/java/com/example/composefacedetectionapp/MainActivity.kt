package com.example.composefacedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.composefacedetectionapp.camera.CameraManager
import com.example.composefacedetectionapp.ui.theme.ComposeFaceDetectionAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var cameraManager: CameraManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraManager.cameraStart()
        } else {
            Toast.makeText(this, "Kamera izni reddedildi!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askCameraPermission()
        setContent {
            ComposeFaceDetectionAppTheme {
                MainScreen(cameraManager)
            }
        }
    }

    private fun askCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraManager.cameraStart()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun MainScreen(cameraManager: CameraManager) {
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { cameraManager.changeCamera() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                        contentDescription = "Kamerayı çevir"
                    )
                }
                
                FloatingActionButton(
                    onClick = { cameraManager.cameraStop() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "Durdur"
                    )
                }
                
                FloatingActionButton(
                    onClick = { cameraManager.cameraStart() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = "Başlat"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}