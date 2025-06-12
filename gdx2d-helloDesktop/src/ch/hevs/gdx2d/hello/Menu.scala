package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color

object MenuState extends Enumeration {
  type MenuState = Value
  val START, OPTIONS, CREDITS, EXIT = Value
}

class MystisMainMenu(unifiedApp: MystisUnifiedApp) {

  // Variables du menu
  private var currentSelection = MenuState.START
  private var backgroundImage: BitmapImage = _
  private val menuOptions = List(MenuState.START, MenuState.OPTIONS, MenuState.CREDITS, MenuState.EXIT)
  private var selectedIndex = 0
  private var arrowMenuSelector: BitmapImage = _

  // Couleurs pour le menu
  private val normalColor = Color.YELLOW
  private val selectedColor = Color.WHITE
  private val highlightColor = Color.ORANGE

  def onInit(): Unit = {
    // Démarrer la musique du menu
    AudioManager.startMenuMusic()
    
    // Créer un InputProcessor simple pour le menu
    val menuInputProcessor = new com.badlogic.gdx.InputProcessor {
      override def keyDown(keycode: Int): Boolean = {
        onKeyDown(keycode)
        true
      }
      override def keyUp(keycode: Int): Boolean = false
      override def keyTyped(character: Char): Boolean = false
      override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
        // Inverser Y car LibGDX utilise un système inversé pour la souris
        val invertedY = Gdx.graphics.getHeight - screenY
        handleMouseClick(screenX, invertedY)
        true
      }
      override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
      override def mouseMoved(screenX: Int, screenY: Int): Boolean = false
      override def scrolled(amount: Int): Boolean = false
    }
    
    Gdx.input.setInputProcessor(menuInputProcessor)
    
    // Charger l'image de fond
    try {
      backgroundImage = new BitmapImage("gdx2d-helloDesktop/data/images/mystis_menu_background-v6-1920x1080.jpg")
      arrowMenuSelector = new BitmapImage("gdx2d-helloDesktop/data/images/arrow_menu-selector.png")
    } catch {
      case e: Exception =>
        println(s"Erreur chargement image: ${e.getMessage}")
    }
  }

  def onGraphicRender(g: GdxGraphics): Unit = {
    // Affichage direct du menu (plus de switch sur currentAppState)
    if (backgroundImage != null) {
      g.drawPicture(g.getScreenWidth / 2, g.getScreenHeight / 2, backgroundImage)
    }
    drawMenuOptions(g)
  }

  private def drawMenuOptions(g: GdxGraphics): Unit = {
    // Positions exactes selon l'image MYSTIS
    val startY = 565 // Position de "Start"
    val optionsY = 465 // Position de "Options"
    val CreditsY = 370 // Position de "Crédits"
    val exitY = 280 // Position de "Exit"
    val menuX = 570 // Position X des options

    val optionPositions = List(
      (menuX, startY), // Start
      (menuX, optionsY), // Load
      (menuX, CreditsY), // Options
      (menuX, exitY) // Exit
    )

    // Flèche mystique à côté de l'option sélectionnée
    if (selectedIndex < 4 && arrowMenuSelector != null) {
      val (x, y) = optionPositions(selectedIndex)
      // Position de la flèche (à gauche de l'option)
      val arrowX = x - 200 // Décalée à gauche des textes
      val arrowY = y

      g.drawTransformedPicture(arrowX, arrowY, 0, 64, 64, arrowMenuSelector)
    }
  }

  def onKeyDown(keycode: Int): Unit = {
    // Navigation directe du menu utilisant le système de contrôles unifié
    if (GameSettings.Controls.isKeyPressed("moveUp", keycode)) {
      selectedIndex = (selectedIndex - 1 + 4) % 4
      updateCurrentSelection()
    } else if (GameSettings.Controls.isKeyPressed("moveDown", keycode)) {
      selectedIndex = (selectedIndex + 1 + 4) % 4
      updateCurrentSelection()
    } else if (GameSettings.Controls.isKeyPressed("jump", keycode) || keycode == Keys.ENTER) {
      executeMenuAction()
    } else if (GameSettings.Controls.isKeyPressed("menu", keycode)) {
      System.exit(0)
    }
  }

  private def handleMouseClick(x: Int, y: Int): Unit = {
    // Coordonnées IDENTIQUES à drawMenuOptions
    val startY = 565 // Position de "Start"
    val optionsY = 465 // Position de "Options"
    val CreditsY = 370 // Position de "Crédits"
    val exitY = 280 // Position de "Exit"
    val menuX = 570 // Position X des options
    val optionWidth = 250
    val optionHeight = 80

    val optionPositions = List(
      (menuX, startY), // Start
      (menuX, optionsY), // Options
      (menuX, CreditsY), // Credits
      (menuX, exitY) // Exit
    )

    // Vérifier les clics sur toutes les options
    optionPositions.zipWithIndex.foreach { case ((optX, optY), index) =>
      if (x >= optX - optionWidth / 2 && x <= optX + optionWidth / 2 &&
        y >= optY - optionHeight / 2 && y <= optY + optionHeight / 2) {
        selectedIndex = index
        updateCurrentSelection()
        executeMenuAction()
        return
      }
    }
  }

  private def updateCurrentSelection(): Unit = {
    currentSelection = selectedIndex match {
      case 0 => MenuState.START
      case 1 => MenuState.OPTIONS
      case 2 => MenuState.CREDITS
      case 3 => MenuState.EXIT
    }
  }

  private def executeMenuAction(): Unit = {
    // Jouer le son de sélection
    AudioManager.playMenuSelectSound()
    
    currentSelection match {
      case MenuState.START =>
        startGame()
      case MenuState.OPTIONS =>
        openOptionsMenu()
      case MenuState.CREDITS =>
        openCreditsMenu()
      case MenuState.EXIT =>
        System.exit(0)
    }
  }

  private def startGame(): Unit = {
    println("Démarrage du jeu...")
    unifiedApp.changeState(AppState.GAME) // Utilise AppState du fichier principal
  }

  private def openOptionsMenu(): Unit = {
    println("Ouverture du menu Options...")
    GameSettings.previousState = "MENU"
    unifiedApp.changeState(AppState.OPTIONS) // Utilise AppState du fichier principal
  }

  private def openCreditsMenu(): Unit = {
    println("Ouverture du menu Credits...")
    // TODO: implémenter le menu de chargement
  }

  def dispose(): Unit = {
    // Nettoyer l'InputProcessor
    Gdx.input.setInputProcessor(null)
    
    if (backgroundImage != null) backgroundImage.dispose()
    if (arrowMenuSelector != null) arrowMenuSelector.dispose()
  }
}