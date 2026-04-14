package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.actors.onChange
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class Bienvenida(private val game: Main) : KtxScreen {
    // Definimos una resolución virtual fija para evitar deformaciones
    private val virtualWidth = 800f
    private val virtualHeight = 480f

    // Usamos FitViewport para mantener la relación de aspecto
    private val stage = Stage(FitViewport(virtualWidth, virtualHeight))
    private var pixelFont: BitmapFont? = null

    private val backgroundTexture by lazy {
        val file = "back_bienvenida.png".toInternalFile()
        if (file.exists()) {
            Texture(file).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
        } else null
    }

    private val pixelTexture: Texture by lazy {
        val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.None
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()

        // Borde Exterior (#3e3e54)
        pixmap.setColor(Color.valueOf("3e3e54"))
        pixmap.fillRectangle(2, 0, 8, 12)
        pixmap.fillRectangle(0, 2, 12, 8)
        pixmap.fillRectangle(1, 1, 10, 10)

        // Cuerpo Verde Principal (#8cbd5c)
        pixmap.setColor(Color.valueOf("8cbd5c"))
        pixmap.fillRectangle(1, 1, 10, 10)

        // Sombra Inferior (#5b8c3f)
        pixmap.setColor(Color.valueOf("5b8c3f"))
        pixmap.fillRectangle(1, 6, 10, 5)

        // Brillo Superior (#c8e6a1)
        pixmap.setColor(Color.valueOf("c8e6a1"))
        pixmap.fillRectangle(2, 1, 8, 1)

        Texture(pixmap).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
    }

    private fun generatePixelFont(): BitmapFont {
        val fontFile = "font.ttf".toInternalFile()
        return if (fontFile.exists()) {
            val generator = FreeTypeFontGenerator(fontFile)
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 40 // Ajustado para la nueva resolución
                color = Color.WHITE
                shadowColor = Color.valueOf("3e3e54")
                shadowOffsetX = 3
                shadowOffsetY = 3
                minFilter = TextureFilter.Nearest
                magFilter = TextureFilter.Nearest
            }
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } else {
            // Fallback si no hay fuente
            BitmapFont().apply {
                data.setScale(2f)
            }
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        pixelFont = generatePixelFont()

        val pixelDrawable = NinePatchDrawable(NinePatch(pixelTexture, 5, 5, 5, 5))

        val pixelButtonStyle = TextButtonStyle().apply {
            font = pixelFont
            up = pixelDrawable
            over = pixelDrawable.tint(Color.valueOf("d1e8b2"))
            down = pixelDrawable.tint(Color.valueOf("a0a0a0"))
        }

        stage.actors {
            stack {
                setFillParent(true)

                backgroundTexture?.let {
                    image(it) { setScaling(Scaling.fill) }
                }

                table {
                    setFillParent(true)
                    bottom() // Alinea el contenido al fondo

                    textButton("START") {
                        style = pixelButtonStyle
                        onChange {
                            // Navegación hacia la pantalla de juego
                            game.setScreen<FirstScreen>()
                        }
                    }.cell(width = 300f, height = 100f, padBottom = 80f)
                }
            }
        }
    }

    override fun render(delta: Float) {
        // Limpiamos pantalla con negro antes de dibujar el stage
        clearScreen(0f, 0f, 0f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        // El viewport se encarga de ajustar la cámara correctamente
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        // Liberación de memoria obligatoria para el Dev B
        stage.dispose()
        pixelTexture.dispose()
        backgroundTexture?.dispose()
        pixelFont?.dispose()
    }
}
