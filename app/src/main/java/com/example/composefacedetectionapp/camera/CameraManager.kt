package com.example.composefacedetectionapp.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.composefacedetectionapp.graphic.GraphicOverlay
import com.example.composefacedetectionapp.graphic.utils.CameraUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var camera: Camera
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay<*>? = null
    private var lifecycleOwner: LifecycleOwner? = null

    fun initialize(
        previewView: PreviewView,
        graphicOverlay: GraphicOverlay<*>,
        lifecycleOwner: LifecycleOwner
    ) {
        this.previewView = previewView
        this.graphicOverlay = graphicOverlay
        this.lifecycleOwner = lifecycleOwner
    }

    fun cameraStart() {
        previewView?.let { previewView ->
            lifecycleOwner?.let { lifecycleOwner ->
                val cameraProcessProvider = ProcessCameraProvider.getInstance(context)

                cameraProcessProvider.addListener(
                    {
                        cameraProvider = cameraProcessProvider.get()
                        preview = Preview.Builder().build()

                        imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, CameraAnalyzer(graphicOverlay!!))
                            }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(cameraOption)
                            .build()

                        setCameraConfig(cameraProvider, cameraSelector)
                    },
                    ContextCompat.getMainExecutor(context)
                )
            }
        }
    }

    private fun setCameraConfig(cameraProvider: ProcessCameraProvider, cameraSelector: CameraSelector) {
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner!!,
                cameraSelector,
                preview,
                imageAnalysis
            )
            preview.setSurfaceProvider(previewView!!.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "setCameraConfig : $e")
        }
    }

    fun changeCamera() {
        cameraStop()
        cameraOption = if (cameraOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK
        CameraUtils.toggleSelector()
        cameraStart()
    }

    fun cameraStop() {
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
    }

    companion object {
        private const val TAG: String = "CameraManager"
        var cameraOption: Int = CameraSelector.LENS_FACING_FRONT
    }
}