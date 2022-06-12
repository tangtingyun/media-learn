package com.example.camera.opengl.camera

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class CameraView : GLSurfaceView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setEGLContextClientVersion(2)
        val cameraRender = CameraRender(this)
        setRenderer(cameraRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}