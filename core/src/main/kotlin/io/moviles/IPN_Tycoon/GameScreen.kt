package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*
import java.util.Locale

class GameScreen(game: Main) : BaseScreen(game) {
    private val viewModel = GameViewModel()

    private val map: TiledMap? by lazy {
        try { TmxMapLoader().load("Mapa/Mapa_General.tmx") } catch (_: Exception) { null }
    }
    private val renderer by lazy { map?.let { IsometricTiledMapRenderer(it) } }

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 480f)
        zoom = 8f
        position.set(-436f, 1360f, 0f)
    }
    private var initialZoom = 8f

    // --- OPTIMIZACIÓN: Caché de Texturas y Posiciones ---
    private val textureCache = mutableMapOf<String, Texture>()
    private val dialogueTextureCache = mutableMapOf<String, Texture>()

    private fun getBuildingTexture(prefix: String, nivel: Int): Texture? {
        val path = "Mapa/Edificios/${prefix}lvl$nivel.png"
        return textureCache.getOrPut(path) {
            val file = path.toInternalFile()
            if (file.exists()) {
                Texture(file).apply {
                    setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
            } else return null
        }
    }

    private val edificiosPosiciones by lazy {
        val lista = mutableListOf<Vector2>()
        map?.layers?.get("Puntos_origen")?.objects?.filterIsInstance<PointMapObject>()?.forEach { obj ->
            val worldX = obj.point.x + obj.point.y
            val worldY = (obj.point.y - obj.point.x) * 0.5f
            lista.add(Vector2(worldX, worldY))
        }
        lista
    }

    private var dialogoActor: DialogoActor? = null
    private val backgroundTexture: Texture by lazy {
        Texture("background.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private var backgroundImage: Image? = null

    // UI Elements (HUD)
    private lateinit var labelDinero: Label
    private lateinit var labelNivel: Label
    private lateinit var labelPopularidad: Label

    private var nombreJugador: String = ""
    private var nombreEscuela: String = ""
    var modoCarga: Boolean = false

    // Estilos IPN
    private val colorGuinda = Color.valueOf("660000")
    private val colorOro = Color.valueOf("D4AF37")

    override fun show() {
        super.show()
        val skin = Scene2DSkin.defaultSkin
        val fuente = skin.getFont("default-font")

        if (dialogoActor == null) {
            dialogoActor = DialogoActor(fuente) { path ->
                val texture = dialogueTextureCache.getOrPut(path) {
                    Texture(path.toInternalFile()).apply {
                        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                    }
                }
                TextureRegion(texture)
            }
        }

        if (backgroundImage == null) {
            backgroundImage = Image(backgroundTexture).apply {
                setScaling(Scaling.fit)
                align = Align.center
            }
            backgroundImage?.setFillParent(true)
        }

        stage.clear()

        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    if (!modoCarga && dialogoActor?.isVisible == true) {
                        if (!(dialogoActor?.retroceder() ?: false)) game.setScreen<SeleccionPartida>()
                        return true
                    } else if (modoCarga) {
                        game.setScreen<SeleccionPartida>()
                        return true
                    }
                }
                return false
            }
        })

        if (!modoCarga) {
            backgroundImage?.let { stage.addActor(it) }
            dialogoActor?.let {
                stage.addActor(it)
                it.width = stage.width
                it.height = stage.height

                it.alTerminarNombre = { nombre ->
                    nombreJugador = nombre
                    it.variables["nombre"] = nombre
                }

                it.alTerminarEscuela = { escuela ->
                    nombreEscuela = escuela
                    it.variables["escuela"] = escuela
                }

                it.mostrarConversacion(listOf(
                    Dialogo("?????", "¡Hola! Soy el Ing. Lázaro Cárdenas.", "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas", "Bienvenido a EDU-TYCOON. Aquí podrás crear tu propia institución educativa, tal como lo hice yo.", "sprite_apenado.png"),
                    Dialogo("Ing. Cárdenas", "¿Y por qué no? Llegar a construir un ¡¡IMPERIO EDUCATIVO!!", "sprite_explicando.png"),
                    Dialogo("Ing. Cárdenas", "Pero antes que nada, empecemos por lo básico....", "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas", "¿Cuál es tu nombre?", "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas", "¡Un gusto conocerte {nombre}! Prepárate para el resto.", "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas", "Tener un buen nombre para tu institución lo es todo.", "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas", "Así que dime, ¿qué nombre llevará?", "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas", "¿{escuela}? Suena genial", "sprite_explicando.png")
                ))
            }

            stage.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    dialogoActor?.let {
                        if (it.isVisible) it.avanzar()
                        else {
                            modoCarga = true
                            crearHUD()
                            configurarControlesMapa()
                        }
                    }
                }
            })
            Gdx.input.inputProcessor = stage
        } else {
            crearHUD()
            configurarControlesMapa()
        }
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
    }

    private fun crearHUD() {
        val skin = Scene2DSkin.defaultSkin

        // Colores con profundidad
        val guindaTransparente = Color(0.4f, 0f, 0f, 0.92f)
        val colorBrillo = Color(1f, 1f, 1f, 0.15f)
        val colorSombra = Color(0f, 0f, 0f, 0.5f)

        val fondoGuinda = skin.newDrawable("white", guindaTransparente)
        val fondoOro = skin.newDrawable("white", colorOro)
        val fondoBrillo = skin.newDrawable("white", colorBrillo)
        val fondoSombra = skin.newDrawable("white", colorSombra)

        stage.addActor(scene2d.table {
            setFillParent(true)
            align(Align.top)

            // --- BARRA SUPERIOR (CON VOLUMEN) ---
            stack {
                // 1. Sombra de la barra
                table {
                    align(Align.bottom)
                    image(fondoSombra).cell(fillX = true, height = 5f)
                }

                // 2. Fondo Principal Guinda
                image(fondoGuinda)

                // 3. Brillo Superior
                table {
                    align(Align.top)
                    image(fondoBrillo).cell(fillX = true, height = 2f)
                }

                // 4. Línea inferior de acento (Oro)
                table {
                    align(Align.bottom)
                    image(fondoOro).cell(fillX = true, height = 3f, padBottom = 2f)
                }

                // 5. Contenido de la Barra
                table {
                    table {
                        label(nombreEscuela.ifEmpty { "MI INSTITUCIÓN" }, "default") {
                            color = colorOro
                            setFontScale(1.25f)
                        }.cell(align = Align.left)
                        row()
                        labelNivel = label("NIVEL: ${viewModel.nivel.value}", "default") {
                            color = Color.LIGHT_GRAY
                            setFontScale(0.85f)
                        }
                    }.cell(padLeft = 25f, expandX = true, align = Align.left)

                    table {
                        label("POPULARIDAD", "default") {
                            color = Color.GRAY
                            setFontScale(0.7f)
                        }.cell(padBottom = 2f)
                        row()
                        labelPopularidad = label("${viewModel.popularidad.value}%", "default") { color = Color.WHITE }
                    }.cell(expandX = true)

                    table {
                        labelDinero = label("$${String.format(Locale.US, "%,d", viewModel.dinero.value)}", "default") {
                            color = Color.WHITE
                            setFontScale(1.4f)
                        }
                    }.cell(padRight = 25f, expandX = true, align = Align.right)
                }
            }.cell(expandX = true, fillX = true, height = 85f)

            row()

            // --- BOTONES LATERALES (CON SOMBRA) ---
            table {
                stack {
                    image(fondoSombra).apply {
                        color.a = 0.3f
                        // Simulamos el desplazamiento de la sombra manualmente
                        setScale(1.02f)
                    }
                    textButton("TIENDA", "default") {
                        color = colorGuinda
                        onChange {
                            Gdx.app.log("HUD", "Tienda click")
                        }
                    }
                }.cell(padBottom = 12f, width = 140f, height = 55f)
                row()
                stack {
                    image(fondoSombra).apply { color.a = 0.3f }
                    textButton("MISIONES", "default") {
                        color = colorGuinda
                        onChange {
                            Gdx.app.log("HUD", "Misiones click")
                        }
                    }
                }.cell(padBottom = 12f, width = 140f, height = 55f)
                row()
                stack {
                    image(fondoSombra).apply { color.a = 0.3f }
                    textButton("AJUSTES", "default") {
                        color = Color.DARK_GRAY
                        onChange {
                            Gdx.app.log("HUD", "Ajustes click")
                        }
                    }
                }.cell(width = 140f, height = 55f)
            }.cell(expandX = true, expandY = true, align = Align.bottomRight, padRight = 20f, padBottom = 25f)
        })
    }
  private fun configurarControlesMapa() {
        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)

        val gestureDetector = GestureDetector(object : GestureAdapter() {
            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                if (dialogoActor?.isVisible == true) return false
                val m = map ?: return false

                val worldTouch = Vector3(x, y, 0f)
                camera.unproject(worldTouch)

                val tileWidth = 64f
                val tileHeight = 32f

                val tiledX = (worldTouch.x / (tileWidth / 2f) - worldTouch.y / (tileHeight / 2f)) / 2f * tileHeight
                val tiledY = (worldTouch.y / (tileHeight / 2f) + worldTouch.x / (tileWidth / 2f)) / 2f * tileHeight

                try {
                    val logicaLayer = m.layers["Logica_Clics"] ?: return false
                    logicaLayer.objects.filterIsInstance<RectangleMapObject>().forEach { obj ->
                        if (obj.rectangle.contains(tiledX, tiledY)) {
                            val propiedad = PropiedadRepository.getPropiedad(obj.name ?: "")
                            if (propiedad != null) {
                                BuildingInfoWindow(propiedad) {
                                    Gdx.app.log("GAME", "Mejorando...")
                                }.show(stage)
                                return true
                            }
                        }
                    }
                } catch (_: Exception) {}
                return false
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom)
                return true
            }

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                val ratio = if (distance > 0) initialDistance / distance else 1f
                camera.zoom = (initialZoom * ratio).coerceIn(1f, 15f)
                return true
            }

            override fun pinchStop() { initialZoom = camera.zoom }
        })

        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        if (modoCarga) {
            if (::labelDinero.isInitialized) {
                // Sincronizamos con GameState para que el HUD refleje los cambios (ej. compras)
                if (viewModel.dinero.value != GameState.dinero) {
                    viewModel.updateState(GameState.dinero, viewModel.nivel.value, viewModel.popularidad.value)
                }

                labelDinero.setText("$${String.format(Locale.US, "%,d", viewModel.dinero.value)}")
                labelNivel.setText("Nivel: ${viewModel.nivel.value}")
                labelPopularidad.setText("${viewModel.popularidad.value}%")
            }

            val r = renderer ?: return
            camera.update()
            r.setView(camera)
            r.render()

            r.batch.begin()
            val w = 3728f
            val h = 1968f

            // Renderizado dinámico de edificios comprados
            PropiedadRepository.propiedades.values.forEach { propiedad ->
                if (propiedad.comprada && propiedad.texturePrefix != null) {
                    val tex = getBuildingTexture(propiedad.texturePrefix, propiedad.nivel)
                    if (tex != null) {
                        // Buscamos si hay un punto de origen para este edificio
                        // Por ahora usamos la lógica simplificada de edificiosPosiciones
                        // En un futuro esto debería mapear ID de objeto -> ID de propiedad
                        edificiosPosiciones.forEach { pos ->
                            // Ajuste temporal: si el ID coincide o para ESCOM (que es el principal)
                            if (propiedad.id == "escom_hitbox") {
                                r.batch.draw(tex, pos.x - (w / 2f), pos.y, w, h)
                            }
                        }
                    }
                }
            }
            r.batch.end()
        }
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
        textureCache.values.forEach { it.dispose() }
        textureCache.clear()
        dialogueTextureCache.values.forEach { it.dispose() }
        dialogueTextureCache.clear()
        dialogoActor?.dispose()
        map?.dispose()
    }
}
