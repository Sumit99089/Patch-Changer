package com.set.patchchanger.ui.theme

import com.set.patchchanger.domain.model.ModxColor

// Color list matching the specific grid in the screenshot
fun getModxColors(): List<ModxColor> {
    return listOf(
        // Row 1
        ModxColor("Charcoal", "#2B2B2B"), // 1
        ModxColor("Red", "#F44336"),      // 2
        ModxColor("Yellow", "#FFEB3B"),   // 3
        ModxColor("Green", "#4CAF50"),    // 4
        // Row 2
        ModxColor("Blue", "#2196F3"),     // 5
        ModxColor("Cyan", "#00BCD4"),     // 6
        ModxColor("Pink", "#E91E63"),     // 7
        ModxColor("Orange", "#FF9800"),   // 8
        // Row 3
        ModxColor("Purple", "#9C27B0"),   // 9
        ModxColor("Sakura", "#F8BBD0"),   // 10
        ModxColor("Cream", "#FFF59D"),    // 11
        ModxColor("Lime", "#CDDC39"),     // 12
        // Row 4
        ModxColor("Ice", "#B2EBF2"),      // 13
        ModxColor("Beige", "#D7CCC8"),    // 14
        ModxColor("Mint", "#B2DFDB"),     // 15
        ModxColor("Lavender", "#D1C4E9")  // 16
    )
}

// Default colors for the 16 slots on a page
fun getDefaultColors(): List<String> {
    return getModxColors().map { it.hex }
}