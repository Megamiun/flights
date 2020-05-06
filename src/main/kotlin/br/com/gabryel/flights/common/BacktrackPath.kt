package br.com.gabryel.flights.common

data class BacktrackPath(val node: String, val nodeValue: Int, val next: BacktrackPath? = null) {

    val price: Int = nodeValue + (next?.price?: 0)

    fun asList() = generateSequence(this) { it.next }.map(BacktrackPath::node).toList().reversed()

    fun getFormattedPath(): String {
        val path = asList().joinToString(" - ")
        return "$path > $price"
    }
}