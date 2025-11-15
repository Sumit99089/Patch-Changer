package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.domain.repository.SampleRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val patchRepository: PatchRepository,
    private val sampleRepository: SampleRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): String {
        return patchRepository.exportToJson()
    }
}