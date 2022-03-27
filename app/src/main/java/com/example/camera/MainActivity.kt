package com.example.camera

import android.media.MediaCodec
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView

class MainActivity : AppCompatActivity() {
    var mMediaPlayer: MediaPlayer? = null

    var mSurfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSurfaceView = findViewById(R.id.surfaceView)


        mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                val h264Player = H264Player(this@MainActivity, "", p0.surface);
                h264Player.start();
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }

        })


        mMediaPlayer = MediaPlayer.create(this, 1);
        var mediaCodec = MediaCodec.createEncoderByType("video/avc");
        mediaCodec.configure(null, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }
}