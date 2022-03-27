package com.example.camera

import android.content.Context
import android.graphics.YuvImage
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaPlayer
import android.view.Surface

class H264Player(
    private val context: Context,
    private val path: String,
    private val surface: Surface
) : Runnable {

    private val mediaPlayer: MediaPlayer? = null;
    private var mMediaCodec: MediaCodec = MediaCodec.createDecoderByType("video/avc");


    fun start() {
        val mediaFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720);
        mMediaCodec.configure(mediaFormat, surface, null, 0);
        mMediaCodec.start()
        var yuvImage: YuvImage? = null
    }

    override fun run() {

    }
}