package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class GameScreen(game: Main) : BaseScreen(game) {
    private lateinit var dialogoActor: DialogoActor

    private val backgroundTexture: Texture by lazy {
        Texture("background.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    override fun show() {
        super.show()

        // --- UI CONFIGURATION ---
        val skin = Scene2DSkin.defaultSkin
        val fuente = skin.getFont("default-font")

        dialogoActor = DialogoActor(fuente) { path ->
            TextureRegion(Texture(path.toInternalFile()))
        }

        stage.actors {
            stack {
                setFillParent(true)

                image(backgroundTexture) {
                    setScaling(Scaling.fit)
                    setAlign(Align.center)
                }

                container(dialogoActor) {
                    fill()
                }
            }
        }

        // --- DIALOGUE SETUP ---
        val charla = listOf(
            Dialogo("?????", "¡Hola, qué tal! Soy el Ing. Lázaro Cárdenas, gracias por jugar Edu-Tycoon.", "sprite_saludando.png"),
            Dialogo("Ing. Lázaro", "Aquí podrás construir tu propio instituto educativo, tal como yo lo hice.", "sprite_apenado.png"),
            Dialogo("Ing. Lázaro", "Y por qué no, convertirlo en un ¡ IMPERIO EDUCATIVO !", "sprite_explicando.png"),
            Dialogo("Ing. Lázaro", "Pero no nos adelantemos. Para comenzar ¿cuál es tu nombre?", "sprite_hablando.png")
        )

        dialogoActor.mostrarConversacion(charla)

        // --- INPUT HANDLING ---
        stage.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (dialogoActor.isVisible) {
                    dialogoActor.avanzar()
                }
            }
        })
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
    }
}
