package io.moviles.IPN_Tycoon

import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*

/**
 * Ventana de gestión de un edificio.
 *
 * Flujo:
 *  • Si [data.comprada] == false  → muestra precio de compra, botón "COMPRAR"
 *  • Si [data.comprada] == true y [data.nivel] < [data.mejoraMax]
 *                                 → muestra costo de mejora, botón "MEJORAR"
 *  • Si [data.nivel] == [data.mejoraMax] → botón deshabilitado "NIVEL MÁXIMO"
 *
 * Al completar la acción llama a [onBuildingChanged] para que GameScreen
 * recargue la textura correcta.
 */
class BuildingInfoWindow(
    private val data: Propiedad,
    private val onBuildingChanged: () -> Unit
) : VisWindow("Gestión del Plantel") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = false

        val costo: Long
        val btnTexto: String
        val puedeMejorar: Boolean

        when {
            !data.comprada -> {
                costo       = data.precio
                btnTexto    = "COMPRAR  \$${formatMoney(costo)}"
                puedeMejorar = true
            }
            data.nivel < data.mejoraMax -> {
                costo        = GameState.costoMejora(data)
                btnTexto     = "MEJORAR LVL ${data.nivel + 1}  \$${formatMoney(costo)}"
                puedeMejorar = true
            }
            else -> {
                costo        = 0L
                btnTexto     = "NIVEL MÁXIMO"
                puedeMejorar = false
            }
        }

        add(scene2d.table {

            // ── Nombre ────────────────────────────────────────────────
            label("[GOLD]${data.nombre}[]").cell(padBottom = 6f)
            row()

            // ── Descripción ───────────────────────────────────────────
            label(data.descripcion).apply { setWrap(true) }
                .cell(width = 280f, padBottom = 10f)
            row()

            // ── Stats ─────────────────────────────────────────────────
            label("Capacidad: ${data.capacidad} alumnos"); row()
            label("Ingresos base: ${data.baseAlumnos} alumnos/ciclo"); row()

            if (data.comprada) {
                label("[CYAN]Nivel actual: ${data.nivel} / ${data.mejoraMax}[]"); row()
            }

            // ── Saldo del jugador ─────────────────────────────────────
            label("[LIGHT_GRAY]Tu saldo: \$${formatMoney(GameState.dinero)}[]")
                .cell(padTop = 6f, padBottom = 4f)
            row()

            // ── Botón acción ──────────────────────────────────────────
            textButton(btnTexto) {
                isDisabled = !puedeMejorar

                onChange {
                    if (!puedeMejorar) return@onChange

                    if (!GameState.puedeComprar(costo)) {
                        // Saldo insuficiente → feedback visual (sin cerrar)
                        setText("[RED]¡Saldo insuficiente![]")
                        isDisabled = true
                        return@onChange
                    }

                    GameState.gastar(costo)

                    if (!data.comprada) {
                        data.comprada = true
                        data.nivel    = 1
                    } else {
                        data.nivel++
                    }

                    onBuildingChanged()
                    this@BuildingInfoWindow.remove()
                }
            }.cell(padTop = 14f, expandX = true, fillX = true)
        }).pad(16f)

        pack()
        centerWindow()
    }

    fun show(stage: Stage) {
        stage.addActor(this)
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private fun formatMoney(amount: Long): String = when {
        amount >= 1_000_000L -> "${"%.1f".format(amount / 1_000_000.0)}M"
        amount >= 1_000L     -> "${"%.0f".format(amount / 1_000.0)}K"
        else                 -> amount.toString()
    }
}
