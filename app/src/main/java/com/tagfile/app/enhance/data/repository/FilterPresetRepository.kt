package com.tagfile.app.enhance.data.repository

import com.tagfile.app.data.local.dao.FilterPresetDao
import com.tagfile.app.data.local.entity.FilterPresetEntity
import com.tagfile.app.enhance.domain.model.EnhanceParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class FilterPreset(
    val id: Long,
    val name: String,
    val params: EnhanceParams,
    val createdAt: Long
)

@Singleton
class FilterPresetRepository @Inject constructor(
    private val dao: FilterPresetDao
) {
    fun getAll(): Flow<List<FilterPreset>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getById(id: Long): FilterPreset? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getParamsById(id: Long): EnhanceParams? {
        val entity = dao.getById(id) ?: return null
        return EnhanceParams(
            strength = entity.strength,
            sharpness = entity.sharpness,
            denoise = entity.denoise,
            lineDarkening = entity.lineDarkening,
            contrast = entity.contrast,
            saturation = entity.saturation,
            upscaleFactor = entity.upscaleFactor
        )
    }

    suspend fun create(name: String, params: EnhanceParams): Long {
        return dao.insert(FilterPresetEntity(
            name = name,
            strength = params.strength,
            sharpness = params.sharpness,
            denoise = params.denoise,
            lineDarkening = params.lineDarkening,
            contrast = params.contrast,
            saturation = params.saturation,
            upscaleFactor = params.upscaleFactor
        ))
    }

    suspend fun update(id: Long, name: String, params: EnhanceParams) {
        dao.update(FilterPresetEntity(
            id = id,
            name = name,
            strength = params.strength,
            sharpness = params.sharpness,
            denoise = params.denoise,
            lineDarkening = params.lineDarkening,
            contrast = params.contrast,
            saturation = params.saturation,
            upscaleFactor = params.upscaleFactor
        ))
    }

    suspend fun delete(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
    }

    private fun FilterPresetEntity.toDomain() = FilterPreset(
        id = id,
        name = name,
        params = EnhanceParams(
            strength = strength,
            sharpness = sharpness,
            denoise = denoise,
            lineDarkening = lineDarkening,
            contrast = contrast,
            saturation = saturation,
            upscaleFactor = upscaleFactor
        ),
        createdAt = createdAt
    )
}
