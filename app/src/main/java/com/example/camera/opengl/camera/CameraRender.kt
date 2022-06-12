package com.example.camera.opengl.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.core.util.Consumer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CameraRender(private val cameraView: CameraView) : GLSurfaceView.Renderer,
    Preview.SurfaceProvider,
    SurfaceTexture.OnFrameAvailableListener {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private var mCameraTexture: SurfaceTexture? = null

    private var screenFilter: ScreenFilter? = null

    private lateinit var textures: IntArray
    var mtx = FloatArray(16)


    init {

    }


    // render
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textures = createOESTexture()
        mCameraTexture = SurfaceTexture(textures[0])
        mCameraTexture?.setOnFrameAvailableListener(this)
        screenFilter = ScreenFilter(cameraView.context)
        val activity: AppCompatActivity = cameraView.context as AppCompatActivity
        val cameraHelper = CameraHelper(activity, this)
        Log.i(TAG, "onSurfaceCreated: " + Thread.currentThread().getName() + "  " + textures[0]);
    }

    private fun createOESTexture(): IntArray {
        val arr = IntArray(1)
        GLES20.glGenTextures(1, arr, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, arr[0])
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        return arr
    }

    // render
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenFilter?.setSize(width, height)
        Log.i(
            TAG,
            "onSurfaceChanged: " + Thread.currentThread()
                .getName() + "  " + width + " --- " + height
        );
    }

    // render
    override fun onDrawFrame(gl: GL10?) {
        Log.i(TAG, "onDrawFrame: " + Thread.currentThread().getName());
        mCameraTexture?.updateTexImage()
        mCameraTexture?.getTransformMatrix(mtx)
        screenFilter?.setTransformMatrix(mtx)
        screenFilter?.onDraw(textures[0])
    }

    // mCameraTexture
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.i(TAG, "onFrameAvailable: " + Thread.currentThread().getName());
        cameraView.requestRender()
    }

    // SurfaceProvider
    override fun onSurfaceRequested(request: SurfaceRequest) {
        val size = request.resolution
        mCameraTexture?.setDefaultBufferSize(size.width, size.height)
        Log.i(
            TAG, "onSurfaceRequested1: " + Thread.currentThread().getName() +
                    "  width:  " + size.width + "  height  " + size.height
        );
        val surface = Surface(mCameraTexture)
        request.provideSurface(surface, executor, Consumer {
            Log.i(TAG, "onSurfaceRequested2: provideSurface ${it.resultCode}")
            surface.release()
            mCameraTexture?.release()
        })
    }


    companion object {
        private const val TAG = "CameraXBasic"
    }
}