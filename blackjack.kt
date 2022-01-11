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
    override fun toString(): String = tag + suit
}

data class Deck(val temp: Int) {
    val tagsToVals: Map<String, out Int> = mapOf(
        "A" to 11, "2" to 2, "3" to 3,
        "4" to 4, "5" to 5, "6" to 6,
        "7" to 7, "8" to 8, "9" to 9,
        "T" to 10, "J" to 10, "Q" to 10,
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
                }
                cards.add(Card(s, v, k))
            }
        }
        cards.shuffle()
        println(cards.joinToString(separator=" "))
    }
    fun deal(player: Human) {
        for (i in 0..player.hands.size-1) {
            val c: Card = cards.removeAt(0)
            player.receiveCard(c, i)
        }
    }
}

class Hand() {
    var cards = ArrayList<Card>()
    var size = 0
    var score = 0
    constructor(c: Card) : this() {
        cards.add(c)
        size++
    }
    fun receiveCard(c: Card) {
        cards.add(c)
        size++
        eval()
    }
    fun eval() {
        score = 0
        for (c in cards) {
            if (c.tag == "A") continue
            else score += c.worth
        }
        for (c in cards) {
            if (c.tag != "A") continue
            else {
                if (score + 11 <= 21) score += 11
                else score++
            }
        }
    }
    operator fun get(i: Int): Card {
        return cards[i]
    }
    override fun toString(): String {
        return this.cards.joinToString(separator = ", ")
    }
}

open class Human(name: String="Player", amt: Int=100) {

    var tag: String = name
    var hands: ArrayList<Hand> = ArrayList<Hand>()
    var money: Int = amt
    init {
        hands.add(Hand())
    }

    fun receiveCard(c: Card, which_hand: Int=0) {
        for (h in hands) {
            if (h.size == 1 && h[0] == c) {
                hands.add(Hand(c))
                return
            }
        }
        hands[which_hand].receiveCard(c)
    }

    fun getScore(): Int {
        return hands.minByOrNull{ it.score }?.score as Int
    }

    fun eval() {
        for (h in hands) h.eval()
    }

    override fun toString(): String {
        var ret: String = tag + "'s cards:\n"
        for (i in 0..hands.size-1) {
            ret += ("Hand " + (i + 1) + ": (value " + hands[i].score + ") " + hands[i] + "\n")
        }
        return ret
    }
}

class Player(name: String, amt: Int=100): Human(name, amt) {}

class Dealer(amt: Int=100000): Human("Dealer", amt) {
    var d: Deck = Deck(0)
}

infix fun Dealer.deal(player: Human) {
    d.deal(player)
}

class Game() {
    var dealer: Dealer = Dealer()
    var player: Player
    init {
        print("Enter name: ")
        val name: String? = readLine()
        print("Enter money: ")
        val amt: String? = readLine()
        player = Player(name as String, amt?.toInt() as Int)
        gameLoop()
    }
    fun gameLoop() {
        for (i in 1..2) {
            dealer deal player
            dealer deal dealer
        }
        print(player)
        var winner: String = "None"
        while (winner == "None") {
            print("Enter action (hit | stay | hand): ")
            val action: String = readLine() as String
            if (action == "hand") {
                print(player)
            } else if (action == "hit") {
                dealer deal player
                dealer deal dealer
            } else if (action == "stay") {
                print(player)
                print(dealer)
                winner = determineWinner()
            }
        }
    }
    fun determineWinner(): String {
        player.eval()
        dealer.eval()
        val ph: Int = player.getScore()
        val dh: Int = dealer.getScore()
        if (ph > 21) {
            println("Player bust!")
            return "Dealer";
        } else if (dh > 21) {
            println("Dealer bust!")
            return "Player";
        } else if (dh == 21 && ph == 21) {
            println("Dealer and player have 21!")
            return "No one";
        } else if (dh == 21) {
            println("Dealer has 21!")
            return "Dealer";
        } else if (ph == 21) {
            println("Player has 21!")
            return "Player";
        } else if (ph == dh) {
            println("Tie!")
            return "No one";
        } else if (ph > dh) {
            println("Player has a higher total!")
            return "Player";
        } else if (dh > ph) {
            println("Dealer has a higher total!")
            return "Dealer";
        }
        return "None"
    }
}

fun main() {
    println("compiled!")
    var h = Human("Abhi", 200)
    println("${h.money}")
    var c = Card("S", 10, "T")
    println("${c.suit}, ${c.worth}")
    println("${c}")
    var d = Card("S", 10, "T")
    println("${c == d}")
    val g: Game = Game()
}