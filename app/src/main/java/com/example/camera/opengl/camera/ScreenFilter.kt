package com.example.camera.opengl.camera

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TEXTURE0
import com.example.camera.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class ScreenFilter//
//  先编译    再链接   再运行  程序
//cpu 1   没有用  索引     program gpu
//0
//接收纹理坐标，接收采样器采样图片的坐标
//1
//采样点的坐标
//变换矩阵， 需要将原本的vCoord（01,11,00,10） 与矩阵相乘
//        构造 的时候 给 数据  vPosition gpu 是1  不是 2
    (context: Context?) {

    //    顶点着色器
    //    片元着色器
    private var program = 0

    //句柄  gpu中  vPosition
    private var vPosition = 0
    var textureBuffer // 纹理坐标
            : FloatBuffer? = null
    private var vCoord = 0
    private var vTexture = 0
    private var vMatrix = 0
    private var mWidth = 0
    private var mHeight = 0
    private var mtx: FloatArray? = null

    //gpu顶点缓冲区
    var vertexBuffer //顶点坐标缓存区
            : FloatBuffer? = null
    var VERTEX = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )

    var TEXTURE = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    init {
        vertexBuffer =
            ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer?.clear()
        vertexBuffer?.put(VERTEX)
        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureBuffer?.clear()
        textureBuffer?.put(TEXTURE)
        val vertexSharder: String = OpenGLUtils.readRawTextFile(context, R.raw.camera_vert)
        val fragSharder: String = OpenGLUtils.readRawTextFile(context, R.raw.camera_frag)
        program = OpenGLUtils.loadProgram(vertexSharder, fragSharder)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        vCoord = GLES20.glGetAttribLocation(program, "vCoord")
        vTexture = GLES20.glGetUniformLocation(program, "vTexture")
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
    }

    fun setSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }

    //摄像头数据  渲染   摄像  开始渲染
    fun onDraw(texture: Int) {
//        opengl
//View 的大小
        GLES20.glViewport(0, 0, mWidth, mHeight)
        //        使用程序
        GLES20.glUseProgram(program)
        //        从索引位0的地方读
        vertexBuffer?.position(0)
        //     index   指定要修改的通用顶点属性的索引。
//     size  指定每个通用顶点属性的组件数。
        //        type  指定数组中每个组件的数据类型。
        //        接受符号常量GL_FLOAT  GL_BYTE，GL_UNSIGNED_BYTE，GL_SHORT，GL_UNSIGNED_SHORT或GL_FIXED。 初始值为GL_FLOAT。
//      normalized    指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）。
//cpu 和 GPU
//        反人类的操作
        GLES20.glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, vertexBuffer)
        //        生效
        GLES20.glEnableVertexAttribArray(vPosition)
        textureBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            vCoord, 2, GLES20.GL_FLOAT,
            false, 0, textureBuffer
        )
        //CPU传数据到GPU，默认情况下着色器无法读取到这个数据。 需要我们启用一下才可以读取
        GLES20.glEnableVertexAttribArray(vCoord)

//        形状就确定了

//         32  数据
//gpu    获取读取
        GLES20.glActiveTexture(GL_TEXTURE0)

//生成一个采样
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
        GLES20.glUniform1i(vTexture, 0)
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
        //通知画画，
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }


}