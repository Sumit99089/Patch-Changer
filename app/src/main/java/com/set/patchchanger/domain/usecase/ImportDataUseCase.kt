package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.PatchRepository
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val patchRepository: PatchRepository
) {
    suspend operator fun invoke(jsonData: String): Boolean {
        return patchRepository.importFromJson(jsonData)
    }
}