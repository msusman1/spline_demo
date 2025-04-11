package com.msusman.splinedemo.spline


import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SplineTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), SurfaceTextureListener {

    var onSurfaceReady: ((SurfaceWithSize) -> Unit)? = null
    var onSurfaceResized: ((SurfaceWithSize) -> Unit)? = null
    var onSurfaceDestroyed: ((Surface) -> Unit)? = null
    var onTouchStart: ((ByteBuffer, Int) -> Unit)? = null
    var onTouchEnd: ((ByteBuffer, Int) -> Unit)? = null
    var onTouchMove: ((ByteBuffer, Int) -> Unit)? = null

    init {
        surfaceTextureListener = this
        isOpaque = false
        isFocusable = true
        isFocusableInTouchMode = true
        Timber.d("Init")
    }

    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        Timber.d("onSurfaceTextureAvailable")
        val surface = Surface(surfaceTexture)
        val surfaceWithSize = SurfaceWithSize(surface, height, width)
        onSurfaceReady?.invoke(surfaceWithSize)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Timber.d("onSurfaceTextureSizeChanged")
        val surface = Surface(surfaceTexture)
        val surfaceWithSize = SurfaceWithSize(surface, height, width)
        onSurfaceResized?.invoke(surfaceWithSize)

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Timber.d("onSurfaceTextureDestroyed")
        onSurfaceDestroyed?.invoke(Surface(surface))
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val indices = if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            IntArray(event.pointerCount) { it }
        } else {
            intArrayOf(event.actionIndex)
        }

        val bufferSize = indices.size * (Integer.BYTES + 2 * java.lang.Float.BYTES)
        val buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        for (index in indices) {
            val pointerId = event.getPointerId(index)
            val touchX = event.getX(index)
            val touchY = event.getY(index)
            buffer.putInt(pointerId)
            buffer.putFloat(touchX)
            buffer.putFloat(touchY)
        }
        buffer.flip()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                onTouchStart?.invoke(buffer, indices.size)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->
                onTouchEnd?.invoke(buffer, indices.size)

            MotionEvent.ACTION_MOVE ->
                onTouchMove?.invoke(buffer, indices.size)
        }
        return true
    }


}
