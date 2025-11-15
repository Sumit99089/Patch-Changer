package com.set.patchchanger.domain.model

data class SearchResult(
    val slot: PatchSlot,
    val bankIndex: Int,
    val pageIndex: Int,
    val bankName: String,
    val pageName: String
)