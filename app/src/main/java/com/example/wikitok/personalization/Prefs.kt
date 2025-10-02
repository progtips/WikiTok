package com.example.wikitok.personalization

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val STORE_NAME = "wikitok_prefs"

val Context.dataStore by preferencesDataStore(name = STORE_NAME)

private fun meanKey(topic: Topic) = doublePreferencesKey("bandit_mean_${'$'}{topic.name}")
private fun nKey(topic: Topic) = intPreferencesKey("bandit_n_${'$'}{topic.name}")

suspend fun saveBandit(context: Context, snapshot: Map<Topic, ArmState>) {
    context.dataStore.edit { prefs: MutablePreferences ->
        snapshot.forEach { (topic, state) ->
            prefs[meanKey(topic)] = state.mean
            prefs[nKey(topic)] = state.n
        }
    }
}

suspend fun loadBandit(context: Context): Map<Topic, ArmState> {
    val prefs = context.dataStore.data.first()
    val map = mutableMapOf<Topic, ArmState>()
    Topic.values().forEach { topic ->
        val n = prefs[nKey(topic)] ?: 0
        val mean = prefs[meanKey(topic)] ?: 0.0
        if (n > 0 || mean > 0.0) {
            map[topic] = ArmState(n = n, rewardSum = mean * n)
        }
    }
    return map
}


