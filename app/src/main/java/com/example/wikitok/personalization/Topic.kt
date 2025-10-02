package com.example.wikitok.personalization

import com.example.wikitok.data.Article

enum class Topic { SCIENCE, HISTORY, GEO, CULTURE, PEOPLE, TECH, SPORTS, OTHER }

object TopicClassifier {
    private val map = listOf(
        Topic.SCIENCE to listOf("наука","учёный","биология","физика","химия","эксперимент"),
        Topic.HISTORY to listOf("империя","война","династия","битва","история","революция"),
        Topic.GEO to listOf("город","страна","река","гора","остров","регион"),
        Topic.CULTURE to listOf("музей","искусство","литература","фильм","музыка","театр"),
        Topic.PEOPLE to listOf("родился","умер","персона","биография"),
        Topic.TECH to listOf("компьютер","технолог","инженер","алгоритм","программа","устройство"),
        Topic.SPORTS to listOf("футбол","спорт","олимпи","чемпион","матч")
    )

    fun classify(a: Article): Topic {
        val text = listOfNotNull(a.title, a.description, a.extract).joinToString(" ").lowercase()
        return map.firstOrNull { (_, kws) -> kws.any { it in text } }?.first ?: Topic.OTHER
    }
}


