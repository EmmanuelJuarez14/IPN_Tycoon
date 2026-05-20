package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository

/**
 * Recalcula alumnosTotales en GameState al final de cada ciclo.
 * Corre después de EconomyEngine (ResolutionOrder.STUDENTS).
 */
class EstudiantesEngine : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.STUDENTS

    override fun onResolveCycle(cycle: Int) {
        val mult = ReputacionEngine.multiplier(GameState.reputacion)
        val base = PropiedadRepository.propiedades.values
            .filter { it.comprada }
            .sumOf { it.baseAlumnos * it.nivel }
        GameState.alumnosTotales = (base * mult).toInt()
    }
}
