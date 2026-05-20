package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository

/**
 * Gestiona la reputación del plantel (0–100) cada ciclo.
 *
 * Target = proporción de niveles obtenidos respecto al máximo posible (43 niveles en total).
 * La reputación se acerca al target lentamente: ±3 si la brecha es grande, ±1 si es pequeña.
 *
 * El multiplicador que exponen [multiplier] se aplica en EconomyEngine y EstudiantesEngine:
 *   rep 0  → 0.50x  (crisis: la mitad de alumnos e ingresos)
 *   rep 50 → 1.00x  (neutral)
 *   rep 100→ 1.50x  (prestigio máximo)
 */
class ReputacionEngine : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.REPUTATION

    companion object {
        private const val MAX_NIVELES = 43

        fun multiplier(reputacion: Int): Double = 0.5 + reputacion / 100.0
    }

    override fun onResolveCycle(cycle: Int) {
        val totalNiveles = PropiedadRepository.propiedades.values
            .filter { it.comprada }
            .sumOf { it.nivel }

        val targetRep = (totalNiveles * 100 / MAX_NIVELES).coerceIn(0, 100)
        val gap = targetRep - GameState.reputacion

        val delta = when {
            gap > 10  ->  3
            gap > 0   ->  1
            gap < -10 -> -3
            gap < 0   -> -1
            else      ->  0
        }

        GameState.reputacion = (GameState.reputacion + delta).coerceIn(0, 100)
    }
}
