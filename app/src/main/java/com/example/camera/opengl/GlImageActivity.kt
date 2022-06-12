package com.example.camera.opengl

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.camera.R

class GlImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_gl_image)
        setupViews()
    }

    private fun setupViews() {
        val glSurfaceView = GLSurfaceView(this)
        setContentView(glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        val textureRender = TextureRender()
        glSurfaceView.setRenderer(textureRender)
    }
}