package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.model.Performance
import javax.inject.Inject

class GetPerformancesUseCase @Inject constructor() {

    private val performanceData = mapOf(
        "For Montage (Single)" to PerformanceCategory(
            msb = 63,
            banks = (0..15).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (16..20).map { PerformanceBank("User ${it - 15}", it, 128) } +
                    (24..63).map { PerformanceBank("Library ${it - 23}", it, 128) }
        ),
        "For Montage (Multi)" to PerformanceCategory(
            msb = 63,
            banks = (64..79).map { PerformanceBank("Preset ${it - 63}", it, 128) } +
                    (80..84).map { PerformanceBank("User ${it - 79}", it, 128) } +
                    (88..127).map { PerformanceBank("Library ${it - 87}", it, 128) }
        ),
        "For ModX / ModX+ (Single)" to PerformanceCategory(
            msb = 63,
            banks = (0..31).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (32..36).map { PerformanceBank("User ${it - 31}", it, 128) } +
                    (40..79).map { PerformanceBank("Library ${it - 39}", it, 128) }
        ),
        "For ModX / ModX+ (Multi)" to PerformanceCategory(
            msb = 64,
            banks = (0..31).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (32..36).map { PerformanceBank("User ${it - 31}", it, 128) } +
                    (40..79).map { PerformanceBank("Library ${it - 39}", it, 128) }
        ),
        "For MODX M / Montage M (Single) - Presets" to PerformanceCategory(
            msb = 63,
            banks = (0..39).map { PerformanceBank("Preset ${it + 1}", it, 128) }
        ),
        "For MODX M / Montage M (Single) - User/Lib" to PerformanceCategory(
            msb = 64,
            banks = (0..4).map { PerformanceBank("User ${it + 1}", it, 128) } +
                    (8..87).map { PerformanceBank("Library ${it - 7}", it, 128) }
        ),
        "For MODX M / Montage M (Multi) - Presets" to PerformanceCategory(
            msb = 65,
            banks = (0..39).map { PerformanceBank("Preset ${it + 1}", it, 128) }
        ),
        "For MODX M / Montage M (Multi) - User/Lib" to PerformanceCategory(
            msb = 66,
            banks = (0..4).map { PerformanceBank("User ${it + 1}", it, 128) } +
                    (8..87).map { PerformanceBank("Library ${it - 7}", it, 128) }
        ),
        "For GM Voice" to PerformanceCategory(
            msb = 0,
            banks = listOf(PerformanceBank("GM Voice", 0, 128))
        ),
        "For GM Drum Voice" to PerformanceCategory(
            msb = 127,
            banks = listOf(PerformanceBank("GM Drum Voice", 0, 128))
        )
    )

    fun getCategories(): List<String> = performanceData.keys.toList()

    fun getBanks(category: String): List<PerformanceBank> {
        return performanceData[category]?.banks ?: emptyList()
    }

    operator fun invoke(category: String, bankIndex: Int): List<Performance> {
        val categoryData = performanceData[category] ?: return emptyList()
        val bank = categoryData.banks.getOrNull(bankIndex) ?: return emptyList()

        return (0 until bank.pcCount).map { pc ->
            Performance(
                category = category,
                bankName = bank.name,
                msb = categoryData.msb,
                lsb = bank.lsb,
                pc = pc,
                name = "${bank.name} - ${(pc + 1).toString().padStart(3, '0')}"
            )
        }
    }

    fun search(query: String): List<Performance> {
        val results = mutableListOf<Performance>()
        val lowerQuery = query.lowercase()

        performanceData.forEach { (category, categoryData) ->
            categoryData.banks.forEach { bank ->
                (0 until bank.pcCount).forEach { pc ->
                    val name = "${bank.name} - ${(pc + 1).toString().padStart(3, '0')}"
                    if (name.lowercase().contains(lowerQuery) ||
                        (pc + 1).toString().contains(lowerQuery)
                    ) {
                        results.add(
                            Performance(
                                category = category,
                                bankName = bank.name,
                                msb = categoryData.msb,
                                lsb = bank.lsb,
                                pc = pc,
                                name = name
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    data class PerformanceCategory(
        val msb: Int,
        val banks: List<PerformanceBank>
    )

    data class PerformanceBank(
        val name: String,
        val lsb: Int,
        val pcCount: Int
    )
}