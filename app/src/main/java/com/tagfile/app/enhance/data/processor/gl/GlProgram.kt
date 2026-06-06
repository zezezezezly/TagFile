package com.tagfile.app.enhance.data.processor.gl

import android.opengl.GLES20

class GlProgram(vertexSource: String, fragmentSource: String) {

    val programId: Int

    init {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        programId = GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vertexShader)
            GLES20.glAttachShader(prog, fragmentShader)
            GLES20.glLinkProgram(prog)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val log = GLES20.glGetProgramInfoLog(prog)
                GLES20.glDeleteProgram(prog)
                throw RuntimeException("Program link failed: $log")
            }

            GLES20.glDetachShader(prog, vertexShader)
            GLES20.glDetachShader(prog, fragmentShader)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(programId, name)
    }

    fun setUniform1f(name: String, value: Float) {
        GLES20.glUniform1f(getUniformLocation(name), value)
    }

    fun setUniform1i(name: String, value: Int) {
        GLES20.glUniform1i(getUniformLocation(name), value)
    }

    fun setUniform2f(name: String, v0: Float, v1: Float) {
        GLES20.glUniform2f(getUniformLocation(name), v0, v1)
    }

    fun release() {
        GLES20.glDeleteProgram(programId)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compile failed ($type): $log")
        }

        return shader
    }
}
