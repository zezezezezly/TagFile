package com.tagfile.app.enhance.data.processor.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import com.tagfile.app.enhance.domain.model.EnhanceParams
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GpuProcessor {

    private var eglCore: EglCore? = null
    private var drawProgram: GlProgram? = null
    private var denoiseProgram: GlProgram? = null
    private var boxBlurProgram: GlProgram? = null
    private var unsharpProgram: GlProgram? = null
    private var lineDarkenProgram: GlProgram? = null
    private var luminanceProgram: GlProgram? = null
    private var colorAdjustProgram: GlProgram? = null

    private var quadVbo: Int = 0
    private var currentWidth: Int = 0
    private var currentHeight: Int = 0

    private val quadVertices = floatArrayOf(
        -1f, -1f, 0f, 0f,
         1f, -1f, 1f, 0f,
        -1f,  1f, 0f, 1f,
         1f,  1f, 1f, 1f
    )

    @Synchronized
    fun process(source: Bitmap, params: EnhanceParams): Bitmap {
        if (eglCore == null) {
            eglCore = EglCore()
            eglCore!!.initialize()
        }

        val w = source.width
        val h = source.height
        currentWidth = w
        currentHeight = h

        var activeSurface = eglCore!!.createOffscreenSurface(w, h)
        eglCore!!.makeCurrent(activeSurface)

        if (drawProgram == null) {
            initGlResources()
        }

        GLES20.glViewport(0, 0, w, h)

        var inputTex = createTextureFromBitmap(source)

        if (params.upscaleFactor > 1 && params.upscaleFactor <= 4) {
            val factor = params.upscaleFactor
            val upW = minOf(w * factor, 2048)
            val upH = minOf(h * factor, 2048)
            val upSurface = eglCore!!.createOffscreenSurface(upW, upH)
            eglCore!!.destroySurface(activeSurface)
            activeSurface = upSurface
            eglCore!!.makeCurrent(activeSurface)
            GLES20.glViewport(0, 0, upW, upH)

            val upTex = createEmptyTexture(upW, upH)
            renderFullscreenQuad(upTex, upW, upH, drawProgram!!) { prog ->
                bindTexture(0, inputTex, "uTexture")
            }
            deleteTexture(inputTex)
            inputTex = upTex
            currentWidth = upW
            currentHeight = upH
        }

        var texA = inputTex
        var texB = createEmptyTexture(currentWidth, currentHeight)

        if (params.denoise > 0.01f) {
            val rangeSigma = 40f + (1f - params.denoise) * 70f
            renderFullscreenQuad(texB, currentWidth, currentHeight, denoiseProgram!!) { prog ->
                prog.setUniform1f("uStrength", params.denoise)
                prog.setUniform1f("uRangeSigma", rangeSigma)
                prog.setUniform2f("uTexelSize", 1f / currentWidth, 1f / currentHeight)
                bindTexture(0, texA, "uTexture")
            }
            val tmp = texA; texA = texB; texB = tmp
        }

        if (params.lineDarkening > 0.01f) {
            val blurRadius = maxOf(1, (params.lineDarkening * 4f).toInt())
            val threshold = 0.157f + (1f - params.lineDarkening) * 0.235f

            val lumTex = createEmptyTexture(currentWidth, currentHeight)
            renderFullscreenQuad(lumTex, currentWidth, currentHeight, luminanceProgram!!) { prog ->
                bindTexture(0, texA, "uTexture")
            }

            val blurTemp = createEmptyTexture(currentWidth, currentHeight)
            val blurredLum = createEmptyTexture(currentWidth, currentHeight)
            renderBoxBlur(lumTex, blurTemp, blurredLum, currentWidth, currentHeight, blurRadius)

            renderFullscreenQuad(texB, currentWidth, currentHeight, lineDarkenProgram!!) { prog ->
                prog.setUniform1f("uStrength", params.lineDarkening)
                prog.setUniform1f("uThreshold", threshold)
                bindTexture(0, texA, "uTexture")
                bindTexture(1, blurredLum, "uBlurredLum")
            }

            deleteTexture(lumTex)
            deleteTexture(blurTemp)
            deleteTexture(blurredLum)
            val tmp = texA; texA = texB; texB = tmp
        }

        if (params.contrast > 0.01f || params.saturation > 0.01f) {
            renderFullscreenQuad(texB, currentWidth, currentHeight, colorAdjustProgram!!) { prog ->
                prog.setUniform1f("uContrast", params.contrast)
                prog.setUniform1f("uSaturation", params.saturation)
                bindTexture(0, texA, "uTexture")
            }
            val tmp = texA; texA = texB; texB = tmp
        }

        if (params.sharpness > 0.01f) {
            val amount = params.sharpness * 1.5f * (0.5f + params.strength * 0.5f)
            val edgeThreshold = (1f - params.sharpness) * 0.04f
            val blurRadius = maxOf(1, ((1f - params.sharpness) * 4f).toInt() + 1)

            val blurTemp = createEmptyTexture(currentWidth, currentHeight)
            val blurred = createEmptyTexture(currentWidth, currentHeight)
            renderBoxBlur(texA, blurTemp, blurred, currentWidth, currentHeight, blurRadius)

            renderFullscreenQuad(texB, currentWidth, currentHeight, unsharpProgram!!) { prog ->
                prog.setUniform1f("uAmount", amount)
                prog.setUniform1f("uThreshold", edgeThreshold)
                bindTexture(0, texA, "uTexture")
                bindTexture(1, blurred, "uBlurred")
            }

            deleteTexture(blurTemp)
            deleteTexture(blurred)
            val tmp = texA; texA = texB; texB = tmp
        }

        val result = readTextureToBitmap(texA, currentWidth, currentHeight)

        deleteTexture(texA)
        deleteTexture(texB)

        eglCore!!.destroySurface(activeSurface)

        return result
    }

    @Synchronized
    fun release() {
        drawProgram?.release()
        denoiseProgram?.release()
        boxBlurProgram?.release()
        unsharpProgram?.release()
        lineDarkenProgram?.release()
        luminanceProgram?.release()
        colorAdjustProgram?.release()
        if (quadVbo != 0) {
            val buffers = intArrayOf(quadVbo)
            GLES20.glDeleteBuffers(1, buffers, 0)
            quadVbo = 0
        }
        eglCore?.release()
        eglCore = null
    }

    private fun initGlResources() {
        drawProgram = GlProgram(GlShaders.VERTEX_SHADER, BASIC_FRAGMENT)
        denoiseProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.DENOISE_FRAGMENT)
        boxBlurProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.BOX_BLUR_FRAGMENT)
        unsharpProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.UNSHARP_MASK_FRAGMENT)
        lineDarkenProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.LINE_DARKEN_FRAGMENT)
        luminanceProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.LUMINANCE_FRAGMENT)
        colorAdjustProgram = GlProgram(GlShaders.VERTEX_SHADER, GlShaders.COLOR_ADJUST_FRAGMENT)

        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        quadVbo = vbo[0]

        val buffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVertices)
                position(0)
            }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, quadVbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, quadVertices.size * 4, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    private fun renderFullscreenQuad(
        outputTex: Int, width: Int, height: Int,
        program: GlProgram,
        setupUniforms: (GlProgram) -> Unit
    ) {
        val fb = createFramebuffer()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, outputTex, 0
        )
        GLES20.glViewport(0, 0, width, height)

        program.use()
        setupQuadAttributes(program.programId)
        setupUniforms(program)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        deleteFramebuffer(fb)
    }

    private fun renderBoxBlur(
        sourceTex: Int, tempTex: Int, outputTex: Int,
        width: Int, height: Int, radius: Int
    ) {
        renderFullscreenQuad(tempTex, width, height, boxBlurProgram!!) { prog ->
            prog.setUniform1i("uRadius", radius)
            prog.setUniform1i("uHorizontal", 1)
            prog.setUniform2f("uTexelSize", 1f / width, 1f / height)
            bindTexture(0, sourceTex, "uTexture")
        }

        renderFullscreenQuad(outputTex, width, height, boxBlurProgram!!) { prog ->
            prog.setUniform1i("uRadius", radius)
            prog.setUniform1i("uHorizontal", 0)
            prog.setUniform2f("uTexelSize", 1f / width, 1f / height)
            bindTexture(0, tempTex, "uTexture")
        }
    }

    private fun setupQuadAttributes(programId: Int) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, quadVbo)
        val posLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 2, GLES20.GL_FLOAT, false, 16, 0)

        val texLoc = GLES20.glGetAttribLocation(programId, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texLoc)
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 16, 8)
    }

    private fun bindTexture(unit: Int, textureId: Int, uniformName: String) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        val progId = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, progId, 0)
        if (progId[0] > 0) {
            GLES20.glUniform1i(GLES20.glGetUniformLocation(progId[0], uniformName), unit)
        }
    }

    private fun createTextureFromBitmap(bitmap: Bitmap): Int {
        val texId = intArrayOf(0)
        GLES20.glGenTextures(1, texId, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        val buffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.position(0)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            bitmap.width, bitmap.height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer
        )

        return texId[0]
    }

    private fun createEmptyTexture(width: Int, height: Int): Int {
        val texId = intArrayOf(0)
        GLES20.glGenTextures(1, texId, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            width, height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        return texId[0]
    }

    private fun createFramebuffer(): Int {
        val fbs = IntArray(1)
        GLES20.glGenFramebuffers(1, fbs, 0)
        return fbs[0]
    }

    private fun deleteFramebuffer(fb: Int) {
        GLES20.glDeleteFramebuffers(1, intArrayOf(fb), 0)
    }

    private fun deleteTexture(texId: Int) {
        GLES20.glDeleteTextures(1, intArrayOf(texId), 0)
    }

    private fun readTextureToBitmap(textureId: Int, width: Int, height: Int): Bitmap {
        val fb = createFramebuffer()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textureId, 0
        )

        val buffer = ByteBuffer.allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        buffer.position(0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        deleteFramebuffer(fb)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    companion object {
        private val BASIC_FRAGMENT = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()
    }
}
