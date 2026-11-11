package com.gasczoology.animalbiotech.content

/** Review state of a Tamil string. Everything starts as DRAFT until the owner reviews it. */
enum class TaStatus { DRAFT, REVIEWED, BRACKETED }

/** A bilingual text value. [ta] is empty when no Tamil has been authored yet. */
data class Bi(
    val en: String,
    val ta: String = "",
    val taStatus: TaStatus = TaStatus.DRAFT,
)

/** One renderable unit of chapter content. */
sealed interface Block

data class Paragraph(val text: Bi) : Block
data class Bullets(val items: List<Bi>, val ordered: Boolean) : Block
data class Table(val header: List<Bi>, val rows: List<List<Bi>>) : Block

enum class CalloutKind { INFO, WARN, TIP, EXAM }

data class Callout(val kind: CalloutKind, val text: Bi) : Block
data class KeyTerm(val term: Bi, val definition: Bi) : Block
data class Diagram(val id: String, val caption: Bi) : Block
data class Steps(val items: List<Bi>) : Block
data class Reveal(val question: Bi, val answer: Bi) : Block
data class Summary(val items: List<Bi>) : Block
data class GlossaryEntry(val term: Bi, val definition: Bi) : Block

/** Content the extractor or parser could not structure. Shown with a visible warning, never dropped. */
data class Unparsed(val text: String, val source: String, val selector: String) : Block

data class Section(val id: String, val title: Bi, val blocks: List<Block>)
data class Chapter(val id: String, val unit: String, val title: Bi, val sections: List<Section>)

data class ParseResult(val chapter: Chapter?, val warnings: List<String>)
