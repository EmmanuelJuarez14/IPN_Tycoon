package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen

// Definimos la clase como abstract para que otras pantallas la hereden
abstract class BaseScreen(
    val game: Main,
    // Usamos FitViewport con la resolución 800x480 para mantener la proporción
    val viewport: Viewport = FitViewport(800f, 480f)
) : KtxScreen {

    // El stage es el contenedor de actores (botones, imágenes, etc.)
    val stage: Stage = Stage(viewport)

    override fun show() {
        // Al mostrar la pantalla, el stage toma el control de los clics/toques
        Gdx.input.inputProcessor = stage
    }

    override fun resize(width: Int, height: Int) {
        // Actualiza el viewport para que el juego no se estire feo
        viewport.update(width, height, true)
    }

    override fun hide() {
        // Quitamos el procesador de entrada cuando la pantalla no es visible
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        // ¡Muy importante! Liberamos la memoria del stage al cerrar la pantalla
        stage.dispose()
    }
}
