package com.set.patchchanger.ui.theme

import com.set.patchchanger.domain.model.ModxColor

// Color list from the video
fun getModxColors(): List<ModxColor> {
    return listOf(
        ModxColor("Black", "#333333"),
        ModxColor("Red", "#F44336"),
        ModxColor("Yellow", "#FFEB3B"),
        ModxColor("Green", "#4CAF50"),
        ModxColor("Blue", "#2196F3"),
        ModxColor("Azure", "#00BCD4"),
        ModxColor("Pink", "#E91E63"),
        ModxColor("Orange", "#FF9800"),
        ModxColor("Purple", "#9C27B0"),
        ModxColor("Sakura", "#F8BBD0"),
        ModxColor("Cream", "#FFECB3"),
        ModxColor("Lime", "#CDDC39"),
        ModxColor("Aqua", "#B2EBF2"),
        ModxColor("Beige", "#D7CCC8"),
        ModxColor("Mint", "#B2DFDB"),
        ModxColor("Lilac", "#D1C4E9")
    )
}

// Default colors for the 16 slots on a page
fun getDefaultColors(): List<String> {
    return getModxColors().map { it.hex }
}