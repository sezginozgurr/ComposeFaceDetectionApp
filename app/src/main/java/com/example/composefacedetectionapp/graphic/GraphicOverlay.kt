package com.example.composefacedetectionapp.graphic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

abstract class GraphicOverlay<T : GraphicOverlay.Graphic>(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val lock = Any()
    private val graphics = mutableListOf<T>()
    private val imageMatrix = Matrix()
    private var imageWidth = 0
    private var imageHeight = 0
    private var scaleFactor = 1.0f
    private var postInferenceCallback: Runnable? = null
    private val needUpdateTransformation = AtomicBoolean(true)

    abstract class Graphic(private val overlay: GraphicOverlay<*>) {
        abstract fun draw(canvas: Canvas)
        protected fun drawRect(
            canvas: Canvas,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            paint: android.graphics.Paint
        ) {
            canvas.drawRect(left, top, right, bottom, paint)
        }

        protected fun drawCircle(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            paint: android.graphics.Paint
        ) {
            canvas.drawCircle(cx, cy, radius, paint)
        }

        protected fun drawLine(
            canvas: Canvas,
            startX: Float,
            startY: Float,
            stopX: Float,
            stopY: Float,
            paint: android.graphics.Paint
        ) {
            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }

        protected fun drawText(
            canvas: Canvas,
            text: String,
            x: Float,
            y: Float,
            paint: android.graphics.Paint
        ) {
            canvas.drawText(text, x, y, paint)
        }

        protected fun translateX(horizontal: Float): Float {
            return overlay.imageWidth * horizontal
        }

        protected fun translateY(vertical: Float): Float {
            return overlay.imageHeight * vertical
        }

        protected fun scale(horizontal: Float): Float {
            return horizontal * overlay.scaleFactor
        }

        protected fun postInvalidate() {
            overlay.postInvalidate()
        }
    }

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: RectangleOverlay) {
        synchronized(lock) {
            graphics.add(graphic as T)
        }
        postInvalidate()
    }

    fun remove(graphic: T) {
        synchronized(lock) {
            graphics.remove(graphic)
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            updateTransformationIfNeeded()

            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

    fun setImageSourceInfo(width: Int, height: Int, isFlipped: Boolean) {
        assert(width > 0 && height > 0)
        synchronized(lock) {
            imageWidth = width
            imageHeight = height
            needUpdateTransformation.set(true)
        }
        postInvalidate()
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation.get()) {
            return
        }

        val viewAspectRatio = width.toFloat() / height
        val imageAspectRatio = imageWidth.toFloat() / imageHeight

        val postScaleWidthOffset: Float
        val postScaleHeightOffset: Float
        if (viewAspectRatio > imageAspectRatio) {
            // Görüntü yüksekliğe göre ölçeklenir
            scaleFactor = height.toFloat() / imageHeight
            postScaleWidthOffset = (width - imageWidth * scaleFactor) / 2
            postScaleHeightOffset = 0f
        } else {
            // Görüntü genişliğe göre ölçeklenir
            scaleFactor = width.toFloat() / imageWidth
            postScaleWidthOffset = 0f
            postScaleHeightOffset = (height - imageHeight * scaleFactor) / 2
        }

        imageMatrix.reset()
        imageMatrix.postScale(scaleFactor, scaleFactor)
        imageMatrix.postTranslate(postScaleWidthOffset, postScaleHeightOffset)

        needUpdateTransformation.set(false)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
    }

    override fun setOnTouchListener(listener: OnTouchListener?) {
        super.setOnTouchListener(listener)
    }
}