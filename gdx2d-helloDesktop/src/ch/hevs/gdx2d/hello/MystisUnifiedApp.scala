// ==== FICHIER PRINCIPAL COMPLET : MystisUnifiedApp.scala ====
/**
 * Point d'entrée principal du jeu Mystis - Application LibGDX utilisant le framework GDX2D.
 * 
 * Ce fichier contient:
 * - La classe principale MystisUnifiedApp qui gère le cycle de vie de l'application
 * - Les énumérations d'états (MENU, GAME, OPTIONS, CREDITS)
 * - Les couleurs personnalisées du thème Mystis
 * - Le système de navigation entre écrans
 * - La gestion centralisée de l'audio et des ressources
 * 
 * Développé pour le cours 103.2 - Programmation Orientée Objet (POO)
 * HES-SO Valais-Wallis, Informatique et Système de Communication
 * Par: Dino Bijelic et Vadym Chobu, JUIN 2025
 */
package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import ch.hevs.gdx2d.lib.utils.Logger

// ==== ÉNUMÉRATION DES ÉTATS DE L'APPLICATION ====
/**
 * États possibles de l'application pour la navigation entre écrans.
 * Chaque état correspond à un écran différent du jeu.
 */
object AppState extends Enumeration {
  type AppState = Value
  val MENU,      // Menu principal avec Start/Options/Credits/Quit
      GAME,      // Jeu principal avec joueur, ennemis, projectiles
      OPTIONS,   // Écran de configuration (audio, contrôles)
      CREDITS = Value // Écran des crédits et informations développeurs
}


// ==== COULEURS MYSTIS ====
object MystisColors {
  val GOLD = new Color(1.0f, 0.84f, 0.0f, 1.0f)
  val ORANGE = new Color(1.0f, 0.65f, 0.0f, 1.0f)
  val DARK_ORANGE = new Color(0.8f, 0.4f, 0.0f, 1.0f)
  val BLUE = new Color(0.2f, 0.5f, 0.9f, 1.0f)
  val DARK = new Color(0.1f, 0.1f, 0.2f, 0.8f)
  val RED = new Color(1.0f, 0.2f, 0.2f, 1.0f)
  val GREEN = new Color(0.2f, 1.0f, 0.2f, 1.0f)
  val MYSTIS_BROWN = new Color(0.15f, 0.1f, 0.05f, 1.0f)
}





// ==== APPLICATION UNIFIÉE PRINCIPALE ====
class MystisUnifiedApp extends PortableApplication(1920, 1080) {

  private var currentState: AppState.AppState = AppState.MENU
  private var nextState: Option[AppState.AppState] = None
  private var stateChanged: Boolean = false

  // Instances des différents écrans
  private var menu: MystisMainMenu = _
  private var game: MystisGameMenu = _
  private var options: MystisOptionsMenu = _
  private var credits: MystisCreditsMenu = _

  override def onInit(): Unit = {
    setTitle("Mystis Game")
    Logger.log("Lancement de Mystis Game...")
    
    // Initialiser AudioManager (version stub qui ne fait rien)
    AudioManager.initialize()
    
    initCurrentState()
  }

  private def initCurrentState(): Unit = {
    Logger.log(s"Initialisation de l'état: $currentState")

    currentState match {
      case AppState.MENU =>
        menu = new MystisMainMenu(this)
        menu.onInit()
        // Démarrer la musique du menu (seulement si pas déjà en cours)
        AudioManager.startMenuMusic()

      case AppState.GAME =>
        // Ne créer une nouvelle instance que si elle n'existe pas déjà
        if (game == null) {
          game = new MystisGameMenu(this)
          game.onInit()
          // La musique de gameplay est démarrée dans Game.onInit()
        }
        // Si le jeu existe déjà, on reprend simplement où on en était

      case AppState.OPTIONS =>
        options = new MystisOptionsMenu(this)
        options.onInit()
        // Les options ne changent pas la musique - elle continue selon l'état précédent
        
      case AppState.CREDITS =>
        credits = new MystisCreditsMenu(this)
        credits.onInit()
        // Les crédits ne changent pas la musique - elle continue selon l'état précédent
    }
    stateChanged = false
  }

  def changeState(newState: AppState.AppState): Unit = {
    Logger.log(s"Changement d'état: $currentState -> $newState")
    nextState = Some(newState)
    stateChanged = true
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    // Vérifier changement d'état
    if (stateChanged && nextState.isDefined) {
      // Nettoyer l'état précédent (sauf le jeu en cours pour préserver l'état)
      currentState match {
        case AppState.MENU if menu != null =>
          menu.dispose()
          menu = null
        case AppState.GAME if game != null =>
          // NE PAS dispose le jeu - le garder en mémoire pour reprendre
          // game.dispose()
          // game = null
        case AppState.OPTIONS if options != null =>
          options.dispose()
          options = null
        case AppState.CREDITS if credits != null =>
          credits.dispose()
          credits = null
      }

      currentState = nextState.get
      nextState = None
      initCurrentState()
    }

    // Rendre l'état actuel
    currentState match {
      case AppState.MENU if menu != null =>
        menu.onGraphicRender(g)
      case AppState.GAME if game != null =>
        game.onGraphicRender(g)
      case AppState.OPTIONS if options != null =>
        options.onGraphicRender(g)
      case AppState.CREDITS if credits != null =>
        credits.onGraphicRender(g)
    }
  }

  override def onKeyDown(keycode: Int): Unit = {
    currentState match {
      case AppState.MENU if menu != null =>
        menu.onKeyDown(keycode)
      case AppState.GAME if game != null =>
        game.onKeyDown(keycode)
      case AppState.OPTIONS if options != null =>
        options.onKeyDown(keycode)
      case AppState.CREDITS if credits != null =>
        // Credits menu handles input internally
    }
  }

  override def onDispose(): Unit = {
    Logger.log("Fermeture de l'application")

    // Nettoyer AudioManager (version stub)
    AudioManager.dispose()

    if (menu != null) {
      menu.dispose()
      menu = null
    }
    if (game != null) {
      game.dispose()
      game = null
    }
    if (options != null) {
      options.dispose()
      options = null
    }
    if (credits != null) {
      credits.dispose()
      credits = null
    }

    super.onDispose()
  }
}




// ==== POINT D'ENTRÉE PRINCIPAL ====
object MystisGameLauncher extends App {
  new MystisUnifiedApp()
}