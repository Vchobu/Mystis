package ch.hevs.gdx2d.hello

// === IMPORTS LIBGDX ET SCALA ===
import ch.hevs.gdx2d.desktop.PortableApplication
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.utils.Logger
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.{Input, InputMultiplexer, InputProcessor}
import com.badlogic.gdx.graphics.g2d.SpriteBatch

import scala.collection.mutable.ArrayBuffer                   // Collection mutable pour les éléments dynamiques

/**
 * Menu des options avancé de Mystis.
 *
 * Interface de configuration complète permettant de modifier :
 * - Paramètres audio (musique, effets sonores avec toggle ON/OFF)
 * - Configuration des touches en temps réel (6 actions configurables)
 * - Paramètres d'affichage (section préparée pour futurs développements)
 * - Paramètres de gameplay (section préparée pour futurs développements)
 *
 * ARCHITECTURE :
 * - Layout en 2 colonnes avec positionnement absolu précis
 * - Système de capture de touches sophistiqué (clavier + souris)
 * - Sauvegarde immédiate des paramètres
 * - Navigation intelligente (retour vers menu ou jeu selon provenance)
 *
 * DESIGN PATTERN :
 * - Séparation claire : données ↔ logique ↔ présentation
 * - Constantes centralisées pour faciliter la maintenance
 * - Gestion d'état robuste avec variables de contrôle
 *
 * @param unifiedApp Référence optionnelle vers l'application principale pour la navigation entre états
 */
class MystisOptionsMenu(unifiedApp: MystisUnifiedApp = null) {

  // === COMPOSANTS UI CORE ===
  var stage: Stage = _                      // Scène LibGDX contenant tous les éléments UI
  var skin: Skin = _                        // Thème visuel/style des composants UI (chargé depuis JSON)
  var inputMultiplexer: InputMultiplexer = _ // Gestionnaire multi-sources pour entrées UI + capture touches

  // ========================================
  // SYSTÈME DE CONSTANTES CENTRALISÉES
  // ========================================
  // Toutes les valeurs de mise en page sont définies ici pour faciliter les modifications
  // Changer ces valeurs redimensionne automatiquement toute l'interface

  // === HIÉRARCHIE DES TAILLES DE POLICE ===
  val TITLE_FONT_SIZE = 5f          // Titre principal "Options" (énorme)
  val SECTION_FONT_SIZE = 2.0f      // Sections principales "=== AUDIO ===" (grand)
  val SUB_SECTION_FONT_SIZE = 1.6f  // Sous-sections "--- Keyboard Shortcuts ---" (moyen-grand)
  val LABEL_FONT_SIZE = 1.5f        // Labels et séparateurs (moyen)
  val NORMAL_FONT_SIZE = 1.5f       // Texte normal et valeurs affichées (moyen)
  val BUTTON_FONT_SIZE = 1.5f       // Texte des boutons (moyen)

  // === ESPACEMENTS VERTICAUX (en pixels) ===
  val ESPACEMENT_TITLE = 200        // Espace après le titre principal (très grand)
  val ESPACEMENT_SECTION = 60       // Espace après une section principale (grand)
  val ESPACEMENT_SOUS_SECTION = 60  // Espace après une sous-section (grand)
  val ESPACEMENT_NORMAL = 35        // Espacement standard entre éléments (moyen)
  val ESPACEMENT_ITEM = 50          // Espacement entre items individuels (moyen-grand)

  // === SYSTÈME DE GRILLE À 2 COLONNES (inspiré de Microsoft Word) ===
  val PAGE_MARGIN = 100             // Marge générale de la page
  val COLUMN_WIDTH = 800            // Largeur de chaque colonne (en pixels)
  val COLUMN_GAP = 300              // Espacement horizontal entre les 2 colonnes
  val LABEL_WIDTH = 180             // Largeur fixe réservée aux labels (ex: "Music:")
  val CONTROL_WIDTH = 200           // Largeur des contrôles interactifs (boutons, sliders)
  val VALUE_WIDTH = 80              // Largeur des valeurs numériques affichées
  val LINE_HEIGHT = 50              // Hauteur standard d'une ligne

  // === DIMENSIONS SPÉCIFIQUES DES BOUTONS DE TOUCHES ===
  val KEY_BUTTON_WIDTH = 90         // Largeur des boutons de configuration des touches (ex: "W", "SPACE")
  val KEY_BUTTON_HEIGHT = 40        // Hauteur des boutons de configuration des touches

  // === CALCULS AUTOMATIQUES BASÉS SUR LES POLICES ===
  val FONT_SCALE_FACTOR = NORMAL_FONT_SIZE / 1.0f            // Facteur d'échelle général
  val COMPONENT_HEIGHT = (40 * FONT_SCALE_FACTOR).toInt      // Hauteur auto des composants UI
  val MARGIN = (120 * FONT_SCALE_FACTOR).toInt               // Marge auto basée sur la police

  // ========================================
  // PALETTE DE COULEURS MYSTIS
  // ========================================
  // Couleurs cohérentes avec l'identité visuelle du jeu
  val MYSTIS_GOLD = new Color(1.0f, 0.84f, 0.0f, 1.0f)        // Or doré - éléments importants/actifs
  val MYSTIS_ORANGE = new Color(1.0f, 0.65f, 0.0f, 1.0f)      // Orange chaud - titres de sections
  val MYSTIS_DARK_ORANGE = new Color(0.8f, 0.4f, 0.0f, 1.0f)  // Orange foncé - éléments désactivés
  val MYSTIS_BLUE = new Color(0.2f, 0.5f, 0.9f, 1.0f)         // Bleu mystique - accents spéciaux
  val MYSTIS_DARK = new Color(0.1f, 0.1f, 0.2f, 0.8f)         // Fond semi-transparent - overlays
  val MYSTIS_BROWN = new Color(0.15f, 0.1f, 0.05f, 1.0f)      // Brun très sombre - arrière-plan principal

  // ========================================
  // ÉTAT DE CAPTURE DES TOUCHES
  // ========================================
  // Variables de contrôle pour le système de reconfiguration des touches en temps réel
  var waitingForKey: Boolean = false    // État : true = en attente d'une pression de touche
  var captureIndex: Int = -1           // Index de l'action en cours de reconfiguration (0-5)
  var capturePrimary: Boolean = true   // true = touche primaire, false = touche secondaire

  // ========================================
  // DÉCLARATION DE TOUS LES COMPOSANTS UI
  // ========================================
  // Organisation par sections pour une maintenance facile

  // === TITRE PRINCIPAL ===
  var optionsTitleLabel: Label = _      // Label "Options" centré en haut

  // === SECTION AUDIO ===
  var audioSectionLabel: Label = _      // Titre "=== AUDIO ==="
  var audioInfoLabel: Label = _         // Info "Use system volume controls"
  var musicLabel: Label = _             // Label "Music:"
  var musicToggleButton: TextButton = _ // Bouton ON/OFF pour la musique
  var sfxLabel: Label = _               // Label "Sound Effects:"
  var sfxToggleButton: TextButton = _   // Bouton ON/OFF pour les effets sonores

  // === SECTION AFFICHAGE ===
  var displaySectionLabel: Label = _    // Titre "=== DISPLAY ===" (section préparée)

  // === SECTION GAMEPLAY ===
  var gameplaySectionLabel: Label = _   // Titre "=== GAMEPLAY ===" (section préparée)

  // === SECTION CONTRÔLES ===
  var controlsSectionLabel: Label = _   // Titre "=== CONTROLS ==="
  var bindingsSubLabel: Label = _       // Sous-titre "--- Keyboard Shortcuts ---"

  // Collections dynamiques pour la configuration des touches
  // Ces ArrayBuffer permettent de gérer un nombre variable d'actions configurables
  var keyBindingLabels: ArrayBuffer[Label] = ArrayBuffer()           // Labels des actions (ex: "Move Forward:")
  var keyBindingButtons1: ArrayBuffer[TextButton] = ArrayBuffer()    // Boutons touches primaires (ex: "W")
  var keyBindingButtons2: ArrayBuffer[TextButton] = ArrayBuffer()    // Boutons touches secondaires (ex: "↑")
  var separatorLabels: ArrayBuffer[Label] = ArrayBuffer()            // Séparateurs visuels "|"

  // === INSTRUCTIONS DE CAPTURE DE TOUCHES ===
  var captureInstructionLabel1: Label = _    // "Press a key..." (affiché au centre pendant capture)
  var captureInstructionLabel2: Label = _    // "ESC to cancel" (affiché au centre pendant capture)
  var captureInstructionVisible: Boolean = false // État d'affichage des instructions

  // === BOUTONS PRINCIPAUX ===
  var returnButton: TextButton = _      // Bouton "Return" (retour menu/jeu)
  var applyButton: TextButton = _       // Bouton "Apply" (appliquer paramètres)
  var resetButton: TextButton = _       // Bouton "Reset" (réinitialiser tout)

  /**
   * Structure de données pour une liaison de touche.
   * Relie une action du jeu à ses touches primaire et secondaire.
   *
   * @param action Description lisible par l'utilisateur (ex: "Move Forward")
   * @param gameAction Identifiant interne pour GameSettings (ex: "moveUp")
   * @param primary Nom affiché de la touche primaire (ex: "W")
   * @param secondary Nom affiché de la touche secondaire (ex: "↑")
   */
  case class KeyBinding(action: String, gameAction: String, var primary: String, var secondary: String)

  // === DONNÉES DE CONFIGURATION DES TOUCHES ===
  // Collection de toutes les actions configurables du jeu
  // Synchronisée automatiquement avec GameSettings.Controls pour maintenir la cohérence
  val keyBindings = ArrayBuffer(
    // Chaque ligne définit une action avec ses touches par défaut actuelles
    KeyBinding("Move Forward", "moveUp",
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveUp._2)),
    KeyBinding("Move Backward", "moveDown",
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveDown._2)),
    KeyBinding("Move Left", "moveLeft",
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveLeft._2)),
    KeyBinding("Move Right", "moveRight",
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.moveRight._2)),
    KeyBinding("Ultimate", "ultimate",
      GameSettings.Controls.getKeyName(GameSettings.Controls.ultimate._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.ultimate._2)),
    KeyBinding("Menu", "menu",
      GameSettings.Controls.getKeyName(GameSettings.Controls.menu._1),
      GameSettings.Controls.getKeyName(GameSettings.Controls.menu._2))
  )

  /**
   * Initialisation complète du menu des options.
   *
   * SÉQUENCE D'INITIALISATION :
   * 1. Configuration de la scène LibGDX avec SpriteBatch personnalisé
   * 2. Chargement du thème UI depuis le fichier JSON
   * 3. Configuration du multiplexeur d'entrées (UI + capture touches)
   * 4. Création de tous les composants UI
   * 5. Positionnement précis des éléments
   * 6. Connexion des événements aux boutons
   * 7. Ajout final des composants à la scène
   *
   * NOTE : Préserve la musique en cours selon la provenance (menu ou gameplay)
   */
  def onInit(): Unit = {
    // Les options préservent la musique en cours (menu ou gameplay selon d'où on vient)

    // === CONFIGURATION DE LA SCÈNE LIBGDX ===
    import com.badlogic.gdx.utils.viewport.ScreenViewport
    val customBatch = new SpriteBatch()     // SpriteBatch personnalisé pour un contrôle précis
    val viewport = new ScreenViewport()     // Viewport qui s'adapte à la taille d'écran
    stage = new Stage(viewport, customBatch)

    // Chargement du thème UI depuis le fichier JSON
    skin = new Skin(Gdx.files.internal("gdx2d-helloDesktop/data/ui/uiskin.json"))

    // === CONFIGURATION DU SYSTÈME D'ENTRÉES ===
    // Le multiplexeur permet de gérer simultanément :
    // - Les interactions UI normales (clics sur boutons)
    // - La capture de touches personnalisée pour la reconfiguration
    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(stage)                    // Entrées UI standard
    inputMultiplexer.addProcessor(createKeyInputProcessor()) // Capture touches personnalisée
    setupInput()                                           // Navigation ESC
    Gdx.input.setInputProcessor(inputMultiplexer)           // Activation du gestionnaire

    // === SÉQUENCE D'INITIALISATION COMPLÈTE ===
    initializeComponents()  // Créer tous les éléments UI avec leurs propriétés
    setupLayout()          // Positionner précisément chaque élément à l'écran
    addEventListeners()    // Connecter les actions aux boutons interactifs
    addComponentsToStage() // Ajouter tous les éléments à la scène pour affichage
  }

  /**
   * Crée un processeur d'entrées spécialisé pour la capture de touches.
   *
   * Ce processeur intercepte les événements clavier et souris quand le système
   * est en mode capture (waitingForKey = true). Il permet de :
   * - Capturer n'importe quelle touche du clavier
   * - Capturer les boutons de souris (gauche, droite, molette)
   * - Ignorer les entrées normales pendant la capture
   *
   * @return InputProcessor configuré pour la capture de touches
   */
  private def createKeyInputProcessor(): InputProcessor = {
    new InputProcessor {
      /**
       * Gestionnaire des pressions de touches pendant la capture.
       * @param keycode Code LibGDX de la touche pressée
       * @return true si l'événement a été traité, false sinon
       */
      override def keyDown(keycode: Int): Boolean = {
        if (waitingForKey) {
          captureKey(keycode)  // Traiter la capture de touche
          true                 // Marquer l'événement comme traité
        } else {
          false               // Laisser passer l'événement aux autres processeurs
        }
      }

      // Méthodes obligatoires de l'interface InputProcessor mais non utilisées
      override def keyUp(keycode: Int): Boolean = false
      override def keyTyped(character: Char): Boolean = false

      /**
       * Gestionnaire des clics de souris pendant la capture.
       * Permet d'assigner des boutons de souris aux actions du jeu.
       */
      override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
        if (waitingForKey) {
          captureMouseButton(button)  // Traiter la capture de bouton de souris
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

  /**
   * Traite la capture d'une touche du clavier.
   *
   * FONCTIONNEMENT :
   * 1. Vérifie si c'est Échap (pour annuler)
   * 2. Convertit le code touche en nom lisible
   * 3. Met à jour GameSettings.Controls avec la nouvelle assignation
   * 4. Met à jour l'affichage du bouton correspondant
   * 5. Termine le mode capture
   *
   * @param keycode Code LibGDX de la touche pressée (ex: Input.Keys.W = 29)
   */
  private def captureKey(keycode: Int): Unit = {
    // Permettre à Échap d'annuler la capture sans assigner de touche
    if (keycode == Input.Keys.ESCAPE) {
      cancelKeyCapture()
      return
    }

    // Conversion code → nom lisible (ex: 29 → "W")
    val keyName = GameSettings.Controls.getKeyName(keycode)
    val binding = keyBindings(captureIndex)

    // === MISE À JOUR DU SYSTÈME CENTRAL ===
    // Sauvegarder la nouvelle assignation dans GameSettings.Controls
    GameSettings.Controls.updateKeyBinding(binding.gameAction, capturePrimary, keycode)

    // === MISE À JOUR DE L'INTERFACE ===
    // Modifier l'affichage selon si c'est la touche primaire ou secondaire
    if (capturePrimary) {
      binding.primary = keyName
      keyBindingButtons1(captureIndex).setText(keyName)
      keyBindingButtons1(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    } else {
      binding.secondary = keyName
      keyBindingButtons2(captureIndex).setText(keyName)
      keyBindingButtons2(captureIndex).getLabel.setColor(MYSTIS_GOLD)
    }

    // === TERMINER LA CAPTURE ===
    waitingForKey = false
    captureIndex = -1
    Logger.log(s"New key assigned: $keyName for ${binding.action}")
  }

  /**
   * Traite la capture d'un bouton de souris.
   *
   * Similaire à captureKey mais pour les boutons de souris.
   * Convertit les boutons de souris en codes internes négatifs :
   * - Clic gauche → -1
   * - Clic droit → -2
   * - Molette/clic milieu → -3
   *
   * @param button Code LibGDX du bouton de souris (Input.Buttons.LEFT, RIGHT, MIDDLE)
   */
  private def captureMouseButton(button: Int): Unit = {
    // === CONVERSION BOUTON SOURIS → CODE INTERNE ===
    // Les codes négatifs distinguent souris (négatif) du clavier (positif)
    val keycode = button match {
      case Input.Buttons.LEFT => -1      // Clic gauche
      case Input.Buttons.RIGHT => -2     // Clic droit
      case Input.Buttons.MIDDLE => -3    // Molette/clic milieu
      case _ => -button                  // Autres boutons (rares)
    }

    val buttonName = GameSettings.Controls.getKeyName(keycode)
    val binding = keyBindings(captureIndex)

    // === MISE À JOUR IDENTIQUE À captureKey ===
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
    Logger.log(s"New mouse button assigned: $buttonName for ${binding.action}")
  }

  /**
   * Initialise tous les composants UI avec leurs propriétés visuelles.
   *
   * ORGANISATION :
   * - Titre principal
   * - Section Audio (labels + boutons ON/OFF)
   * - Section Affichage (préparée pour futurs paramètres)
   * - Section Gameplay (préparée pour futurs paramètres)
   * - Section Contrôles (génération dynamique des boutons de touches)
   * - Boutons principaux (Return, Apply, Reset)
   * - Labels d'instruction pour la capture
   *
   * Chaque composant est configuré avec :
   * - Taille de police appropriée
   * - Couleur selon le thème Mystis
   * - Style cohérent
   */
  private def initializeComponents(): Unit = {
    // === TITRE PRINCIPAL ===
    optionsTitleLabel = new Label("Options", skin, "default")
    optionsTitleLabel.setFontScale(TITLE_FONT_SIZE)
    optionsTitleLabel.setColor(MYSTIS_GOLD)

    // ========================================
    // SECTION AUDIO
    // ========================================

    // Titre de section avec style distinctif
    audioSectionLabel = new Label("=== AUDIO ===", skin, "default")
    audioSectionLabel.setFontScale(SECTION_FONT_SIZE)
    audioSectionLabel.setColor(MYSTIS_GOLD)

    // Message informatif pour l'utilisateur
    audioInfoLabel = new Label("Use system volume controls", skin)
    audioInfoLabel.setFontScale(LABEL_FONT_SIZE)
    audioInfoLabel.setColor(MYSTIS_ORANGE)

    // === CONTRÔLE MUSIQUE ===
    // Label + bouton ON/OFF avec état basé sur GameSettings
    musicLabel = new Label("Music:", skin)
    musicLabel.setFontScale(LABEL_FONT_SIZE)
    musicLabel.setColor(MYSTIS_ORANGE)

    // Bouton avec texte et couleur dynamiques selon l'état actuel
    musicToggleButton = new TextButton(if (GameSettings.musicEnabled) "ON" else "OFF", skin)
    musicToggleButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    musicToggleButton.getLabel.setColor(if (GameSettings.musicEnabled) MYSTIS_GOLD else MYSTIS_DARK_ORANGE)

    // === CONTRÔLE EFFETS SONORES ===
    // Structure identique à la musique
    sfxLabel = new Label("Sound Effects:", skin)
    sfxLabel.setFontScale(LABEL_FONT_SIZE)
    sfxLabel.setColor(MYSTIS_ORANGE)

    sfxToggleButton = new TextButton(if (GameSettings.sfxEnabled) "ON" else "OFF", skin)
    sfxToggleButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    sfxToggleButton.getLabel.setColor(if (GameSettings.sfxEnabled) MYSTIS_GOLD else MYSTIS_DARK_ORANGE)

    // ========================================
    // SECTIONS PRÉPARÉES POUR L'AVENIR
    // ========================================

    // === SECTION AFFICHAGE ===
    // Prête pour ajouter : résolution, plein écran, luminosité, etc.
    displaySectionLabel = new Label("=== DISPLAY ===", skin)
    displaySectionLabel.setFontScale(SECTION_FONT_SIZE)
    displaySectionLabel.setColor(MYSTIS_GOLD)

    // === SECTION GAMEPLAY ===
    // Prête pour ajouter : difficulté, vitesse de jeu, langues, etc.
    gameplaySectionLabel = new Label("=== GAMEPLAY ===", skin)
    gameplaySectionLabel.setFontScale(SECTION_FONT_SIZE)
    gameplaySectionLabel.setColor(MYSTIS_GOLD)
    // Note: Langues et sous-titres supprimés pour garder seulement l'essentiel

    // ========================================
    // SECTION CONTRÔLES - GÉNÉRATION DYNAMIQUE
    // ========================================

    controlsSectionLabel = new Label("=== CONTROLS ===", skin)
    controlsSectionLabel.setFontScale(SECTION_FONT_SIZE)
    controlsSectionLabel.setColor(MYSTIS_GOLD)

    bindingsSubLabel = new Label("--- Keyboard Shortcuts ---", skin)
    bindingsSubLabel.setFontScale(SUB_SECTION_FONT_SIZE)
    bindingsSubLabel.setColor(MYSTIS_ORANGE)

    // === GÉNÉRATION DYNAMIQUE DES CONTRÔLES DE TOUCHES ===
    // Pour chaque action dans keyBindings, créer automatiquement :
    // Label d'action + Bouton primaire + Séparateur + Bouton secondaire
    for (binding <- keyBindings) {
      // Label descriptif de l'action (ex: "Move Forward:")
      val actionLabel = new Label(s"${binding.action}:", skin)
      actionLabel.setFontScale(LABEL_FONT_SIZE)
      actionLabel.setColor(MYSTIS_GOLD)
      keyBindingLabels += actionLabel

      // Bouton pour la touche primaire (ex: "W")
      val button1 = new TextButton(binding.primary, skin)
      button1.getLabel.setFontScale(BUTTON_FONT_SIZE)
      button1.getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons1 += button1

      // Séparateur visuel entre les deux boutons
      val separatorLabel = new Label("|", skin)
      separatorLabel.setFontScale(BUTTON_FONT_SIZE)
      separatorLabel.setColor(MYSTIS_GOLD)
      separatorLabels += separatorLabel

      // Bouton pour la touche secondaire (ex: "↑")
      val button2 = new TextButton(binding.secondary, skin)
      button2.getLabel.setFontScale(BUTTON_FONT_SIZE)
      button2.getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons2 += button2
    }

    // ========================================
    // BOUTONS PRINCIPAUX
    // ========================================

    returnButton = new TextButton("Return", skin)
    returnButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    returnButton.getLabel.setColor(MYSTIS_GOLD)

    applyButton = new TextButton("Apply", skin)
    applyButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    applyButton.getLabel.setColor(MYSTIS_GOLD)

    resetButton = new TextButton("Reset", skin)
    resetButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    resetButton.getLabel.setColor(MYSTIS_GOLD)

    // ========================================
    // INSTRUCTIONS DE CAPTURE DE TOUCHES
    // ========================================
    // Labels qui apparaissent au centre de l'écran pendant la capture

    captureInstructionLabel1 = new Label("Press a key...", skin)
    captureInstructionLabel1.setFontScale(2.5f)
    captureInstructionLabel1.setColor(Color.YELLOW)

    captureInstructionLabel2 = new Label("ESC to cancel", skin)
    captureInstructionLabel2.setFontScale(2.2f)
    captureInstructionLabel2.setColor(Color.YELLOW)
  }

  /**
   * Configure la mise en page de tous les éléments UI.
   *
   * SYSTÈME DE GRILLE À 2 COLONNES :
   * - Colonne 1 (gauche) : Audio + Contrôles
   * - Colonne 2 (droite) : Affichage + Gameplay
   *
   * MÉTHODE DE POSITIONNEMENT :
   * - Calcul des positions des colonnes
   * - Placement du titre centré en haut
   * - Positionnement séquentiel dans chaque colonne avec Y décroissant
   * - Boutons principaux centrés en bas
   *
   * AVANTAGES :
   * - Positionnement précis au pixel près
   * - Layout prévisible et stable
   * - Facile à modifier via les constantes
   */
  private def setupLayout(): Unit = {
    val screenWidth = 1920   // Résolution cible en largeur
    val screenHeight = 1080  // Résolution cible en hauteur

    // ========================================
    // CALCUL DES POSITIONS DES COLONNES
    // ========================================
    val col1X = PAGE_MARGIN                           // X de la colonne 1 (Audio + Contrôles)
    val col2X = PAGE_MARGIN + COLUMN_WIDTH + COLUMN_GAP  // X de la colonne 2 (Affichage + Gameplay)

    // === TITRE CENTRÉ EN HAUT ===
    optionsTitleLabel.pack()  // Calculer automatiquement la taille du label
    // Centrer horizontalement : (largeur écran - largeur label) / 2
    optionsTitleLabel.setPosition((screenWidth - optionsTitleLabel.getWidth) / 2, screenHeight - MARGIN + 30)

    // ========================================
    // COLONNE 1: SECTION AUDIO + CONTRÔLES
    // ========================================
    var row1Y = screenHeight - ESPACEMENT_TITLE  // Y de départ pour la colonne 1

    // === SECTION AUDIO ===
    // Titre de section
    audioSectionLabel.setPosition(col1X, row1Y)
    row1Y -= ESPACEMENT_NORMAL

    // Message informatif avec léger décalage à droite
    audioInfoLabel.setPosition(col1X + 20, row1Y)
    row1Y -= ESPACEMENT_SECTION

    // === LIGNE MUSIQUE : Label + Bouton ON/OFF ===
    // Alignement : Label à gauche, Bouton à distance fixe du label
    musicLabel.setPosition(col1X + 20, row1Y)
    musicToggleButton.setPosition(col1X + 40 + LABEL_WIDTH, row1Y - 20)  // Légèrement plus bas pour alignement visuel
    musicToggleButton.setSize(100, COMPONENT_HEIGHT)
    row1Y -= ESPACEMENT_ITEM

    // === LIGNE EFFETS SONORES : Même structure ===
    sfxLabel.setPosition(col1X + 20, row1Y)
    sfxToggleButton.setPosition(col1X + 40 + LABEL_WIDTH, row1Y - 20)
    sfxToggleButton.setSize(100, COMPONENT_HEIGHT)
    row1Y -= ESPACEMENT_SECTION

    // === SECTION CONTRÔLES ===
    controlsSectionLabel.setPosition(col1X, row1Y)
    row1Y -= ESPACEMENT_SECTION
    bindingsSubLabel.setPosition(col1X + 20, row1Y)
    row1Y -= ESPACEMENT_SOUS_SECTION

    // === GÉNÉRATION DYNAMIQUE DES LIGNES DE TOUCHES ===
    // Chaque ligne : Label d'action + Bouton primaire + "|" + Bouton secondaire
    for (i <- keyBindings.indices) {
      val currentRowY = row1Y - (i * ESPACEMENT_ITEM)  // Y calculé pour cette ligne

      // Label de l'action, aligné verticalement au centre des boutons
      keyBindingLabels(i).setPosition(col1X + 20, currentRowY + (KEY_BUTTON_HEIGHT - 20) / 2)

      // Bouton de la touche primaire
      keyBindingButtons1(i).setPosition(col1X + 200, currentRowY)
      keyBindingButtons1(i).setSize(KEY_BUTTON_WIDTH, KEY_BUTTON_HEIGHT)

      // Séparateur "|" aligné au centre des boutons
      separatorLabels(i).setPosition(col1X + 200 + KEY_BUTTON_WIDTH + 5, currentRowY + (KEY_BUTTON_HEIGHT - 20) / 2)

      // Bouton de la touche secondaire
      keyBindingButtons2(i).setPosition(col1X + 200 + KEY_BUTTON_WIDTH + 30, currentRowY)
      keyBindingButtons2(i).setSize(KEY_BUTTON_WIDTH, KEY_BUTTON_HEIGHT)
    }

    // ========================================
    // COLONNE 2: SECTIONS AFFICHAGE + GAMEPLAY
    // ========================================
    var row2Y = screenHeight - ESPACEMENT_TITLE  // Y de départ pour la colonne 2

    // === SECTION AFFICHAGE ===
    displaySectionLabel.setPosition(col2X, row2Y)
    row2Y -= ESPACEMENT_SECTION
    // Note: Espace réservé pour futurs paramètres d'affichage
    row2Y -= ESPACEMENT_SECTION

    // === SECTION GAMEPLAY ===
    gameplaySectionLabel.setPosition(col2X, row2Y)
    row2Y -= ESPACEMENT_SECTION
    // Note: Espace réservé pour futurs paramètres de gameplay

    // ========================================
    // BOUTONS PRINCIPAUX CENTRÉS EN BAS
    // ========================================
    val buttonY = 80           // Y fixe pour tous les boutons (proche du bas)
    val buttonSpacing = 140    // Espacement horizontal entre boutons

    // Disposition horizontale centrée : Reset | Apply | Return
    resetButton.setPosition(screenWidth/2 - buttonSpacing - 60, buttonY)
    resetButton.setSize(120, 45)

    applyButton.setPosition(screenWidth/2 - 60, buttonY)
    applyButton.setSize(120, 45)

    returnButton.setPosition(screenWidth/2 + buttonSpacing - 60, buttonY)
    returnButton.setSize(120, 45)
  }

  /**
   * Connecte les événements aux composants UI interactifs.
   *
   * TYPES D'ÉVÉNEMENTS GÉRÉS :
   * - Boutons ON/OFF audio : Toggle immédiat avec sauvegarde
   * - Boutons de configuration des touches : Démarrage du mode capture
   * - Boutons principaux : Apply, Reset, Return avec logiques spécifiques
   *
   * PATTERN UTILISÉ :
   * - ClickListener anonymes pour chaque bouton
   * - Capture d'index pour les boucles (éviter les closures incorrectes)
   * - Sauvegarde immédiate des paramètres audio
   */
  private def addEventListeners(): Unit = {
    // ========================================
    // BOUTONS ON/OFF AUDIO
    // ========================================

    // === BOUTON MUSIQUE ===
    // Toggle entre ON/OFF avec sauvegarde immédiate
    musicToggleButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        val isOn = musicToggleButton.getText.toString == "ON"
        if (isOn) {
          // Désactiver la musique
          musicToggleButton.setText("OFF")
          musicToggleButton.getLabel.setColor(MYSTIS_DARK_ORANGE)  // Couleur désactivée
          AudioManager.toggleMusic(false)                          // Action système
          GameSettings.musicEnabled = false                        // Sauvegarde immédiate
        } else {
          // Activer la musique
          musicToggleButton.setText("ON")
          musicToggleButton.getLabel.setColor(MYSTIS_GOLD)         // Couleur activée
          AudioManager.toggleMusic(true)                           // Action système
          GameSettings.musicEnabled = true                         // Sauvegarde immédiate
        }
      }
    })

    // === BOUTON EFFETS SONORES ===
    // Logique identique au bouton musique
    sfxToggleButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        val isOn = sfxToggleButton.getText.toString == "ON"
        if (isOn) {
          sfxToggleButton.setText("OFF")
          sfxToggleButton.getLabel.setColor(MYSTIS_DARK_ORANGE)
          AudioManager.toggleSFX(false)
          GameSettings.sfxEnabled = false                           // Sauvegarde immédiate
        } else {
          sfxToggleButton.setText("ON")
          sfxToggleButton.getLabel.setColor(MYSTIS_GOLD)
          AudioManager.toggleSFX(true)
          GameSettings.sfxEnabled = true                            // Sauvegarde immédiate
        }
      }
    })

    // ========================================
    // BOUTONS DE CONFIGURATION DES TOUCHES
    // ========================================

    // Pour chaque action du jeu, connecter les boutons primaire et secondaire
    for (i <- keyBindings.indices) {
      val index = i  // Capture de l'index pour éviter les problèmes de closure

      // === BOUTON TOUCHE PRIMAIRE ===
      keyBindingButtons1(i).addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          startKeyCapture(index, primary = true)  // Démarrer capture pour touche primaire
        }
      })

      // === BOUTON TOUCHE SECONDAIRE ===
      keyBindingButtons2(i).addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          startKeyCapture(index, primary = false)  // Démarrer capture pour touche secondaire
        }
      })
    }

    // ========================================
    // BOUTONS PRINCIPAUX
    // ========================================

    // === BOUTON RETURN ===
    // Navigation intelligente selon la provenance
    returnButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        Logger.log(s"Returning to ${GameSettings.previousState}")
        if (unifiedApp != null) {
          GameSettings.previousState match {
            case "GAME" =>
              // Retourner au jeu avec pause maintenue pour permettre de reprendre
              GameSettings.isGamePaused = true
              unifiedApp.changeState(AppState.GAME)
            case _ =>
              // Retourner au menu principal
              unifiedApp.changeState(AppState.MENU)
          }
        } else {
          Gdx.app.exit()  // Fermer l'application si pas d'app unifiée
        }
      }
    })

    // === BOUTON APPLY ===
    // Applique et confirme tous les paramètres
    applyButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        applySettings()  // Exécuter la logique d'application
      }
    })

    // === BOUTON RESET ===
    // Remet tous les paramètres aux valeurs d'usine
    resetButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        resetToDefaults()  // Exécuter la logique de réinitialisation
      }
    })
  }

  /**
   * Démarre le processus de capture d'une nouvelle touche.
   *
   * FONCTIONNEMENT :
   * 1. Annule toute capture précédente en cours
   * 2. Active le mode capture avec les paramètres appropriés
   * 3. Change l'apparence du bouton pour indiquer l'attente
   * 4. Log de l'opération pour le débogage
   *
   * @param index Index de l'action dans keyBindings (0-5)
   * @param primary true pour touche primaire, false pour secondaire
   */
  private def startKeyCapture(index: Int, primary: Boolean): Unit = {
    // Annuler toute capture précédente pour éviter les conflits
    if (waitingForKey) {
      cancelKeyCapture()
    }

    // === ACTIVATION DU MODE CAPTURE ===
    waitingForKey = true      // Signal global pour le système de capture
    captureIndex = index      // Quelle action est en cours de reconfiguration
    capturePrimary = primary  // Quelle touche (primaire ou secondaire)

    // === FEEDBACK VISUEL ===
    // Modifier l'apparence du bouton pour indiquer l'état d'attente
    val button = if (primary) keyBindingButtons1(index) else keyBindingButtons2(index)
    button.setText("Press...")              // Texte temporaire explicite
    button.getLabel.setColor(Color.YELLOW)  // Couleur distinctive d'attente

    Logger.log(s"Waiting for key for: ${keyBindings(index).action} (${if (primary) "primary" else "secondary"})")
  }

  /**
   * Annule le processus de capture de touche en cours.
   *
   * UTILISATIONS :
   * - Quand l'utilisateur presse Échap pendant la capture
   * - Quand une nouvelle capture démarre (annuler la précédente)
   * - En cas d'erreur ou d'interruption
   *
   * ACTIONS :
   * - Restaure l'apparence originale du bouton
   * - Remet la touche d'origine
   * - Désactive le mode capture
   */
  private def cancelKeyCapture(): Unit = {
    if (waitingForKey && captureIndex >= 0) {
      // === RESTAURATION DE L'APPARENCE ===
      val button = if (capturePrimary) keyBindingButtons1(captureIndex) else keyBindingButtons2(captureIndex)
      val originalKey = if (capturePrimary) keyBindings(captureIndex).primary else keyBindings(captureIndex).secondary
      button.setText(originalKey)           // Remettre le nom de la touche originale
      button.getLabel.setColor(MYSTIS_GOLD) // Remettre la couleur normale
    }

    // === DÉSACTIVATION DU MODE CAPTURE ===
    waitingForKey = false
    captureIndex = -1
  }

  /**
   * Applique et sauvegarde tous les paramètres modifiés.
   *
   * NOTE IMPORTANTE :
   * Dans cette implémentation, la plupart des paramètres sont sauvegardés
   * immédiatement lors des modifications (sauvegarde en temps réel).
   * Cette méthode sert principalement à :
   * - Confirmer les changements
   * - Logger l'état final
   * - Futurs paramètres qui nécessiteraient une application différée
   */
  private def applySettings(): Unit = {
    Logger.log("Applying settings...")

    // Les paramètres audio sont déjà sauvegardés automatiquement lors du clic
    // Les configurations de touches sont sauvegardées lors de la capture
    // Cette méthode confirme simplement l'état final

    Logger.log(s"Musique: ${if (GameSettings.musicEnabled) "activee" else "desactivee"}")
    Logger.log(s"SFX: ${if (GameSettings.sfxEnabled) "actives" else "desactives"}")
    Logger.log("Settings applied and saved!")
  }

  /**
   * Remet tous les paramètres aux valeurs par défaut d'usine.
   *
   * SECTIONS RÉINITIALISÉES :
   * 1. Paramètres audio (musique et SFX à ON)
   * 2. Configuration des touches (WASD + flèches + touches par défaut)
   * 3. Interface mise à jour pour refléter les changements
   *
   * VALEURS PAR DÉFAUT :
   * - Musique : ON
   * - SFX : ON
   * - Mouvement : WASD + flèches directionnelles
   * - Ultimate : Espace + Pavé numérique 0
   * - Menu : Échap + B
   */
  private def resetToDefaults(): Unit = {
    // ========================================
    // RÉINITIALISATION AUDIO
    // ========================================

    // === MUSIQUE ===
    musicToggleButton.setText("ON")
    musicToggleButton.getLabel.setColor(MYSTIS_GOLD)
    AudioManager.toggleMusic(true)
    GameSettings.musicEnabled = true

    // === EFFETS SONORES ===
    sfxToggleButton.setText("ON")
    sfxToggleButton.getLabel.setColor(MYSTIS_GOLD)
    AudioManager.toggleSFX(true)
    GameSettings.sfxEnabled = true

    // ========================================
    // RÉINITIALISATION DES TOUCHES
    // ========================================

    // Restaurer les mappings par défaut dans GameSettings.Controls
    import com.badlogic.gdx.Input
    GameSettings.Controls.moveUp = (Input.Keys.W, Input.Keys.UP)          // W + ↑
    GameSettings.Controls.moveDown = (Input.Keys.S, Input.Keys.DOWN)      // S + ↓
    GameSettings.Controls.moveLeft = (Input.Keys.A, Input.Keys.LEFT)      // A + ←
    GameSettings.Controls.moveRight = (Input.Keys.D, Input.Keys.RIGHT)    // D + →
    GameSettings.Controls.ultimate = (Input.Keys.SPACE, Input.Keys.NUMPAD_0)  // Espace + Pavé 0
    GameSettings.Controls.menu = (Input.Keys.ESCAPE, Input.Keys.B)        // Échap + B

    // ========================================
    // MISE À JOUR DE L'INTERFACE
    // ========================================

    // Synchroniser l'affichage avec les nouvelles valeurs
    for (i <- keyBindings.indices) {
      val binding = keyBindings(i)

      // === DÉTERMINATION DES NOUVELLES TOUCHES ===
      // Selon l'action, récupérer les touches par défaut correspondantes
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
        case "ultimate" =>
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.ultimate._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.ultimate._2)
        case "menu" =>
          binding.primary = GameSettings.Controls.getKeyName(GameSettings.Controls.menu._1)
          binding.secondary = GameSettings.Controls.getKeyName(GameSettings.Controls.menu._2)
        case _ =>
        // Cas par défaut pour les actions non reconnues (extensibilité future)
      }

      // === MISE À JOUR VISUELLE DES BOUTONS ===
      keyBindingButtons1(i).setText(binding.primary)
      keyBindingButtons2(i).setText(binding.secondary)
      keyBindingButtons1(i).getLabel.setColor(MYSTIS_GOLD)
      keyBindingButtons2(i).getLabel.setColor(MYSTIS_GOLD)
    }

    Logger.log("Settings reset to defaults")
  }

  /**
   * Ajoute tous les composants UI créés à la scène LibGDX.
   *
   * ORDRE D'AJOUT :
   * 1. Titre principal
   * 2. Section Audio (titre + composants)
   * 3. Sections Affichage et Gameplay (préparées)
   * 4. Section Contrôles (titre + composants dynamiques)
   * 5. Boutons principaux
   *
   * NOTE : Cette méthode doit être appelée APRÈS initializeComponents() et setupLayout()
   * car elle référence tous les composants créés et positionnés.
   */
  private def addComponentsToStage(): Unit = {
    // === TITRE ===
    stage.addActor(optionsTitleLabel)

    // === SECTION AUDIO ===
    stage.addActor(audioSectionLabel)
    stage.addActor(audioInfoLabel)
    stage.addActor(musicLabel)
    stage.addActor(musicToggleButton)
    stage.addActor(sfxLabel)
    stage.addActor(sfxToggleButton)

    // === SECTION AFFICHAGE ===
    stage.addActor(displaySectionLabel)
    // Note: Composants futurs à ajouter ici

    // === SECTION GAMEPLAY ===
    stage.addActor(gameplaySectionLabel)
    // Note: Composants langues et sous-titres supprimés pour simplification

    // === SECTION CONTRÔLES ===
    stage.addActor(controlsSectionLabel)
    stage.addActor(bindingsSubLabel)

    // === AJOUT DYNAMIQUE DES CONTRÔLES DE TOUCHES ===
    // Pour chaque action configurée, ajouter tous ses composants
    for (i <- keyBindings.indices) {
      stage.addActor(keyBindingLabels(i))     // Label de l'action
      stage.addActor(keyBindingButtons1(i))   // Bouton touche primaire
      stage.addActor(separatorLabels(i))      // Séparateur "|"
      stage.addActor(keyBindingButtons2(i))   // Bouton touche secondaire
    }

    // === BOUTONS PRINCIPAUX ===
    stage.addActor(returnButton)
    stage.addActor(applyButton)
    stage.addActor(resetButton)
  }

  /**
   * Méthode de rendu appelée à chaque frame (60 FPS).
   *
   * RESPONSABILITÉS :
   * 1. Effacer l'écran avec la couleur de fond appropriée
   * 2. Configurer le blending pour la transparence
   * 3. Gérer l'affichage/masquage des instructions de capture
   * 4. Traiter et dessiner tous les éléments UI
   *
   * GESTION DES INSTRUCTIONS DE CAPTURE :
   * - Apparaissent au centre quand waitingForKey = true
   * - Disparaissent automatiquement quand la capture se termine
   * - Positionnement dynamique centré
   *
   * @param g Contexte graphique GDX2D pour le rendu
   */
  def onGraphicRender(g: GdxGraphics): Unit = {
    // ========================================
    // PRÉPARATION DU RENDU
    // ========================================
    import com.badlogic.gdx.Gdx
    import com.badlogic.gdx.graphics.GL20

    // Effacer l'écran avec la couleur de fond Mystis
    Gdx.gl.glClearColor(MYSTIS_BROWN.r, MYSTIS_BROWN.g, MYSTIS_BROWN.b, 1.0f) // Brun très sombre
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // Activer le blending pour supporter les couleurs semi-transparentes
    Gdx.gl.glEnable(GL20.GL_BLEND)
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

    // ========================================
    // GESTION DES INSTRUCTIONS DE CAPTURE
    // ========================================

    if (waitingForKey) {
      // === CALCUL DE LA POSITION CENTRALE ===
      val screenWidth = 1920
      val screenHeight = 1080
      val boxWidth = 400     // Largeur de la zone d'instructions
      val boxHeight = 200    // Hauteur de la zone d'instructions
      val boxX = screenWidth / 2   // Centre horizontal
      val boxY = screenHeight / 2  // Centre vertical

      // === AFFICHAGE DES INSTRUCTIONS ===
      // Ajouter les labels seulement s'ils ne sont pas déjà affichés
      if (!captureInstructionVisible) {
        // Positionner les deux lignes d'instruction au centre
        captureInstructionLabel1.setPosition(boxX - boxWidth/2, boxY/3*2 + 30)  // "Press a key..."
        captureInstructionLabel2.setPosition(boxX - boxWidth/2, boxY/3*2 - 30)  // "ESC to cancel"

        // Ajouter à la scène pour affichage
        stage.addActor(captureInstructionLabel1)
        stage.addActor(captureInstructionLabel2)
        captureInstructionVisible = true
      }
    } else {
      // === MASQUAGE DES INSTRUCTIONS ===
      // Retirer les instructions si elles sont affichées mais que la capture est terminée
      if (captureInstructionVisible) {
        captureInstructionLabel1.remove()
        captureInstructionLabel2.remove()
        captureInstructionVisible = false
      }
    }

    // ========================================
    // RENDU DE L'INTERFACE
    // ========================================
    stage.act()   // Traitement de la logique UI (animations, interactions, timers)
    stage.draw()  // Rendu visuel de tous les éléments ajoutés à la scène
  }

  /**
   * Gère les entrées clavier globales (hors mode capture).
   *
   * FONCTIONNALITÉS :
   * - Touche Échap : retour au menu (seulement si pas en capture)
   * - Extensible pour d'autres raccourcis globaux
   *
   * PROTECTION : Ne traite les entrées que si waitingForKey = false
   * pour éviter les conflits avec le système de capture.
   *
   * @param keycode Code LibGDX de la touche pressée
   */
  def onKeyDown(keycode: Int): Unit = {
    // Traiter seulement si on n'est pas en mode capture de touches
    if ((GameSettings.Controls.isKeyPressed("menu", keycode) || keycode == Input.Keys.ESCAPE) && !waitingForKey) {
      if (unifiedApp != null) {
        // Retourner vers l'état précédent (GAME ou MENU)
        if (GameSettings.previousState == "GAME") {
          unifiedApp.changeState(AppState.GAME)
        } else {
          unifiedApp.changeState(AppState.MENU)
        }
      } else {
        Gdx.app.exit()  // Fermer l'application si pas d'app unifiée
      }
    }
  }

  /**
   * Retour au menu précédent (GAME ou MENU selon previousState).
   */
  private def goBack(): Unit = {
    if (unifiedApp != null) {
      // Retourner vers l'état précédent (GAME ou MENU)
      if (GameSettings.previousState == "GAME") {
        unifiedApp.changeState(AppState.GAME)
      } else {
        unifiedApp.changeState(AppState.MENU)
      }
    } else {
      Logger.log("Fermeture de l'application...")
      Gdx.app.exit()
    }
  }

  /**
   * Configure la gestion des entrées clavier.
   * Permet de naviguer avec la touche Échap.
   */
  private def setupInput(): Unit = {
    // Créer un processeur d'entrées personnalisé
    val inputProcessor = new InputProcessor {
      override def keyDown(keycode: Int): Boolean = {
        if (!waitingForKey) { // Seulement si pas en mode capture
          keycode match {
            case Input.Keys.ESCAPE =>
              goBack()  // Retour au menu avec Échap
              true      // Indiquer que l'entrée a été traitée
            case _ => false  // Autres touches non traitées
          }
        } else {
          false // En mode capture, laisser passer
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

    // Ajouter le processeur d'entrées au multiplexeur
    inputMultiplexer.addProcessor(inputProcessor)
  }

  /**
   * Nettoie les ressources quand le menu des options n'est plus utilisé.
   *
   * NETTOYAGE EFFECTUÉ :
   * 1. Réinitialisation du gestionnaire d'entrées (évite les conflits)
   * 2. Libération de la scène LibGDX et tous ses composants
   * 3. Libération des ressources du thème UI
   *
   * IMPORTANCE : Évite les fuites mémoire et les conflits d'entrées
   * lors du changement d'état de l'application.
   */
  def dispose(): Unit = {
    // Réinitialiser le gestionnaire d'entrées pour éviter les conflits
    Gdx.input.setInputProcessor(null)

    // Libérer les ressources LibGDX
    if (stage != null) stage.dispose()  // Libère la scène et tous ses composants
    if (skin != null) skin.dispose()    // Libère les textures et polices du thème
  }
}

/**
 * Lanceur autonome pour tester le menu des Options indépendamment.
 *
 * UTILISATION :
 * - Développement : tester le menu sans lancer tout le jeu
 * - Débogage : isoler les problèmes du menu des options
 * - Démonstration : montrer le menu à des tiers
 *
 * FONCTIONNEMENT :
 * Crée une instance du menu sans application unifiée (unifiedApp = null)
 */
object MystisOptionsMenuLauncher extends PortableApplication(1920, 1080) with App {
  var optionsMenu: MystisOptionsMenu = _

  override def onInit(): Unit = {
    optionsMenu = new MystisOptionsMenu()
    optionsMenu.onInit()
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    optionsMenu.onGraphicRender(g)
  }

  override def onKeyDown(keycode: Int): Unit = {
    optionsMenu.onKeyDown(keycode)
  }

  def dispose(): Unit = {
    if (optionsMenu != null) optionsMenu.dispose()
  }
}