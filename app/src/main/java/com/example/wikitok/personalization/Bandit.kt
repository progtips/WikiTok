package com.example.wikitok.personalization

data class ArmState(var n: Int = 0, var rewardSum: Double = 0.0) {
    val mean: Double get() = if (n == 0) 0.0 else rewardSum / n
}

class EpsilonGreedy(private val epsilon: Double = 0.2) {
    private val arms = mutableMapOf<Topic, ArmState>()

    fun update(topic: Topic, reward: Double) {
        val s = arms.getOrPut(topic) { ArmState() }
        s.n++
        s.rewardSum += reward
    }

    fun score(topic: Topic): Double {
        val exploit = arms[topic]?.mean ?: 0.0
        val exploreBonus = if (Math.random() < epsilon) 1.0 else 0.0
        return exploit + exploreBonus * 0.01
    }
}


