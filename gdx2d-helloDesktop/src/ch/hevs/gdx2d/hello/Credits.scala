package ch.hevs.gdx2d.hello

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

/**
 * Écran des crédits de Mystis.
 * Affiche les informations sur les développeurs, la technologie utilisée,
 * et les remerciements. Permet de revenir au menu principal.
 *
 * @param unifiedApp Référence vers l'application principale pour la navigation
 */
class MystisCreditsMenu(unifiedApp: MystisUnifiedApp = null) {

  // === COMPOSANTS UI ===
  var stage: Stage = _                    // Scène pour les éléments d'interface
  var skin: Skin = _                      // Style/apparence des éléments UI
  var inputMultiplexer: InputMultiplexer = _ // Gestionnaire des entrées clavier/souris

  // === CONSTANTES DE MISE EN PAGE ===

  // Tailles de police pour différents types de texte
  val TITLE_FONT_SIZE = 5f        // Titre principal "CREDITS"
  val SECTION_FONT_SIZE = 2.5f    // Titres de sections (ex: "TECHNOLOGY")
  val NORMAL_FONT_SIZE = 1.8f     // Texte normal
  val BUTTON_FONT_SIZE = 1.5f     // Texte des boutons

  // Espacements entre les éléments (en pixels)
  val ESPACEMENT_TITLE = 20       // Espace après le titre principal
  val ESPACEMENT_SECTION = 20     // Espace après les titres de sections
  val ESPACEMENT_NORMAL = 10      // Espace normal entre les éléments

  // Marges de la page
  val PAGE_MARGIN = 20

  /**
   * Initialise l'écran des crédits.
   * Configure l'interface utilisateur et les contrôles.
   */
  def onInit(): Unit = {
    Logger.log("Initializing Credits Menu...")

    // Créer la scène pour l'interface utilisateur
    stage = new Stage()

    // Charger le style/thème des éléments UI
    skin = new Skin(Gdx.files.internal("gdx2d-helloDesktop/data/ui/uiskin.json"))

    // Démarrer la musique du menu
    AudioManager.startMenuMusic()

    // Construire l'interface et configurer les contrôles
    createUI()
    setupInput()

    Logger.log("Credits Menu initialized successfully")
  }

  /**
   * Crée et organise tous les éléments visuels de l'écran des crédits.
   * Utilise un système de tableau (Table) pour organiser les éléments verticalement.
   */
  private def createUI(): Unit = {
    // Table principale qui contient tous les éléments
    val table = new Table()
    table.setFillParent(true)        // Remplit tout l'écran
    table.center().top()             // Centré horizontalement, aligné en haut
    table.padTop(20)                 // Marge du haut

    // === TITRE PRINCIPAL ===
    val titleLabel = new Label("CREDITS", skin)
    titleLabel.setFontScale(TITLE_FONT_SIZE)     // Très grande taille
    titleLabel.setColor(MystisColors.GOLD)       // Couleur dorée
    table.add(titleLabel).padBottom(ESPACEMENT_TITLE).row()  // Ajouter avec espacement

    // === SECTION DÉVELOPPEMENT ===
    val gameDevLabel = new Label("GAME DEVELOPMENT", skin)
    gameDevLabel.setFontScale(SECTION_FONT_SIZE)
    gameDevLabel.setColor(MystisColors.ORANGE)   // Couleur orange pour les titres de section
    table.add(gameDevLabel).padBottom(ESPACEMENT_SECTION).row()

    // Texte principal des crédits avec toutes les informations importantes
    val creditText = new Label("Game developed with GDX2D\n" +
      "by Dino Bijelic and Vadym Chobu\n\n" +
      "for the 103.2 - Object Oriented Programming (POO)\n" +
      "at HES-SO Valais-Wallis\n" +
      "Computer Science and Communication System\n\n" +
      "JUNE 2025", skin)
    creditText.setFontScale(NORMAL_FONT_SIZE)
    creditText.setColor(Color.WHITE)             // Texte blanc
    creditText.setAlignment(com.badlogic.gdx.utils.Align.center)  // Centré
    table.add(creditText).padBottom(ESPACEMENT_NORMAL * 2).row()

    // === SECTION TECHNOLOGIE ===
    val techLabel = new Label("TECHNOLOGY", skin)
    techLabel.setFontScale(SECTION_FONT_SIZE)
    techLabel.setColor(MystisColors.ORANGE)
    table.add(techLabel).padBottom(ESPACEMENT_SECTION).row()

    // Informations sur les technologies utilisées
    val techText = new Label("Built with LibGDX Framework & GDX2D Library\n" +
      "Programmed in Scala", skin)
    techText.setFontScale(NORMAL_FONT_SIZE)
    techText.setColor(Color.WHITE)
    techText.setAlignment(com.badlogic.gdx.utils.Align.center)
    table.add(techText).padBottom(ESPACEMENT_NORMAL * 2).row()

    // === SECTION REMERCIEMENTS ===
    val thanksLabel = new Label("SPECIAL THANKS", skin)
    thanksLabel.setFontScale(SECTION_FONT_SIZE)
    thanksLabel.setColor(MystisColors.ORANGE)
    table.add(thanksLabel).padBottom(ESPACEMENT_SECTION).row()

    // Remerciements spéciaux
    val thanksText = new Label("Professor Pierre-André Mudry\n" +
      "HES-SO Valais-Wallis Computer Science and Communication System", skin)
    thanksText.setFontScale(NORMAL_FONT_SIZE)
    thanksText.setColor(Color.WHITE)
    thanksText.setAlignment(com.badlogic.gdx.utils.Align.center)
    table.add(thanksText).padBottom(ESPACEMENT_NORMAL * 3).row()

    // === BOUTON RETOUR ===
    val backButton = new TextButton("Back", skin)
    backButton.getLabel.setFontScale(BUTTON_FONT_SIZE)
    backButton.getLabel.setColor(MystisColors.GOLD)

    // Définir l'action du bouton : retourner au menu principal
    backButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        goBack()  // Appeler la méthode de retour
      }
    })
    table.add(backButton).width(200).height(50).padTop(50)  // Taille et position du bouton

    // Ajouter la table complète à la scène
    stage.addActor(table)
  }

  /**
   * Configure la gestion des entrées clavier et souris.
   * Permet de naviguer avec les touches Échap et B.
   */
  private def setupInput(): Unit = {
    // Créer un processeur d'entrées personnalisé
    val inputProcessor = new InputProcessor {
      override def keyDown(keycode: Int): Boolean = {
        keycode match {
          case Input.Keys.ESCAPE | Input.Keys.B =>
            goBack()  // Retour au menu avec Échap ou B
            true      // Indiquer que l'entrée a été traitée
          case _ => false  // Autres touches non traitées
        }
      }

      // Méthodes obligatoires mais non utilisées
      override def keyUp(keycode: Int): Boolean = false
      override def keyTyped(character: Char): Boolean = false
      override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
      override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
      override def mouseMoved(screenX: Int, screenY: Int): Boolean = false
      override def scrolled(amount: Int): Boolean = false
    }

    // Multiplexeur pour gérer plusieurs sources d'entrées
    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(stage)          // Entrées UI (boutons, etc.)
    inputMultiplexer.addProcessor(inputProcessor) // Entrées clavier personnalisées

    // Activer le gestionnaire d'entrées
    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  /**
   * Retourne au menu principal.
   * Utilise l'application unifiée pour changer d'état.
   */
  private def goBack(): Unit = {
    if (unifiedApp != null) {
      unifiedApp.changeState(AppState.MENU)  // Changer vers l'état MENU
    } else {
      // ✅ AJOUTER cette ligne pour fermer quand pas d'app unifiée !
      Logger.log("Fermeture de l'application...")
      Gdx.app.exit()
    }
  }

  /**
   * Méthode de rendu appelée à chaque frame.
   * Dessine l'interface utilisateur et les éléments décoratifs.
   *
   * @param g Contexte graphique pour le rendu
   */
  def onGraphicRender(g: GdxGraphics): Unit = {
    // Effacer l'écran avec la couleur sombre de Mystis
    g.clear(MystisColors.DARK)

    // Mettre à jour et dessiner tous les éléments UI
    stage.act()    // Traitement de la logique UI (animations, etc.)
    stage.draw()   // Rendu visuel de tous les éléments

    // Dessiner le logo de l'école (élément décoratif)
    g.drawSchoolLogo()

  }

  /**
   * Nettoie les ressources quand l'écran n'est plus utilisé.
   * Important pour éviter les fuites mémoire.
   */
  def dispose(): Unit = {
    if (stage != null) stage.dispose()  // Libérer la scène UI
    if (skin != null) skin.dispose()    // Libérer les ressources du thème
    Gdx.input.setInputProcessor(null)   // Désactiver la gestion des entrées
  }
}

/**
 * Lanceur autonome pour tester le menu des Crédits indépendamment.
 *
 * UTILISATION :
 * - Développement : tester le menu sans lancer tout le jeu
 * - Débogage : isoler les problèmes du menu des options
 * - Démonstration : montrer le menu à des tiers
 *
 * FONCTIONNEMENT :
 * Crée une instance du menu sans application unifiée (unifiedApp = null)
 */
object MystisCreditsMenuLauncher extends PortableApplication(1920, 1080) with App {
  var creditsMenu: MystisCreditsMenu = _

  override def onInit(): Unit = {
    creditsMenu = new MystisCreditsMenu()
    creditsMenu.onInit()
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    creditsMenu.onGraphicRender(g)
  }

  def dispose(): Unit = {
    if (creditsMenu != null) creditsMenu.dispose()
  }
}
