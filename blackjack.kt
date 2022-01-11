enum class Suits {
    Spades, Hearts, Diamonds, Clubs
}

data class Card(val suit: String, val worth: Int, val tag: String) {
    override fun equals(other: Any?) : Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Card

        return tag == other.tag
    }
}

data class Deck(val temp: Int) {
    val tagsToVals: Map<String, out Int> = mapOf(
        "A" to 11,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,
        "T" to 10,
        "J" to 10,
        "Q" to 10,
        "K" to 10
    )
    var cards = ArrayList<Card>()
    init {
        build()
    }
    fun build() {
        for (suit in Suits.values()) {
            for ((k, v) in tagsToVals) {
                var s: String = when(suit) {
                    Suits.Spades -> "S"
                    Suits.Hearts -> "H"
                    Suits.Diamonds -> "D"
                    Suits.Clubs -> "C"
                    else -> "N"
                }
                cards.add(Card(s, v, k))
            }
        }
    }
}

class Hand() {
    var cards = ArrayList<Card>()
    var size = 0
    constructor(c: Card) : this() {
        cards.add(c)
        size++
    }
    fun receiveCard(c: Card) {
        cards.add(c)
        size++
    }
    operator fun get(i: Int): Card {
        return cards[i]
    }
}

open class Human(name: String, amt: Int=100) {

    var tag: String = "Player"
    var hands: ArrayList<Hand> = ArrayList<Hand>()
    var money: Int = amt
    init {
        hands.add(Hand())
    }

    fun receiveCard(c: Card) {
        for (h in hands) {
            if (h.size == 1 && h[0] == c) {
                hands.add(Hand(c))
            }
        }
    }
}

class Player(name: String, amt: Int=100): Human(name, amt) {
    
}

class Dealer(name: String, amt: Int=100000): Human(name, amt) {
    
}

fun main() {
    var h = Human("Abhi", 200)
    println("${h.money}")
    var c = Card("S", 10, "T")
    println("${c.suit}, ${c.worth}")
    var d = Card("S", 10, "T")
    println("${c == d}")
}