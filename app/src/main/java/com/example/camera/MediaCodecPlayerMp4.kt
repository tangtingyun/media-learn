package com.example.camera

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import kotlin.experimental.and


class MediaCodecPlayerMp4 : AppCompatActivity(), SurfaceHolder.Callback {

    val TAG = javaClass.simpleName;
    var mWorkThread: WorkThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val surfaceView = SurfaceView(this)
        surfaceView.holder.addCallback(this)
        setContentView(surfaceView)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mWorkThread == null) {
            mWorkThread = WorkThread(holder.surface);
            mWorkThread?.start();
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mWorkThread?.interrupt()
    }


    inner class WorkThread(val surface: Surface) : Thread() {

        private lateinit var mMediaExtractor: MediaExtractor
        private lateinit var mMediaCodec: MediaCodec

        val NAL_I = 19
        val NAL_VPS = 32

        override fun run() {
            super.run()
            mMediaExtractor = MediaExtractor()
            val path = "android.resource://" + packageName + "/" + R.raw.lechen
            mMediaExtractor.setDataSource(this@MediaCodecPlayerMp4, Uri.parse(path), null)
            for (i in 0 until mMediaExtractor.trackCount) {
                val format = mMediaExtractor.getTrackFormat(i)
                Log.d(TAG, ">> format i  $i  : $format")
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d(TAG, ">> format i  $i  : $mime")
                if (mime != null && mime.startsWith("video/")) {
                    mMediaExtractor.selectTrack(i)
                    mMediaCodec = MediaCodec.createDecoderByType(mime)
                    mMediaCodec.configure(format, surface, null, 0)
                }
            }
            mMediaCodec.start()

            var bufferInfo = MediaCodec.BufferInfo()
            var isEOS = false
            var startMs = System.currentTimeMillis()

            var inputBuffers = mMediaCodec.inputBuffers
            var outputBuffers = mMediaCodec.outputBuffers

            while (!Thread.interrupted()) {
                if (!isEOS) {
                    var dequeueInputBufferIndex = mMediaCodec.dequeueInputBuffer(10000)
                    if (dequeueInputBufferIndex >= 0) {
                        var byteBuffer = inputBuffers[dequeueInputBufferIndex]
                        Log.d(TAG, ">> buffer:  $byteBuffer")
                        var readSampleData = mMediaExtractor.readSampleData(byteBuffer, 0)
                        Log.d(TAG, ">> sampleSize: $readSampleData")

                        if (readSampleData < 0) {
                            Log.d(TAG, "没有数据了")
                            mMediaCodec.queueInputBuffer(
                                dequeueInputBufferIndex,
                                0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            mMediaCodec.queueInputBuffer(
                                dequeueInputBufferIndex,
                                0, readSampleData, mMediaExtractor.sampleTime, 0
                            )
                            mMediaExtractor.advance()
                        }
                    }

                }

                val dequeueOutputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                when (dequeueOutputBufferIndex) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        Log.d(TAG, ">> INFO_OUTPUT_BUFFERS_CHANGED")
                        outputBuffers = mMediaCodec.outputBuffers
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(TAG, ">> INFO_OUTPUT_FORMAT_CHANGED")
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d(TAG, ">> INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
                        var byteBuffer = outputBuffers[dequeueOutputBufferIndex]
                        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(10)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                                break
                            }
                        }
                        mMediaCodec.releaseOutputBuffer(dequeueOutputBufferIndex, true)
                    }
                }

                Log.d(TAG, ">> BUFFER_FLAG  ${bufferInfo.flags}")

                if (bufferInfo.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, ">> BUFFER_FLAG_END_OF_STREAM")
                    break
                }
            }

            mMediaCodec.stop()
            mMediaCodec.release()
            mMediaExtractor.release()
        }


        private fun dealFrame(bb: ByteBuffer) {
            var offset = 4
            var a: Int = 0x01;
            var b: Int = 0x7E;
            var c: Int = 1
            if (bb.get(2).toInt() == a) {
                offset = 3
            }
            val type: Int = bb.get(offset).toInt() and b shr c
            if (type == NAL_VPS) {
                Log.d(TAG, ">> VPS 帧 ")
            } else if (type == NAL_I) {
                Log.d(TAG, ">> I 帧 ")
            } else {
                Log.d(TAG, ">> 其他帧 ")
            }
        }
    }
}