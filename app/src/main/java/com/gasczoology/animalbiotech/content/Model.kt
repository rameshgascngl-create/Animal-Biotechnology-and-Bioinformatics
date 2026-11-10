package com.gasczoology.animalbiotech.content

/** Review state of a Tamil string. Everything starts as DRAFT until the owner reviews it. */
enum class TaStatus { DRAFT, REVIEWED, BRACKETED }

/** A bilingual text value. [ta] is empty when no Tamil has been authored yet. */
data class Bi(
    val en: String,
    val ta: String = "",
    val taStatus: TaStatus = TaStatus.DRAFT,
)
