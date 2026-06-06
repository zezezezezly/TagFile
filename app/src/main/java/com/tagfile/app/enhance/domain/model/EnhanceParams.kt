package com.tagfile.app.enhance.domain.model

data class EnhanceParams(
    val strength: Float = 0.5f,
    val sharpness: Float = 0.5f,
    val denoise: Float = 0.3f,
    val lineDarkening: Float = 0.5f,
    val contrast: Float = 0.3f,
    val saturation: Float = 0.2f,
    val upscaleFactor: Int = 1
) {
    init {
        require(strength in 0f..1f) { "strength must be in [0, 1]" }
        require(sharpness in 0f..1f) { "sharpness must be in [0, 1]" }
        require(denoise in 0f..1f) { "denoise must be in [0, 1]" }
        require(lineDarkening in 0f..1f) { "lineDarkening must be in [0, 1]" }
        require(contrast in 0f..1f) { "contrast must be in [0, 1]" }
        require(saturation in 0f..1f) { "saturation must be in [0, 1]" }
        require(upscaleFactor in 1..4) { "upscaleFactor must be in [1, 4]" }
    }

    companion object {
        val DEFAULT = EnhanceParams()

        val MANGA_PRESET = EnhanceParams(
            strength = 0.6f,
            sharpness = 0.6f,
            denoise = 0.4f,
            lineDarkening = 0.7f,
            contrast = 0.4f,
            saturation = 0.1f,
            upscaleFactor = 2
        )

        val ANIME_PRESET = EnhanceParams(
            strength = 0.5f,
            sharpness = 0.4f,
            denoise = 0.3f,
            lineDarkening = 0.4f,
            contrast = 0.2f,
            saturation = 0.3f,
            upscaleFactor = 2
        )

        val LIGHT_PRESET = EnhanceParams(
            strength = 0.3f,
            sharpness = 0.3f,
            denoise = 0.1f,
            lineDarkening = 0.3f,
            contrast = 0.1f,
            saturation = 0.1f,
            upscaleFactor = 1
        )
    }
}
