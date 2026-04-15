package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Scaling
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class SeleccionPartida(game: Main) : BaseScreen(game) {

    private var pixelFont: BitmapFont? = null

    // Textura de fondo para los botones (Pixel Art)
    private val buttonTexture: Texture by lazy {
        val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        // Borde oscuro
        pixmap.setColor(Color.valueOf("3e3e54"))
        pixmap.fillRectangle(0, 0, 12, 12)
        // Fondo principal (verde)
        pixmap.setColor(Color.valueOf("8cbd5c"))
        pixmap.fillRectangle(1, 1, 10, 10)
        // Brillo superior
        pixmap.setColor(Color.valueOf("c8e6a1"))
        pixmap.fillRectangle(1, 1, 10, 2)
        // Sombra inferior
        pixmap.setColor(Color.valueOf("5b8c3f"))
        pixmap.fillRectangle(1, 9, 10, 2)

        Texture(pixmap).apply { setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest) }
    }

    private fun generatePixelFont(size: Int): BitmapFont {
        val fontFile = "font.ttf".toInternalFile()
        return if (fontFile.exists()) {
            val generator = FreeTypeFontGenerator(fontFile)
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size = size
                color = Color.WHITE
                borderWidth = 2f
                borderColor = Color.valueOf("3e3e54")
                minFilter = Texture.TextureFilter.Nearest
                magFilter = Texture.TextureFilter.Nearest
            }
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } else {
            BitmapFont().apply { data.setScale(size / 15f) }
        }
    }

    override fun show() {
        super.show()
        pixelFont = generatePixelFont(24)

        val drawable = NinePatchDrawable(NinePatch(buttonTexture, 4, 4, 4, 4))
        val customButtonStyle = TextButtonStyle().apply {
            font = pixelFont
            up = drawable
            over = drawable.tint(Color.valueOf("d1e8b2"))
            down = drawable.tint(Color.valueOf("a0a0a0"))
        }

        stage.actors {
            table {
                setFillParent(true)
                background = drawable.tint(Color.valueOf("2d2d3a")) // Fondo tipo panel

                // Título con estilo
                label("GESTIÓN DE CARRERA") {
                    color = Color.GOLD
                    setFontScale(1.2f)
                }.cell(padBottom = 60f)

                row()

                // Contenedor para botones
                table {
                    // Botón Nueva Partida
                    textButton("NUEVA PARTIDA") {
                        style = customButtonStyle
                        onChange { game.setScreen<FirstScreen>() }
                    }.cell(width = 450f, height = 90f, padBottom = 25f)

                    row()

                    // Botón Cargar Partida
                    textButton("CARGAR PARTIDA") {
                        style = customButtonStyle
                        onChange {
                            println("Cargando partida...")
                            game.setScreen<FirstScreen>()
                        }
                    }.cell(width = 450f, height = 90f)
                }.cell(pad = 20f)
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.12f, 0.12f, 0.15f, 1f) // Color de fondo que combine
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        buttonTexture.dispose()
        pixelFont?.dispose()
    }
}
