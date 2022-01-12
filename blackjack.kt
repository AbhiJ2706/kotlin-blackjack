enum class Suits {
    Spades, Hearts, Diamonds, Clubs
}

enum class GameAction {
    NamePrompt, MoneyPrompt, BetPrompt, ActionPrompt, PInfo, Tie, PBust, DBust, DP21, D21, P21, PT, DT
}

class GameObserver() {
    var subject: Game? = null

    fun notify(act: GameAction, player: Human?=null) {
        when(act) {
            GameAction.NamePrompt -> print("Enter name: ")
            GameAction.MoneyPrompt -> print("Enter money (100- 1000): ")
            GameAction.BetPrompt -> print("Enter this round's bet (1- ${player?.money}): ")
            GameAction.ActionPrompt -> print("Enter action (hit | stay | hand): ")
            GameAction.PInfo -> print(player)
            GameAction.Tie -> println("It's a tie!")
            GameAction.PBust -> println("Player bust!")
            GameAction.DBust -> println("Dealer bust!")
            GameAction.DP21 -> println("Dealer and player have 21!")
            GameAction.D21 -> println("Dealer has 21!")
            GameAction.P21 -> println("Player has 21!")
            GameAction.PT -> println("Player has a higher total!")
            GameAction.DT -> println("Dealer has a higher total!")
        }
    }
}

class PlayerObserver(_subject: Human) {
    val subject: Human = _subject
}

class DeckObserver() {
    var subject: Deck? = null

    fun notify(param: String, c: Card) {
        println(param + " receives " + c)
    }
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

data class Deck(val temp: Int, var _obs: DeckObserver) {
    val tagsToVals: Map<String, out Int> = mapOf(
        "A" to 11, "2" to 2, "3" to 3,
        "4" to 4, "5" to 5, "6" to 6,
        "7" to 7, "8" to 8, "9" to 9,
        "T" to 10, "J" to 10, "Q" to 10,
        "K" to 10
    )
    var obs: DeckObserver = _obs
    var cards = ArrayList<Card>()
    init {
        obs.subject = this as Deck?
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
    }
    fun deal(player: Human, outCard: Boolean=false) {
        for (i in 0..player.hands.size-1) {
            val c: Card = cards.removeAt(0)
            if (outCard) obs.notify(player.tag, c)
            player.receiveCard(c, i)
        }
    }
    fun reset() {
        cards = ArrayList<Card>()
        build()
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

    open fun reset() {
        hands = ArrayList<Hand>()
        hands.add(Hand())
    }
}

class Player(name: String, amt: Int=100): Human(name, amt) {}

class Dealer(amt: Int=100000, deckObs: DeckObserver): Human("Dealer", amt) {
    var d: Deck = Deck(0, deckObs)
    override fun reset() {
        hands = ArrayList<Hand>()
        hands.add(Hand())
        d.reset()
    }
}

infix fun Dealer.deal(player: Human) {
    d.deal(player, outCard=(!(player === this)))
}

infix fun Human.wins(amt: Int) {
    this.money += amt
}

class Game(deckObs: DeckObserver, _obs: GameObserver) {
    var dealer: Dealer = Dealer(deckObs=deckObs)
    var player: Player
    var obs: GameObserver = _obs
    init {
        obs.subject = this
        player = run {
            obs.notify(GameAction.NamePrompt)
            val name: String? = readLine()
            val money: Int = run {
                var temp: Int = 0
                while (temp < 100 || temp > 1000) {
                    obs.notify(GameAction.MoneyPrompt)
                    temp = readLine()?.toInt() as Int
                }
                temp
            }
            Player(name as String, money)
        }
        while (player.money > 0) {
            gameLoop()
        }
    }
    fun gameLoop() {
        val bet: Int = run {
            var money: Int = 0
            while (money < 1 || money > player.money) {
                obs.notify(GameAction.BetPrompt, player)
                money = readLine()?.toInt() as Int
            }
            money
        }
        for (i in 1..2) dealer deal player
        for (i in 1..2) dealer deal dealer
        obs.notify(GameAction.PInfo, player)
        var winner: String = "None"
        while (winner == "None") {
            obs.notify(GameAction.ActionPrompt)
            val action: String = readLine() as String
            if (action == "hand") {
                obs.notify(GameAction.PInfo, player)
            } else if (action == "hit" && player.getScore() < 21) {
                dealer deal player
            } else if (action == "stay") {
                while (dealer.getScore() < 17) dealer deal dealer
                obs.notify(GameAction.PInfo, player)
                obs.notify(GameAction.PInfo, dealer)
                winner = determineWinner()
                when(winner) {
                    "Player" -> run {player wins bet}
                    "Dealer" -> run {dealer wins bet; player wins -bet}
                    else -> obs.notify(GameAction.Tie)
                }
            }
        }
        player.reset()
        dealer.reset()
    }
    fun determineWinner(): String {
        player.eval()
        dealer.eval()
        val ph: Int = player.getScore()
        val dh: Int = dealer.getScore()
        if (ph > 21) {
            obs.notify(GameAction.PBust)
            return "Dealer";
        } else if (dh > 21) {
            obs.notify(GameAction.DBust)
            return "Player";
        } else if (dh == 21 && ph == 21) {
            obs.notify(GameAction.DP21)
            return "No one";
        } else if (dh == 21) {
            obs.notify(GameAction.D21)
            return "Dealer";
        } else if (ph == 21) {
            obs.notify(GameAction.P21)
            return "Player";
        } else if (ph == dh) {
            return "No one";
        } else if (ph > dh) {
            obs.notify(GameAction.PT)
            return "Player";
        } else if (dh > ph) {
            obs.notify(GameAction.DT)
            return "Dealer";
        }
        return "None"
    }
}

fun main() {
    var x: DeckObserver = DeckObserver()
    var y: GameObserver = GameObserver()
    Game(x, y)
}