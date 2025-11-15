package com.set.patchchanger.domain.model

data class Performance(
    val category: String,
    val bankName: String,
    val msb: Int,
    val lsb: Int,
    val pc: Int,
    val name: String
) {
    fun getKey(): String = "$msb:$lsb:$pc"
}