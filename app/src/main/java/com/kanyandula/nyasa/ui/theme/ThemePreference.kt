package com.kanyandula.nyasa.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences"
)

object ThemePreferenceManager {

    private val THEME_KEY = stringPreferencesKey("theme_preference")

    fun themeFlow(dataStore: DataStore<Preferences>): Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            val name = prefs[THEME_KEY] ?: ThemePreference.SYSTEM.name
            ThemePreference.valueOf(name)
        }

    suspend fun setTheme(
        dataStore: DataStore<Preferences>,
        preference: ThemePreference
    ) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = preference.name
        }
    }
}
