package io.moviles.IPN_Tycoon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel {
    private val _selectedBuilding = MutableStateFlow<Propiedad?>(null)
    val selectedBuilding = _selectedBuilding.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu = _showMenu.asStateFlow()

    // --- PROPIEDADES DEL JUEGO (Necesarias para GameScreen) ---
    private val _dinero = MutableStateFlow(GameState.dinero)
    val dinero = _dinero.asStateFlow()

    private val _nivel = MutableStateFlow(1)
    val nivel = _nivel.asStateFlow()

    private val _popularidad = MutableStateFlow(50)
    val popularidad = _popularidad.asStateFlow()

    fun onBuildingClicked(propiedad: Propiedad) {
        _selectedBuilding.value = propiedad
        _showMenu.value = true
    }

    fun closeMenu() {
        _showMenu.value = false
        _selectedBuilding.value = null
    }

    /**
     * Actualiza los valores del estado.
     */
    fun updateState(nuevoDinero: Long, nuevoNivel: Int, nuevaPopularidad: Int) {
        _dinero.value = nuevoDinero
        _nivel.value = nuevoNivel
        _popularidad.value = nuevaPopularidad
    }
}
