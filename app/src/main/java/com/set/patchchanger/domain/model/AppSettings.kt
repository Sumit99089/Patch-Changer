package com.set.patchchanger.domain.model

enum class AppTheme(val id: String, val displayName: String) {
    BLACK("black", "Black"),
    WHITE("white", "White"),
    BLUE("blue", "Blue"),
    ORANGE("orange", "Orange"),
    YELLOW("yellow", "Yellow"),
    RED("red", "Red"),
    GREEN("green", "Green"),
    PURPLE("purple", "Purple"),
    TEAL("teal", "Teal");

    companion object {
        fun fromId(id: String): AppTheme {
            return values().find { it.id == id } ?: BLACK
        }
    }
}

data class AppSettings(
    val currentBankIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val currentMidiChannel: Int = 1,
    val currentTranspose: Int = 0,
    val theme: AppTheme = AppTheme.BLACK
)