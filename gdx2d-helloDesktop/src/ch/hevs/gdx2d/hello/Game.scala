package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.{InputMultiplexer, InputProcessor}

import scala.util.Random
import scala.collection.mutable.ArrayBuffer

/**
 * Main game class that manages the core game loop, entities, and game states.
 * Handles player input, enemy AI, collision detection, rendering, and UI management.
 * Supports pause functionality.
 * 
 * @param unifiedApp Optional reference to the unified app for state management
 */
class MystisGameMenu(unifiedApp: MystisUnifiedApp = null) {

  // Core game entities
  var player: Player = _ // Main player character
  val enemies: ArrayBuffer[Enemy] = new ArrayBuffer[Enemy]() // Collection of enemy entities
  val projectiles: ArrayBuffer[Projectile] = new ArrayBuffer[Projectile]() // Player projectiles
  val random: Random = new Random() // Random number generator for spawning
  var gameOver: Boolean = false // Game over state flag

  // Progression and leveling system
  val progressionSystem = new ProgressionSystem()

  // Map rendering components
  var map: TiledMap = _ // Tiled map data
  var mapRenderer: OrthogonalTiledMapRenderer = _ // Map renderer
  var camera: OrthographicCamera = _ // Game camera

  // Pause menu components
  var pauseStage: Stage = _ // UI stage for pause menu
  var pauseSkin: Skin = _ // UI skin for pause menu styling
  var pauseTable: Table = _ // Layout table for pause menu
  var shapeRenderer: ShapeRenderer = _ // For rendering overlay backgrounds
  var gameInputProcessor: InputProcessor = _ // Handles game input
  var inputMultiplexer: InputMultiplexer = _ // Manages multiple input processors


  /**
   * Initialize the game components, load resources, and set up the game state.
   * Called once when the game starts.
   */
  def onInit(): Unit = {
    // Start gameplay music
    AudioManager.startGameplayMusic()

    camera = new OrthographicCamera()
    camera.setToOrtho(false, GameSettings.width, GameSettings.height)
    
    try {
      map = new TmxMapLoader().load("gdx2d-helloDesktop/data/maps/map.tmx")
      mapRenderer = new OrthogonalTiledMapRenderer(map, 0.3f)
      
      // Update world boundaries based on map dimensions
      if (map != null) {
        val properties = map.getProperties
        val tileWidth = properties.get("tilewidth", classOf[Integer])
        val tileHeight = properties.get("tileheight", classOf[Integer])
        val mapWidthInTiles = properties.get("width", classOf[Integer])
        val mapHeightInTiles = properties.get("height", classOf[Integer])
        
        // Calculate actual map size with scale factor
        val mapWidth = tileWidth * mapWidthInTiles * 0.3f
        val mapHeight = tileHeight * mapHeightInTiles * 0.3f
        
        // Update world boundaries
        GameSettings.updateWorldBounds(0f, 0f, mapWidth, mapHeight)
      }
    } catch {
      case e: Exception =>
        println(s"Unable to load map: ${e.getMessage}")
        // Continue without map if it doesn't exist
    }

    player = new Player(new Vector2(1700, 2070))
    for (_ <- 0 until GameSettings.enemyLimit) {
      enemies.append(new Enemy(randomEnemyPosition(), random.nextInt(2)))
    }

    // Initialize pause menu
    shapeRenderer = new ShapeRenderer()
    initGameInputProcessor()
    initPauseMenu()
    
    // IMPORTANT: Check if we're returning from options to game
    if (GameSettings.previousState == "GAME") {
      println("🔄 Returning from options to game - restoring pause state")
      // Force game to be paused when returning from options
      GameSettings.isGamePaused = true
      
      // CRITICAL: Completely reinitialize pause menu to fix button issues
      if (pauseStage != null) pauseStage.dispose()
      if (pauseSkin != null) pauseSkin.dispose()
      initPauseMenu() // Recreate pause menu from scratch
      
      // Ensure pause menu is properly initialized and active
      inputMultiplexer.clear()
      inputMultiplexer.addProcessor(pauseStage)
      inputMultiplexer.addProcessor(gameInputProcessor)
      Gdx.input.setInputProcessor(inputMultiplexer)
      // Reset the previous state
      GameSettings.previousState = ""
    } else {
      // Normal initialization - ensure game input is active
      inputMultiplexer.clear()
      inputMultiplexer.addProcessor(gameInputProcessor)
      Gdx.input.setInputProcessor(inputMultiplexer)
    }
  }


  /**
   * Generate a random enemy spawn position that maintains minimum distance from player.
   * Ensures enemies don't spawn too close to the player.
   * 
   * @return Vector2 position for enemy spawn
   */
  def randomEnemyPosition(): Vector2 = {
    var pos: Vector2 = null
    do {
      val x = random.nextInt(GameSettings.width)
      val y = random.nextInt(GameSettings.height)
      pos = new Vector2(x, y)
    } while (pos.dst(player.pos) < GameSettings.enemySpawnMinDistance)
    pos
  }

  /**
   * Create a new enemy at a safe distance from the player.
   * Used for respawning enemies after kills.
   * 
   * @param playerPos Current player position
   * @return New Enemy instance
   */
  def createEnemyAwayFromPlayer(playerPos: Vector2): Enemy = {
    var pos = new Vector2()
    do {
      pos = new Vector2(MathUtils.random(0f, 1920), MathUtils.random(0f, 1080))
    } while (pos.dst(playerPos) < GameSettings.enemySpawnMinDistance)
    new Enemy(pos, random.nextInt(2))
  }

  /**
   * Initialize the game input processor to handle keyboard events.
   * Sets up pause/resume functionality.
   */
  def initGameInputProcessor(): Unit = {
    gameInputProcessor = new InputProcessor {
      override def keyDown(keycode: Int): Boolean = {
        if (GameSettings.Controls.isKeyPressed("menu", keycode)) {
          println(s"ESC pressed! GameSettings.isGamePaused = ${GameSettings.isGamePaused}")
          if (GameSettings.isGamePaused) {
            println("Resuming game...")
            resumeGame()
          } else {
            println("Pausing game...")
            pauseGame()
          }
          true
        } else {
          false
        }
      }
      override def keyUp(keycode: Int): Boolean = false
      override def keyTyped(character: Char): Boolean = false
      override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
      override def mouseMoved(screenX: Int, screenY: Int): Boolean = false
      override def scrolled(amount: Int): Boolean = false
    }

    // Create a multiplexer that will always have game input
    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(gameInputProcessor)
    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  /**
   * Initialize the pause menu UI components.
   * Creates buttons, layouts, and event handlers for the pause menu.
   */
  def initPauseMenu(): Unit = {
    val viewport = new ScreenViewport()
    pauseStage = new Stage(viewport, new SpriteBatch())
    pauseSkin = new Skin(Gdx.files.internal("gdx2d-helloDesktop/data/ui/uiskin.json"))

    // Create main pause menu table
    pauseTable = new Table()
    pauseTable.setFillParent(true)
    pauseTable.center()

    // Pause menu title
    val titleLabel = new Label("PAUSE", pauseSkin)
    titleLabel.setFontScale(3f)
    titleLabel.setColor(MystisColors.GOLD)

    // Pause menu buttons
    val resumeButton = new TextButton("Resume", pauseSkin)
    resumeButton.getLabel.setFontScale(1.8f)
    resumeButton.getLabel.setColor(MystisColors.GOLD)

    val optionsButton = new TextButton("Options", pauseSkin)
    optionsButton.getLabel.setFontScale(1.8f)
    optionsButton.getLabel.setColor(MystisColors.GOLD)

    val quitButton = new TextButton("Quit", pauseSkin)
    quitButton.getLabel.setFontScale(1.8f)
    quitButton.getLabel.setColor(MystisColors.GOLD)

    // Event listeners
    resumeButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        resumeGame()
      }
    })

    optionsButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        goToOptions()
      }
    })

    quitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        quitToMenu()
      }
    })

    // Add elements to table
    pauseTable.add(titleLabel).pad(20).row()
    pauseTable.add(resumeButton).pad(15).width(200).height(50).row()
    pauseTable.add(optionsButton).pad(15).width(200).height(50).row()
    pauseTable.add(quitButton).pad(15).width(200).height(50).row()

    pauseStage.addActor(pauseTable)
  }

  /**
   * Pause the game and show the pause menu.
   * Switches input handling to prioritize pause menu interactions.
   */
  def pauseGame(): Unit = {
    GameSettings.isGamePaused = true
    // Add pauseStage to multiplexer (first to have priority)
    inputMultiplexer.clear()
    inputMultiplexer.addProcessor(pauseStage)
    inputMultiplexer.addProcessor(gameInputProcessor)
  }

  /**
   * Resume the game and hide the pause menu.
   * Restores normal game input handling.
   */
  def resumeGame(): Unit = {
    GameSettings.isGamePaused = false
    // Remove pauseStage, keep only game input
    inputMultiplexer.clear()
    inputMultiplexer.addProcessor(gameInputProcessor)
  }

  /**
   * Navigate to the options screen.
   * Saves current state for proper return handling.
   */
  def goToOptions(): Unit = {
    GameSettings.previousState = "GAME"
    if (unifiedApp != null) {
      unifiedApp.changeState(AppState.OPTIONS)
    }
  }

  /**
   * Quit the current game and return to main menu.
   * Cleans up game state and handles app state transition.
   */
  def quitToMenu(): Unit = {
    GameSettings.isGamePaused = false
    if (unifiedApp != null) {
      unifiedApp.changeState(AppState.MENU)
    } else {
      Gdx.app.exit()
    }
  }


  /**
   * Main rendering method called every frame.
   * Handles game logic, input processing, rendering, and UI updates.
   * 
   * @param g Graphics context for rendering
   */
  def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Camera follows player with map bounds
    val playerCenter = player.getCenter
    
    // Calculate camera position with bounds checking
    val halfScreenWidth = GameSettings.width / 2f
    val halfScreenHeight = GameSettings.height / 2f
    
    // Constrain camera to not show beyond map edges
    val cameraX = MathUtils.clamp(playerCenter.x, 
      GameSettings.worldMinX + halfScreenWidth, 
      GameSettings.worldMaxX - halfScreenWidth)
    val cameraY = MathUtils.clamp(playerCenter.y, 
      GameSettings.worldMinY + halfScreenHeight, 
      GameSettings.worldMaxY - halfScreenHeight)
    
    // Get map dimensions for moveCamera method
    val mapWidth = GameSettings.worldMaxX - GameSettings.worldMinX
    val mapHeight = GameSettings.worldMaxY - GameSettings.worldMinY
    
    // Use GdxGraphics moveCamera with constrained position
    g.moveCamera(cameraX, cameraY, mapWidth.toInt, mapHeight.toInt)

    // Render map if it exists
    if (mapRenderer != null) {
      mapRenderer.setView(g.getCamera)
      mapRenderer.render()
    }

    // Game logic only if not paused
    if (!GameSettings.isGamePaused) {
      // Input handling for bonuses using unified control system:
      if (progressionSystem.isLevelUpPaused) {
        if (Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus1._1) || Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus1._2)) {
          progressionSystem.chooseBonusCard(0)
        } else if (Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus2._1) || Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus2._2)) {
          progressionSystem.chooseBonusCard(1)
        } else if (Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus3._1) || Gdx.input.isKeyJustPressed(GameSettings.Controls.bonus3._2)) {
          progressionSystem.chooseBonusCard(2)
        }
      }

      // Input for ultimate using unified control system:
      if (Gdx.input.isKeyJustPressed(GameSettings.Controls.ultimate._1) || Gdx.input.isKeyJustPressed(GameSettings.Controls.ultimate._2)) {
        if (progressionSystem.useUltimate()) {
          // Play ultimate sound
          AudioManager.playUltimateSound()
          
          // Ultimate effect: repel and damage all enemies in range
          val ultimateRange = 300f
          for (e <- enemies) {
            val distance = e.getCenter.dst(player.getCenter)
            if (distance <= ultimateRange) {
              // Repel enemy
              e.repelFromPlayer(player.getCenter)
              // Inflict damage (equivalent to multiple hits)
              e.takeDamage()
              e.takeDamage()
              e.takeDamage()
              println(s"Ultimate! Enemy hit at ${distance.toInt} pixels")
            }
          }
        }
      }

      if (!gameOver && !progressionSystem.isLevelUpPaused) {
        var playerTouched = false
        val deltaTime = Gdx.graphics.getDeltaTime
        player.update(deltaTime)
        player.updateShootTimer(deltaTime)

        if (player.state == "dead" && player.isDeathAnimationFinished) {
          gameOver = true
        }

        val nearestEnemyToShoot = enemies.minByOption(_.getCenter.dst(player.getCenter))
        nearestEnemyToShoot.foreach { enemy =>
          val distance = enemy.getCenter.dst(player.getCenter)
          if (distance <= GameSettings.projectileShootRange && player.canShoot && enemy.state != "dead") {
            val direction = enemy.getCenter.cpy().sub(player.getCenter).nor()
            val projectile = new Projectile(player.getCenter.cpy(), direction.scl(GameSettings.projectileSpeed))
            projectiles.append(projectile)
            player.resetShootTimer()
            
            // Play shooting sound
            AudioManager.playShootSound()
          }
        }

        val deadProjectiles = new ArrayBuffer[Projectile]()
        for (p <- projectiles) {
          p.update(deltaTime)
          for (e <- enemies) {
            if (p.isCollidingWith(g, e, deltaTime)) {
              e.takeDamage()
              deadProjectiles.append(p)
            }
          }
          p.draw(g)
        }
        projectiles --= deadProjectiles

        for (e <- enemies) {
          e.update(player.getCenter, deltaTime)
          if (e.isCollidingWith(player)) {
            playerTouched = true
            player.takeDamage(GameSettings.enemyDamage)
            // Play damage sound
            AudioManager.playDamageSound()
          }
        }

        val deadEnemies = enemies.filter(_.state == "dead")

        // XP only if there are dead enemies
        for (e <- deadEnemies) {
          enemies -= e
          
          // Play kill sound
          AudioManager.playKillSound()

          // Give XP based on enemy type (0 = basicEnemy=5xp, 1 = eliteEnemy=10xp)
          val enemyType = if (e.enemyType == 0) "basicEnemy" else "eliteEnemy"
          progressionSystem.onEnemyKilled(enemyType)
        }
        
        // Spawn new enemies to maintain current limit
        while (enemies.length < GameSettings.enemyLimit) {
          val newEnemy = createEnemyAwayFromPlayer(player.getCenter)
          enemies.append(newEnemy)
        }

        if (playerTouched) {
          for (e <- enemies) {
            e.repelFromPlayer(player.getCenter)
          }
        }

        for (e <- enemies) {
          for (other <- enemies) {
            if (e != other && e.getCenter.dst(other.getCenter) < GameSettings.enemySize) {
              e.repelFromEachOther(other.getCenter, 1.5f)
            }
          }

          e.draw(g)
        }
      } // End of if (!gameOver) block
    } // End of pause condition

    // DISPLAY WORLD ELEMENTS (with camera):
    for (p <- projectiles) {
      p.draw(g)
    }

    for (e <- enemies) {
      e.draw(g)
    }

    player.draw(g)

    // Reset camera for UI rendering (fixed screen coordinates)
    val originalCamera = g.getCamera
    g.resetCamera()

    // UI ELEMENTS (fixed screen position):
    progressionSystem.drawAllUI(g, 1920, 1080,
      GameSettings.playerCurrentHP, GameSettings.playerMaxHP)

    // Display FPS
    g.drawFPS(MystisColors.GOLD)

    // Level up screen (only if not paused)
    if (!GameSettings.isGamePaused) {
      progressionSystem.drawLevelUpScreen(g, 1920, 1080)
    }

    // Debug info - player position (fixed on screen)
//    val playerPos = player.getCenter
//    g.setColor(MystisColors.GOLD)
//    g.drawString(GameSettings.width - 200, 20,
//      f"Position: (${playerPos.x}%.0f, ${playerPos.y}%.0f)")

    // Pause menu with semi-transparent background
    if (GameSettings.isGamePaused) {
      // DEBUG: Check input processor setup
      val currentProcessor = Gdx.input.getInputProcessor
      if (currentProcessor != inputMultiplexer) {
        println("🚨 WARNING: InputProcessor mismatch detected! Fixing...")
        inputMultiplexer.clear()
        inputMultiplexer.addProcessor(pauseStage)
        inputMultiplexer.addProcessor(gameInputProcessor)
        Gdx.input.setInputProcessor(inputMultiplexer)
      }
      
      // Darken screen
      Gdx.gl.glEnable(GL20.GL_BLEND)
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
      shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
      shapeRenderer.setColor(0, 0, 0, 0.5f) // Transparent black at 50%
      shapeRenderer.rect(0, 0, 1920, 1080)
      shapeRenderer.end()
      
      // Display pause menu
      pauseStage.act()
      pauseStage.draw()
    }

    if (gameOver) {
      val font = new BitmapFont()
      font.getData.setScale(8f)
      font.setColor(new Color(0.7f, 0f, 0f, 1f))
      g.drawString(GameSettings.width / 2, GameSettings.height / 2, "GAME OVER", font, 1)
    }
  }

  /**
   * Handle keyboard input events.
   * Currently used for additional key handling if needed.
   * 
   * @param keycode The key code that was pressed
   */
  def onKeyDown(keycode: Int): Unit = {
    // This method can be used for other keyboard events if necessary
    // Pause is now handled directly in onGraphicRender
  }

  /**
   * Clean up resources when the game is disposed.
   * Properly disposes of UI components and resets input handling.
   */
  def dispose(): Unit = {
    // Cleanup resources
    GameSettings.isGamePaused = false
    Gdx.input.setInputProcessor(null)
    if (pauseStage != null) pauseStage.dispose()
    if (pauseSkin != null) pauseSkin.dispose()
    if (shapeRenderer != null) shapeRenderer.dispose()
  }
}

/**
 * Lanceur autonome pour tester le menu Game indépendamment.
 *
 * UTILISATION :
 * - Développement : tester le menu sans lancer tout le jeu
 * - Débogage : isoler les problèmes du menu des options
 * - Démonstration : montrer le menu à des tiers
 *
 * FONCTIONNEMENT :
 * Crée une instance du menu sans application unifiée (unifiedApp = null)
 */
object MystisGameMenuLauncher extends PortableApplication(1920, 1080) with App {
  var gameMenu: MystisGameMenu = _

  override def onInit(): Unit = {
    gameMenu = new MystisGameMenu()
    gameMenu.onInit()
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    gameMenu.onGraphicRender(g)
  }

  def dispose(): Unit = {
    if (gameMenu != null) gameMenu.dispose()
  }
}

