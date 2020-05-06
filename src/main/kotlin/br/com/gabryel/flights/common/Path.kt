package br.com.gabryel.flights.common

data class Path(val node: String, val nodeValue: Int, val next: Path? = null) {

    val price: Int = nodeValue + (next?.price?: 0)

    fun asList() = generateSequence(this) { it.next }.map(Path::node).toList().reversed()

    fun getFormattedPath(): String {
        val path = asList().joinToString(" - ")
        return "$path > \$$price"
    }
}