package com.tagfile.app.enhance.data.processor.gl

object GlShaders {

    val VERTEX_SHADER = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    val DENOISE_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform vec2 uTexelSize;
        uniform float uStrength;
        uniform float uRangeSigma;

        vec3 rgb2hsv(vec3 c) {
            vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
            vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
            vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
            float d = q.x - min(q.w, q.y);
            float e = 1.0e-10;
            return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
        }

        vec3 hsv2rgb(vec3 c) {
            vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
            vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }

        void main() {
            vec3 center = texture2D(uTexture, vTexCoord).rgb;
            vec3 centerHsv = rgb2hsv(center);

            int radius = int(1.0 + uStrength * 3.0);
            float sigma = float(radius) / 3.0;
            float rangeSigma = 40.0 + (1.0 - uStrength) * 70.0;

            vec3 sum = vec3(0.0);
            float weightSum = 0.0;

            for (int dy = -3; dy <= 3; dy++) {
                for (int dx = -3; dx <= 3; dx++) {
                    if (abs(dx) > radius || abs(dy) > radius) continue;
                    vec2 offset = vec2(float(dx), float(dy)) * uTexelSize * 1.5;
                    vec3 neighbor = texture2D(uTexture, vTexCoord + offset).rgb;
                    vec3 neighHsv = rgb2hsv(neighbor);

                    float spatialW = exp(-float(dx*dx + dy*dy) / (2.0 * sigma * sigma));
                    float colorDist = length(neighHsv - centerHsv) * 255.0;
                    float rangeW = exp(-colorDist * colorDist / (2.0 * rangeSigma * rangeSigma));
                    float w = spatialW * rangeW;

                    sum += neighbor * w;
                    weightSum += w;
                }
            }

            vec3 result = sum / max(weightSum, 0.001);
            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    val BOX_BLUR_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform vec2 uTexelSize;
        uniform int uRadius;
        uniform bool uHorizontal;

        void main() {
            vec4 sum = vec4(0.0);
            float count = 0.0;
            int r = uRadius;

            for (int i = -5; i <= 5; i++) {
                if (i > r || i < -r) continue;
                vec2 offset = uHorizontal
                    ? vec2(float(i) * uTexelSize.x, 0.0)
                    : vec2(0.0, float(i) * uTexelSize.y);
                sum += texture2D(uTexture, vTexCoord + offset);
                count += 1.0;
            }

            gl_FragColor = sum / count;
        }
    """.trimIndent()

    val UNSHARP_MASK_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform sampler2D uBlurred;
        uniform float uAmount;
        uniform float uThreshold;

        void main() {
            vec3 original = texture2D(uTexture, vTexCoord).rgb;
            vec3 blurred = texture2D(uBlurred, vTexCoord).rgb;
            vec3 diff = original - blurred;

            float edgeStrength = length(diff);
            float edgeMask = edgeStrength > uThreshold ? 1.0 : edgeStrength / max(uThreshold, 0.01);

            float adjustedAmount = uAmount * edgeMask;
            vec3 result = original + diff * adjustedAmount;
            result = clamp(result, 0.0, 1.0);
            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    val LINE_DARKEN_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform sampler2D uBlurredLum;
        uniform float uStrength;
        uniform float uThreshold;

        float luminance(vec3 c) {
            return dot(c, vec3(0.299, 0.587, 0.114));
        }

        void main() {
            vec3 color = texture2D(uTexture, vTexCoord).rgb;
            float blurredLum = texture2D(uBlurredLum, vTexCoord).r;
            float pixelLum = luminance(color);
            float darkenFactor = 0.3 + uStrength * 0.7;

            if (pixelLum < blurredLum * 0.85 && pixelLum < uThreshold) {
                float alpha = ((blurredLum - pixelLum) / max(blurredLum, 0.004)) * uStrength;
                float adjustedAlpha = alpha * darkenFactor;
                vec3 result = color * (1.0 - adjustedAlpha);
                gl_FragColor = vec4(result, 1.0);
            } else {
                gl_FragColor = vec4(color, 1.0);
            }
        }
    """.trimIndent()

    val LUMINANCE_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;

        void main() {
            vec3 color = texture2D(uTexture, vTexCoord).rgb;
            float lum = dot(color, vec3(0.299, 0.587, 0.114));
            gl_FragColor = vec4(lum, lum, lum, 1.0);
        }
    """.trimIndent()

    val COLOR_ADJUST_FRAGMENT = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform float uContrast;
        uniform float uSaturation;

        vec3 rgb2hsv(vec3 c) {
            vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
            vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
            vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
            float d = q.x - min(q.w, q.y);
            float e = 1.0e-10;
            return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
        }

        vec3 hsv2rgb(vec3 c) {
            vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
            vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }

        void main() {
            vec3 color = texture2D(uTexture, vTexCoord).rgb;

            float contrastFactor = 1.0 + uContrast * 0.8;
            vec3 contrasted = (color - 0.5) * contrastFactor + 0.5;
            contrasted = clamp(contrasted, 0.0, 1.0);

            vec3 hsv = rgb2hsv(contrasted);
            float saturationFactor = 1.0 + uSaturation * 1.5;
            hsv.y = clamp(hsv.y * saturationFactor, 0.0, 1.0);
            vec3 result = hsv2rgb(hsv);

            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()
}
