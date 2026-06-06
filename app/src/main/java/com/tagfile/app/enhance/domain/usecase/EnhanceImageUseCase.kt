package com.tagfile.app.enhance.domain.usecase

import android.graphics.Bitmap
import com.tagfile.app.enhance.data.processor.Anime4KProcessor
import com.tagfile.app.enhance.domain.model.EnhanceParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhanceImageUseCase @Inject constructor(
    private val processor: Anime4KProcessor
) {
    suspend operator fun invoke(source: Bitmap, params: EnhanceParams): Bitmap {
        return processor.process(source, params)
    }
}
