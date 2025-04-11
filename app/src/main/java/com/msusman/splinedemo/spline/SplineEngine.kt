package com.msusman.splinedemo.spline

import android.content.Context
import android.view.Choreographer
import android.view.Surface
import design.spline.runtime.RustBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.channels.Channels

data class SurfaceWithSize(
    val surface: Surface, val height: Int, val width: Int
)


class SplineEngine(private val context: Context) {
    private var rustEngine: Long = 0L
    private var buffer: Buffer? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)
    val devicePixelRatio = context.resources.displayMetrics.density
    val choreographer = Choreographer.getInstance()
    var surfaceWithSize: SurfaceWithSize? = null
    val _engineInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val engineInitialized: StateFlow<Boolean> = _engineInitialized.asStateFlow()

    val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (rustEngine != 0L) {
                RustBridge.surfaceFrameEvent(rustEngine)
                choreographer.postFrameCallback(this)
            }
        }
    }
    var splineView: SplineTextureView = SplineTextureView(context).apply {
        onSurfaceReady = {
            surfaceWithSize = it
            if (rustEngine == 0L) {
                tryInitializingEngine()
            } else {
                RustBridge.surfaceCreateEvent(rustEngine, it.surface)
                RustBridge.surfaceResizeEvent(rustEngine, width, height, devicePixelRatio)
                choreographer.postFrameCallback(frameCallback)
            }
        }
        onSurfaceResized = {
            if (rustEngine != 0L) {
                RustBridge.surfaceResizeEvent(rustEngine, it.width, it.height, devicePixelRatio)
            }
        }
        onSurfaceDestroyed = {
            choreographer.removeFrameCallback(frameCallback)
            if (rustEngine != 0L) {
                RustBridge.surfaceDestroyEvent(rustEngine)
            }
        }
        onTouchStart = { byteBuffer, size ->
            if (rustEngine != 0L) {
                RustBridge.touchStartEvent(rustEngine, byteBuffer, size)
            }
        }
        onTouchEnd = { byteBuffer, size ->
            if (rustEngine != 0L) {
                RustBridge.touchEndEvent(rustEngine, byteBuffer, size)
            }
        }
        onTouchMove = { byteBuffer, size ->
            if (rustEngine != 0L) {
                RustBridge.touchMoveEvent(rustEngine, byteBuffer, size)
            }
        }

    }


    fun initialize(id: Int) {
        Timber.d("initialize() called with: id = $id")
        if (buffer == null) {
            val stream = this.context.resources.openRawResource(id)
            val byteBuffer = ByteBuffer.allocateDirect(stream.available())
            Channels.newChannel(stream).read(byteBuffer)
            this.buffer = byteBuffer
            stream.close()
        }
        tryInitializingEngine()
    }

    fun reInit(id: Int) {
        Timber.d("reInit() called with: id = $id")
        val stream = this.context.resources.openRawResource(id)
        val byteBuffer = ByteBuffer.allocateDirect(stream.available())
        Channels.newChannel(stream).read(byteBuffer)
        this.buffer = byteBuffer
        stream.close()
        if (rustEngine != 0L) {
            RustBridge.engineDestroy(rustEngine)
        }
        rustEngine = 0L
        tryInitializingEngine()
    }

    fun destroy() {
        Timber.d("destroy")
        if (rustEngine != 0L) {
            RustBridge.engineDestroy(rustEngine)
        }
    }

    private fun tryInitializingEngine() {
        if (buffer != null && rustEngine == 0L && surfaceWithSize != null) {
            _engineInitialized.value = false
            Timber.d("loadEngineAsync rustEngine:$rustEngine")
            uiScope.launch {
                Timber.d("RustBridge.engineCreate(surface, buf) Before")
                val engine = withContext(Dispatchers.Default) {
                    RustBridge.engineCreate(surfaceWithSize!!.surface, buffer)
                }
                Timber.d("RustBridge.engineCreate(surface, buf) After")
                rustEngine = engine
                RustBridge.surfaceResizeEvent(
                    rustEngine,
                    surfaceWithSize!!.width,
                    surfaceWithSize!!.height,
                    devicePixelRatio
                )
                _engineInitialized.value = true
                choreographer.postFrameCallback(frameCallback)
                Timber.d("Engine Initialized")
            }
        }
    }

}

