package ch.hevs.gdx2d.hello

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.utils.Logger
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.{Input, InputProcessor, InputMultiplexer}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import scala.collection.mutable.ArrayBuffer

class MystisOptionsMenu(unifiedApp: MystisUnifiedApp = null) {

  var stage: Stage = _
  var skin: Skin = _
  var inputMultiplexer: InputMultiplexer = _

  // Font Sizes - Modifier ces valeurs pour redimensionner automatiquement tout
  val TITLE_FONT_SIZE = 5f
  val SECTION_FONT_SIZE = 2.0f
  val SUB_SECTION_FONT_SIZE = 1.6f
  val LABEL_FONT_SIZE = 1.5f      // Label + Separator
  val NORMAL_FONT_SIZE = 1.5f     // VolumeValue
  val BUTTON_FONT_SIZE = 1.5f     // Button et Button.text

  // Espacements verticaux
  val ESPACEMENT_TITLE = 200      // Espacement après le titre
  val ESPACEMENT_SECTION = 60     // Espacement après la section
  val ESPACEMENT_SOUS_SECTION = 60  // Espacement après la sous-section
  val ESPACEMENT_NORMAL = 35      // Espacement aprés du normal
  val ESPACEMENT_ITEM = 50      // Espacement aprés un item
  
  // Layout en grille comme Word - Positions absolues
  val PAGE_MARGIN = 100               // Marge de page
  val COLUMN_WIDTH = 800             // Largeur d'une colonne
  val COLUMN_GAP = 300               // Espacement entre colonnes
  val LABEL_WIDTH = 180              // Largeur fixe des labels
  val CONTROL_WIDTH = 200            // Largeur des contrôles (sliders/boutons)
  val VALUE_WIDTH = 80               // Largeur des valeurs
  val LINE_HEIGHT = 50               // Hauteur de ligne fixe
  
  // Dimensions des boutons de contrôle
  val KEY_BUTTON_WIDTH = 90          // Largeur des boutons de touches
  val KEY_BUTTON_HEIGHT = 40         // Hauteur des boutons de touches

  // Layout scaling basé sur les tailles de police
  val FONT_SCALE_FACTOR = NORMAL_FONT_SIZE / 1.0f
  val COMPONENT_HEIGHT = (40 * FONT_SCALE_FACTOR).toInt
  val MARGIN = (120 * FONT_SCALE_FACTOR).toInt

  // Mystis Color Scheme
  val MYSTIS_GOLD = new Color(1.0f, 0.84f, 0.0f, 1.0f)        // Or doré
  val MYSTIS_ORANGE = new Color(1.0f, 0.65f, 0.0f, 1.0f)      // Orange chaud
  val MYSTIS_DARK_ORANGE = new Color(0.8f, 0.4f, 0.0f, 1.0f)  // Orange foncé
  val MYSTIS_BLUE = new Color(0.2f, 0.5f, 0.9f, 1.0f)         // Bleu mystique
  val MYSTIS_DARK = new Color(0.1f, 0.1f, 0.2f, 0.8f)         // Fond semi-transparent
  val MYSTIS_BROWN = new Color(0.15f, 0.1f, 0.05f, 1.0f)      // Brun très sombre

  // Key binding capture state
  var waitingForKey: Boolean = false
  var captureIndex: Int = -1
  var capturePrimary: Boolean = true

  //Title
  var optionsTitleLabel: Label = _

  // Audio Section Components
  var audioSectionLabel: Label = _
  var audioInfoLabel: Label = _
  var musicLabel: Label = _
  var musicToggleButton: TextButton = _
  var sfxLabel: Label = _
  var sfxToggleButton: TextButton = _

  // Display Section Components
  var displaySectionLabel: Label = _

  // Gameplay Section Components
  var gameplaySectionLabel: Label = _
  var languageLabel: Label = _
  var languageButton: TextButton = _
  var languageDropdownButtons: ArrayBuffer[TextButton] = ArrayBuffer()
  var languageDropdownVisible: Boolean = false
  var subtitlesLabel: Label = _
  var subtitlesButton: TextButton = _

  // Controls Section Components
  var controlsSectionLabel: Label = _
  var bindingsSubLabel: Label = _
  var keyBindingLabels: ArrayBuffer[Label] = ArrayBuffer()
  var keyBindingButtons1: ArrayBuffer[TextButton] = ArrayBuffer()
  var keyBindingButtons2: ArrayBuffer[TextButton] = ArrayBuffer()
  var separatorLabels: ArrayBuffer[Label] = ArrayBuffer()

  // Labels pour les instructions de capture de touches
  var captureInstructionLabel1: Label = _
  var captureInstructionLabel2: Label = _
  var captureInstructionVisible: Boolean = false

  // Main Buttons
  var returnButton: TextButton = _
  var applyButton: TextButton = _
  var resetButton: TextButton = _

  // State variables - Load from GameSettings
  var currentLanguage: Int = 0
  var subtitlesEnabled: Boolean = GameSettings.subtitlesEnabled
  val languages = Array("English", "Français", "Deutsch", "Español", "Italiano")

  // Key bindings data - synchronized with GameSettings.Controls
  case class KeyBinding(action: String, gameAction: String, var primary: String, var secondary: String)
  val keyBindings = ArrayBuffer(
    KeyBinding("Avancer", "moveUp", GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._1), GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._2)),
    KeyBinding("Reculer", "moveDown", GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._1), GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._2)),
    KeyBinding("Gauche", "moveLeft", GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._1), GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._2)),
    KeyBinding("Droite", "moveRight", GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._1), GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._2)),
    KeyBinding("Saut", "jump", GameSettings.Controls.getKeyName(GameSettings.Controls.jump._1), GameSettings.Controls.getKeyName(GameSettings.Controls.jump._2)),
    KeyBinding("Attaque", "attack", GameSettings.Controls.getKeyName(GameSettings.Controls.attack._1), GameSettings.Controls.getKeyName(GameSettings.Controls.attack._2)),
    KeyBinding("Magie", "magic", GameSettings.Controls.getKeyName(GameSettings.Controls.magic._1), GameSettings.Controls.getKeyName(GameSettings.Controls.magic._2)),
    KeyBinding("Inventaire", "inventory", GameSettings.Controls.getKeyName(GameSettings.Controls.inventory._1), GameSettings.Controls.getKeyName(GameSettings.Controls.inventory._2)),
    KeyBinding("Menu", "menu", GameSettings.Controls.getKeyName(GameSettings.Controls.menu._1), GameSettings.Controls.getKeyName(GameSettings.Controls.menu._2)),
    KeyBinding("Carte", "map", GameSettings.Controls.getKeyName(GameSettings.Controls.map._1), GameSettings.Controls.getKeyName(GameSettings.Controls.map._2))
  )

  def onInit(): Unit = {
    // Les options préservent la musique en cours (menu ou gameplay selon d'où on vient)

    // Créer un stage avec SpriteBatch personnalisé
    import com.badlogic.gdx.utils.viewport.ScreenViewport
    val customBatch = new SpriteBatch()
    val viewport = new ScreenViewport()
    stage = new Stage(viewport, customBatch)
    skin = new Skin(Gdx.files.internal("gdx2d-helloDesktop/data/ui/uiskin.json"))

    // Setup input multiplexer for key capture
    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(stage)
    inputMultiplexer.addProcessor(createKeyInputProcessor())
    Gdx.input.setInputProcessor(inputMultiplexer)

    initializeComponents()
    setupLayout()
    addEventListeners()
    addComponentsToStage()
  }

  private def createKeyInputProcessor(): InputProcessor = {
    new InputProcessor {
      override def keyDown(keycode: Int): Boolean = {
        if (waitingForKey) {
          captureKey(keycode)
          true
        } else {
          false
        }
      }

      override def keyUp(keycode: Int): Boolean = false
      override def keyTyped(character: Char): Boolean = false
      override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
        if (waitingForKey) {
          captureMouseButton(button)
          true
        } else {
          false
        }
      }
      override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
      override def mouseMoved(screenX: Int, screenY: Int): Boolean = false
      override def scrolled(amount: Int): Boolean = false
    }
  }

  private def captureKey(keycode: Int): Unit = {
    // Allow ESC to cancel key capture
    if (keycode == Input.Keys.ESCAPE) {
      cancelKeyCapture()
      return
    }

    val keyName = GameSettings.Controls.getKeyName(keycode)
    val binding = keyBindings(captureIndex)

    // Update GameSettings.Controls
    GameSettings.Controls.updateKeyBinding(binding.gameAction, capturePrimary, keycode)

    if (capturePrimary) {
      binding.primary = keyName
      keyBindingButtons1(captureIndex).setText(keyName)
      keyBindingButtons1(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    } else {
      binding.secondary = keyName
      keyBindingButtons2(captureIndex).setText(keyName)
      keyBindingButtons2(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    }

    waitingForKey = false
    captureIndex = -1
    Logger.log(s"Nouvelle touche assignée: $keyName pour ${binding.action}")
  }

  private def captureMouseButton(button: Int): Unit = {
    val keycode = button match {
      case Input.Buttons.LEFT => -1
      case Input.Buttons.RIGHT => -2
      case Input.Buttons.MIDDLE => -3
      case _ => -button
    }
    
    val buttonName = GameSettings.Controls.getKeyName(keycode)
    val binding = keyBindings(captureIndex)

    // Update GameSettings.Controls
    GameSettings.Controls.updateKeyBinding(binding.gameAction, capturePrimary, keycode)

    if (capturePrimary) {
      binding.primary = buttonName
      keyBindingButtons1(captureIndex).setText(buttonName)
      keyBindingButtons1(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    } else {
      binding.secondary = buttonName
      keyBindingButtons2(captureIndex).setText(buttonName)
      keyBindingButtons2(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    }

    waitingForKey = false
    captureIndex = -1
    Logger.log(s"Nouveau bouton souris assigné: $buttonName pour ${binding.action}")
  }


  private def initializeComponents(): Unit = {
    optionsTitleLabel = new Label("Options", skin, "default")
    optionsTitleLabel.setFontScale(TITLE_FONT_SIZE)
    optionsTitleLabel.setColor(MYSTIS_GOLD)

    // Audio Section
    audioSectionLabel = new Label("=== AUDIO ===", skin, "default")
    audioSectionLabel.setFontScale(SECTION_FONT_SIZE)
    audioSectionLabel.setColor(MYSTIS_GOLD)
    
    audioInfoLabel = new Label("Utilisez les contrôles volume système", skin)
    audioInfoLabel.setFontScale(LABEL_FONT_SIZE)
    audioInfoLabel.setColor(MYSTIS_ORANGE)

    musicLabel = new Label("Musique:", skin)
    musicLabel.setFontScale(LABEL_FONT_SIZE)
    musicLabel.setColor(MYSTIS_ORANGE)
    musicToggleButton = new TextButton(if (GameSettings.musicEnabled) "ON" else "OFF", skin)
    musicToggleButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    musicToggleButton.getLabel.setColor(if (GameSettings.musicEnabled) MYSTIS_GOLD else MYSTIS_DARK_ORANGE)

    sfxLabel = new Label("Effets Sonores:", skin)
    sfxLabel.setFontScale(LABEL_FONT_SIZE)
    sfxLabel.setColor(MYSTIS_ORANGE)
    sfxToggleButton = new TextButton(if (GameSettings.sfxEnabled) "ON" else "OFF", skin)
    sfxToggleButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    sfxToggleButton.getLabel.setColor(if (GameSettings.sfxEnabled) MYSTIS_GOLD else MYSTIS_DARK_ORANGE)

    // Display Section
    displaySectionLabel = new Label("=== AFFICHAGE ===", skin)
    displaySectionLabel.setFontScale(SECTION_FONT_SIZE)
    displaySectionLabel.setColor(MYSTIS_GOLD)

    // Gameplay Section
    gameplaySectionLabel = new Label("=== JEU ===", skin)
    gameplaySectionLabel.setFontScale(SECTION_FONT_SIZE)
    gameplaySectionLabel.setColor(MYSTIS_GOLD)
    languageLabel = new Label("Langue", skin)
    languageLabel.setFontScale(LABEL_FONT_SIZE)
    languageLabel.setColor(MYSTIS_ORANGE)
    languageButton = new TextButton(s"${languages(currentLanguage)} ▼", skin)
    languageButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    languageButton.getLabel.setColor(MYSTIS_GOLD)

    // Create language dropdown buttons
    for (lang <- languages) {
      val button = new TextButton(lang, skin)
      button.getLabel.setFontScale(BUTTON_FONT_SIZE)
      button.getLabel.setColor(MYSTIS_ORANGE)
      languageDropdownButtons += button
    }

    subtitlesLabel = new Label("Sous-titres", skin)
    subtitlesLabel.setFontScale(LABEL_FONT_SIZE)
    subtitlesLabel.setColor(MYSTIS_ORANGE)
    subtitlesButton = new TextButton(if (subtitlesEnabled) "ACTIVES" else "DESACTIVES", skin)
    subtitlesButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    subtitlesButton.getLabel.setColor(MYSTIS_GOLD)

    // Controls Section
    controlsSectionLabel = new Label("=== CONTROLES ===", skin)
    controlsSectionLabel.setFontScale(SECTION_FONT_SIZE)
    controlsSectionLabel.setColor(MYSTIS_GOLD)
    bindingsSubLabel = new Label("--- Raccourcis Clavier ---", skin)
    bindingsSubLabel.setFontScale(SUB_SECTION_FONT_SIZE)
    bindingsSubLabel.setColor(MYSTIS_ORANGE)

    // Create key binding components
    for (binding <- keyBindings) {
      val actionLabel = new Label(s"${binding.action}:", skin)
      actionLabel.setFontScale(LABEL_FONT_SIZE)
      actionLabel.setColor(MYSTIS_GOLD)
      keyBindingLabels += actionLabel

      val button1 = new TextButton(binding.primary, skin)
      button1.getLabel.setFontScale(BUTTON_FONT_SIZE)
      button1.getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons1 += button1

      val separatorLabel = new Label("|", skin)
      separatorLabel.setFontScale(BUTTON_FONT_SIZE)
      separatorLabel.setColor(MYSTIS_GOLD)
      separatorLabels += separatorLabel

      val button2 = new TextButton(binding.secondary, skin)
      button2.getLabel.setFontScale(BUTTON_FONT_SIZE)
      button2.getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons2 += button2
    }

    // Main Buttons
    returnButton = new TextButton("Retour", skin)
    returnButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    returnButton.getLabel.setColor(MYSTIS_GOLD)
    applyButton = new TextButton("Appliquer", skin)
    applyButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    applyButton.getLabel.setColor(MYSTIS_GOLD)
    resetButton = new TextButton("Reset", skin)
    resetButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    resetButton.getLabel.setColor(MYSTIS_GOLD)

    captureInstructionLabel1 = new Label("Appuyez sur une touche...", skin)
    captureInstructionLabel1.setFontScale(2.5f)
    captureInstructionLabel1.setColor(Color.YELLOW)

    captureInstructionLabel2 = new Label("ESC pour annuler", skin)
    captureInstructionLabel2.setFontScale(2.2f)
    captureInstructionLabel2.setColor(Color.YELLOW)
  }

  // LAYOUT EN GRILLE COMME WORD - 2 COLONNES PRÉCISES
  private def setupLayout(): Unit = {
    val screenWidth = 1920
    val screenHeight = 1080

    // === SYSTÈME DE GRILLE 2 COLONNES ===
    val col1X = PAGE_MARGIN                           // Colonne 1 (Audio + Contrôles)
    val col2X = PAGE_MARGIN + COLUMN_WIDTH + COLUMN_GAP  // Colonne 2 (Affichage + Jeu)

    // TITRE centré
    optionsTitleLabel.pack()
    optionsTitleLabel.setPosition((screenWidth - optionsTitleLabel.getWidth) / 2, screenHeight - MARGIN + 30)

    // === COLONNE 1: AUDIO ===
    var row1Y = screenHeight - ESPACEMENT_TITLE

    // Titre section Audio
    audioSectionLabel.setPosition(col1X, row1Y)
    row1Y -= ESPACEMENT_NORMAL
    
    // Info message
    audioInfoLabel.setPosition(col1X + 20, row1Y)
    row1Y -= ESPACEMENT_SECTION

    // Musique ON/OFF - ligne 1
    musicLabel.setPosition(col1X + 20, row1Y)
    musicToggleButton.setPosition(col1X + 40 + LABEL_WIDTH, row1Y - 20)
    musicToggleButton.setSize(100, COMPONENT_HEIGHT)
    row1Y -= ESPACEMENT_ITEM

    // SFX ON/OFF - ligne 2
    sfxLabel.setPosition(col1X + 20, row1Y)
    sfxToggleButton.setPosition(col1X + 40 + LABEL_WIDTH, row1Y - 20)
    sfxToggleButton.setSize(100, COMPONENT_HEIGHT)
    row1Y -= ESPACEMENT_SECTION

    // === SECTION CONTRÔLES ===
    controlsSectionLabel.setPosition(col1X, row1Y)
    row1Y -= ESPACEMENT_SECTION
    bindingsSubLabel.setPosition(col1X + 20, row1Y)
    row1Y -= ESPACEMENT_SOUS_SECTION

    // Key bindings avec espacement régulier
    for (i <- keyBindings.indices) {
      val currentRowY = row1Y - (i * ESPACEMENT_ITEM)
      
      // Label aligné verticalement avec les boutons
      keyBindingLabels(i).setPosition(col1X + 20, currentRowY + (KEY_BUTTON_HEIGHT - 20) / 2)
      
      // Premier bouton de touche
      keyBindingButtons1(i).setPosition(col1X + 200, currentRowY)
      keyBindingButtons1(i).setSize(KEY_BUTTON_WIDTH, KEY_BUTTON_HEIGHT)
      
      // Séparateur aligné avec les boutons
      separatorLabels(i).setPosition(col1X + 200 + KEY_BUTTON_WIDTH + 5, currentRowY + (KEY_BUTTON_HEIGHT - 20) / 2)
      
      // Deuxième bouton de touche
      keyBindingButtons2(i).setPosition(col1X + 200 + KEY_BUTTON_WIDTH + 30, currentRowY)
      keyBindingButtons2(i).setSize(KEY_BUTTON_WIDTH, KEY_BUTTON_HEIGHT)
    }

    // === COLONNE 2: AFFICHAGE ===
    var row2Y = screenHeight - ESPACEMENT_TITLE

    // Titre section Affichage
    displaySectionLabel.setPosition(col2X, row2Y)
    row2Y -= ESPACEMENT_SECTION

    row2Y -= ESPACEMENT_SECTION

    // === SECTION JEU ===
    gameplaySectionLabel.setPosition(col2X, row2Y)
    row2Y -= ESPACEMENT_SECTION

    // Langue
    languageLabel.setPosition(col2X + 20, row2Y)
    languageButton.setPosition(col2X + 20 + LABEL_WIDTH, row2Y - 20)
    languageButton.setSize(CONTROL_WIDTH, COMPONENT_HEIGHT)

    // Position language dropdown (cachés)
    var dropdownY = row2Y - COMPONENT_HEIGHT - 20
    for (button <- languageDropdownButtons) {
      button.setPosition(col2X + 20 + LABEL_WIDTH, dropdownY)
      button.setSize(CONTROL_WIDTH, COMPONENT_HEIGHT)
      dropdownY -= COMPONENT_HEIGHT
    }
    row2Y -= ESPACEMENT_ITEM

    // Sous-titres
    subtitlesLabel.setPosition(col2X + 20, row2Y - 20)
    subtitlesButton.setPosition(col2X + 20 + LABEL_WIDTH, row2Y - 40)
    subtitlesButton.setSize(CONTROL_WIDTH, COMPONENT_HEIGHT)

    // === BOUTONS PRINCIPAUX CENTRÉS ===
    val buttonY = 80
    val buttonSpacing = 140
    resetButton.setPosition(screenWidth/2 - buttonSpacing - 60, buttonY)
    resetButton.setSize(120, 45)
    applyButton.setPosition(screenWidth/2 - 60, buttonY)
    applyButton.setSize(120, 45)
    returnButton.setPosition(screenWidth/2 + buttonSpacing - 60, buttonY)
    returnButton.setSize(120, 45)
  }

  private def addEventListeners(): Unit = {
    // Boutons ON/OFF audio
    musicToggleButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        val isOn = musicToggleButton.getText.toString == "ON"
        if (isOn) {
          musicToggleButton.setText("OFF")
          musicToggleButton.getLabel.setColor(MYSTIS_DARK_ORANGE)
          AudioManager.toggleMusic(false)
          GameSettings.musicEnabled = false // Sauvegarder immédiatement
        } else {
          musicToggleButton.setText("ON")
          musicToggleButton.getLabel.setColor(MYSTIS_GOLD)
          AudioManager.toggleMusic(true)
          GameSettings.musicEnabled = true // Sauvegarder immédiatement
        }
      }
    })

    sfxToggleButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        val isOn = sfxToggleButton.getText.toString == "ON"
        if (isOn) {
          sfxToggleButton.setText("OFF")
          sfxToggleButton.getLabel.setColor(MYSTIS_DARK_ORANGE)
          AudioManager.toggleSFX(false)
          GameSettings.sfxEnabled = false // Sauvegarder immédiatement
        } else {
          sfxToggleButton.setText("ON")
          sfxToggleButton.getLabel.setColor(MYSTIS_GOLD)
          AudioManager.toggleSFX(true)
          GameSettings.sfxEnabled = true // Sauvegarder immédiatement
        }
      }
    })


    // Language dropdown
    languageButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        toggleLanguageDropdown()
      }
    })

    for (i <- languageDropdownButtons.indices) {
      val index = i
      languageDropdownButtons(i).addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          selectLanguage(index)
        }
      })
    }

    // Subtitles toggle
    subtitlesButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        toggleSubtitles()
      }
    })

    // Key binding buttons
    for (i <- keyBindings.indices) {
      val index = i
      keyBindingButtons1(i).addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          startKeyCapture(index, primary = true)
        }
      })

      keyBindingButtons2(i).addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          startKeyCapture(index, primary = false)
        }
      })
    }

    // Main buttons
    returnButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        Logger.log(s"Retour vers ${GameSettings.previousState}")
        if (unifiedApp != null) {
          GameSettings.previousState match {
            case "GAME" =>
              // Retourner au jeu et reprendre
              GameSettings.isGamePaused = true // On garde la pause pour permettre de reprendre
              unifiedApp.changeState(AppState.GAME)
            case _ =>
              unifiedApp.changeState(AppState.MENU)
          }
        } else {
          Gdx.app.exit()
        }
      }
    })

    applyButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        applySettings()
      }
    })

    resetButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        resetToDefaults()
      }
    })
  }

  private def startKeyCapture(index: Int, primary: Boolean): Unit = {
    // Cancel any previous capture
    if (waitingForKey) {
      cancelKeyCapture()
    }

    waitingForKey = true
    captureIndex = index
    capturePrimary = primary

    val button = if (primary) keyBindingButtons1(index) else keyBindingButtons2(index)
    button.setText("Appuyez...")
    button.getLabel.setColor(Color.YELLOW)

    Logger.log(s"En attente d'une touche pour: ${keyBindings(index).action} (${if (primary) "principale" else "secondaire"})")
  }

  private def cancelKeyCapture(): Unit = {
    if (waitingForKey && captureIndex >= 0) {
      val button = if (capturePrimary) keyBindingButtons1(captureIndex) else keyBindingButtons2(captureIndex)
      val originalKey = if (capturePrimary) keyBindings(captureIndex).primary else keyBindings(captureIndex).secondary
      button.setText(originalKey)
      button.getLabel.setColor(MYSTIS_GOLD)
    }

    waitingForKey = false
    captureIndex = -1
  }

  private def toggleLanguageDropdown(): Unit = {
    languageDropdownVisible = !languageDropdownVisible
    languageButton.setText(s"${languages(currentLanguage)} ${if (languageDropdownVisible) "▲" else "▼"}")

    // Add or remove dropdown buttons from stage
    if (languageDropdownVisible) {
      for (button <- languageDropdownButtons) {
        stage.addActor(button)
      }
    } else {
      for (button <- languageDropdownButtons) {
        button.remove()
      }
    }
  }

  private def selectLanguage(index: Int): Unit = {
    currentLanguage = index
    languageButton.setText(s"${languages(currentLanguage)} ▼")
    languageDropdownVisible = false

    // Remove dropdown buttons from stage
    for (button <- languageDropdownButtons) {
      button.remove()
    }

    Logger.log(s"Langue selectionnee: ${languages(currentLanguage)}")
  }

  private def toggleSubtitles(): Unit = {
    subtitlesEnabled = !subtitlesEnabled
    subtitlesButton.setText(if (subtitlesEnabled) "ACTIVES" else "DESACTIVES")
    Logger.log(s"Sous-titres: ${if (subtitlesEnabled) "actives" else "desactives"}")
  }
  

  private def applySettings(): Unit = {
    Logger.log("Application des parametres...")
    
    // Sauvegarder les paramètres dans GameSettings (audio déjà sauvegardé au clic)
    GameSettings.subtitlesEnabled = subtitlesEnabled
    
    Logger.log(s"Langue: ${languages(currentLanguage)}")
    Logger.log(s"Sous-titres: ${if (subtitlesEnabled) "actives" else "desactives"}")
    Logger.log(s"Musique: ${if (GameSettings.musicEnabled) "activee" else "desactivee"}")
    Logger.log(s"SFX: ${if (GameSettings.sfxEnabled) "actives" else "desactives"}")
    Logger.log("Parametres appliqués et sauvegardés !")
  }

  private def resetToDefaults(): Unit = {
    // Reset audio to defaults
    musicToggleButton.setText("ON")
    musicToggleButton.getLabel.setColor(MYSTIS_GOLD)
    AudioManager.toggleMusic(true)
    GameSettings.musicEnabled = true
    
    sfxToggleButton.setText("ON")
    sfxToggleButton.getLabel.setColor(MYSTIS_GOLD)
    AudioManager.toggleSFX(true)
    GameSettings.sfxEnabled = true

    // Reset display and gameplay
    currentLanguage = 0
    languageButton.setText(s"${languages(currentLanguage)} ▼")
    subtitlesEnabled = true
    subtitlesButton.setText("ACTIVES")
    
    // Reset GameSettings display options
    GameSettings.subtitlesEnabled = true

    // Reset key bindings to defaults in GameSettings.Controls
    import com.badlogic.gdx.Input
    GameSettings.Controls.moveUp = (Input.Keys.W, Input.Keys.NUMPAD_8)
    GameSettings.Controls.moveDown = (Input.Keys.S, Input.Keys.NUMPAD_5)
    GameSettings.Controls.moveLeft = (Input.Keys.A, Input.Keys.NUMPAD_4)
    GameSettings.Controls.moveRight = (Input.Keys.D, Input.Keys.NUMPAD_6)
    GameSettings.Controls.jump = (Input.Keys.SPACE, Input.Keys.NUMPAD_0)
    GameSettings.Controls.attack = (-1, Input.Keys.CONTROL_LEFT)
    GameSettings.Controls.magic = (-2, Input.Keys.ALT_LEFT)
    GameSettings.Controls.inventory = (Input.Keys.I, Input.Keys.TAB)
    GameSettings.Controls.menu = (Input.Keys.ESCAPE, Input.Keys.M)
    GameSettings.Controls.map = (Input.Keys.M, Input.Keys.F1)

    // Update UI to reflect the reset values
    for (i <- keyBindings.indices) {
      val binding = keyBindings(i)
      binding.gameAction match {
        case "moveUp" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._2)
        case "moveDown" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._2)
        case "moveLeft" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._2)
        case "moveRight" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._2)
        case "jump" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.jump._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.jump._2)
        case "attack" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.attack._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.attack._2)
        case "magic" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.magic._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.magic._2)
        case "inventory" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.inventory._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.inventory._2)
        case "menu" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.menu._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.menu._2)
        case "map" => 
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.map._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.map._2)
        case _ =>
          // Cas par défaut pour les actions non reconnues
      }
      
      keyBindingButtons1(i).setText(binding.primary)
      keyBindingButtons2(i).setText(binding.secondary)
      keyBindingButtons1(i).getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons2(i).getLabel.setColor(MYSTIS_GOLD)
    }


    Logger.log("Parametres reinitialises")
  }

  private def addComponentsToStage(): Unit = {
    // Title
    stage.addActor(optionsTitleLabel)

    // Audio section
    stage.addActor(audioSectionLabel)
    stage.addActor(audioInfoLabel)
    stage.addActor(musicLabel)
    stage.addActor(musicToggleButton)
    stage.addActor(sfxLabel)
    stage.addActor(sfxToggleButton)

    // Display section
    stage.addActor(displaySectionLabel)

    // Gameplay section
    stage.addActor(gameplaySectionLabel)
    stage.addActor(languageLabel)
    stage.addActor(languageButton)
    stage.addActor(subtitlesLabel)
    stage.addActor(subtitlesButton)

    // Controls section
    stage.addActor(controlsSectionLabel)
    stage.addActor(bindingsSubLabel)

    // Key bindings
    for (i <- keyBindings.indices) {
      stage.addActor(keyBindingLabels(i))
      stage.addActor(keyBindingButtons1(i))
      stage.addActor(separatorLabels(i))
      stage.addActor(keyBindingButtons2(i))
    }

    // Main buttons
    stage.addActor(returnButton)
    stage.addActor(applyButton)
    stage.addActor(resetButton)
  }

  def onGraphicRender(g: GdxGraphics): Unit = {
    // Dessiner le stage sans fond noir
    import com.badlogic.gdx.Gdx
    import com.badlogic.gdx.graphics.GL20

    Gdx.gl.glClearColor(MYSTIS_BROWN.r, MYSTIS_BROWN.g, MYSTIS_BROWN.b, 1.0f) // Brun très sombre
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


    // Activer le blending pour la transparence
    Gdx.gl.glEnable(GL20.GL_BLEND)
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

    // Show key capture instructions avec cadre doré
    if (waitingForKey) {
      val screenWidth = 1920
      val screenHeight = 1080
      val boxWidth = 400
      val boxHeight = 200
      val boxX = (screenWidth / 2)
      val boxY = (screenHeight / 2)

      // Dessiner le texte en blanc au centre du cadre
      // Positionner et afficher les Labels
      if (!captureInstructionVisible) {
        captureInstructionLabel1.setPosition(boxX - boxWidth/2, boxY/3*2 + 30)
        captureInstructionLabel2.setPosition(boxX - boxWidth/2, boxY/3*2 - 30)
        stage.addActor(captureInstructionLabel1)
        stage.addActor(captureInstructionLabel2)
        captureInstructionVisible = true
      }
    } else {
      if (captureInstructionVisible) {
        captureInstructionLabel1.remove()
        captureInstructionLabel2.remove()
        captureInstructionVisible = false
      }
    }

    stage.act()
    stage.draw()
  }

  def onKeyDown(keycode: Int): Unit = {
    if (GameSettings.Controls.isKeyPressed("menu", keycode) && !waitingForKey) {
      if (unifiedApp != null) {
        unifiedApp.changeState(AppState.MENU)
      } else {
        Gdx.app.exit()
      }
    }
  }

  def dispose(): Unit = {
    // Nettoyer l'InputProcessor avant de quitter
    Gdx.input.setInputProcessor(null)
    
    if (stage != null) stage.dispose()
    if (skin != null) skin.dispose()
  }
}

object MystisOptionsMenuLauncher extends App {
  new MystisOptionsMenu()
}