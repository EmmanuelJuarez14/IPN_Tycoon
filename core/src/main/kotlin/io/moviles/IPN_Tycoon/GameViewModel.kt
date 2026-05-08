package io.moviles.IPN_Tycoon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel {
    // Estadísticas del jugador
    private val _dinero = MutableStateFlow(150000L) // Empieza con algo de presupuesto
    val dinero = _dinero.asStateFlow()

    private val _nivel = MutableStateFlow(1)
    val nivel = _nivel.asStateFlow()

    private val _popularidad = MutableStateFlow(10) // 0-100%
    val popularidad = _popularidad.asStateFlow()

    // Gestión de edificios
    private val _selectedBuilding = MutableStateFlow<Propiedad?>(null)
    val selectedBuilding = _selectedBuilding.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu = _showMenu.asStateFlow()

    fun onBuildingClicked(propiedad: Propiedad) {
        _selectedBuilding.value = propiedad
        _showMenu.value = true
    }

    fun closeMenu() {
        _showMenu.value = false
        _selectedBuilding.value = null
    }

    fun sumarDinero(cantidad: Long) {
        _dinero.value += cantidad
    }

    fun restarDinero(cantidad: Long): Boolean {
        if (_dinero.value >= cantidad) {
            _dinero.value -= cantidad
            return true
        }
        return false
    }
}
