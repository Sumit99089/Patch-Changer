package com.set.patchchanger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bank(
    val name: String,
    val pages: List<Page>
) : Parcelable

@Parcelize
data class Page(
    val slots: List<PatchSlot>
) : Parcelable

data class PatchData(
    val banks: List<Bank>,
    val bankNames: List<String>,
    val pageNames: List<String>
)