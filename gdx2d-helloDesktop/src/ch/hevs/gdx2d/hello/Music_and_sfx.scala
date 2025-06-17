package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.audio.MusicPlayer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

/**
 * Gestionnaire audio simplifié pour Mystis
 * Gère la musique de fond avec contrôles ON/OFF
 */
object AudioManager {
  
  // Musique de fond
  private var menuMusic: MusicPlayer = _
  private var gameplayMusic: MusicPlayer = _
  private var currentMusic: MusicPlayer = _
  private var currentMusicType: String = "none" // "menu", "gameplay", "none"
  private var lastMusicType: String = "menu" // Mémoriser le dernier type pour le toggle
  private var isMusicInitialized = false
  
  // Sons d'effets
  private var shootSound: Sound = _
  private var killSound: Sound = _
  private var ultimateSound: Sound = _
  private var areSFXInitialized = false
  
  // Contrôles ON/OFF simples - initialisés depuis GameSettings
  private var musicEnabled: Boolean = true
  private var sfxEnabled: Boolean = true
  
  /**
   * Initialise le système audio
   */
  def initialize(): Unit = {
    try {
      // Synchroniser avec GameSettings
      musicEnabled = GameSettings.musicEnabled
      sfxEnabled = GameSettings.sfxEnabled
      
      // Charger les musiques Ori and the Will of the Wisps
      menuMusic = new MusicPlayer("gdx2d-helloDesktop/data/Soundtrack/OST/Ori and the Will of the Wisps - Soundtrack - Main Theme_and_A Yearning for the Sky.wav")
      gameplayMusic = new MusicPlayer("gdx2d-helloDesktop/data/Soundtrack/OST/Ori and the Will of the Wisps - Soundtrack - Escaping a Foul Presence_and_Shriek and Ori.wav")
      isMusicInitialized = true
      
      // Charger les sons d'effets
      shootSound = Gdx.audio.newSound(Gdx.files.internal("gdx2d-helloDesktop/data/Soundtrack/SFX/[SHOOT]-uppbeat-magic-spell-sparkle-blast-epic-stock-media.wav"))
      killSound = Gdx.audio.newSound(Gdx.files.internal("gdx2d-helloDesktop/data/Soundtrack/SFX/[KILL]-uppbeat-magic-spell-light-magic-epic-stock-media.wav"))
      ultimateSound = Gdx.audio.newSound(Gdx.files.internal("gdx2d-helloDesktop/data/Soundtrack/SFX/[ULT]-uppbeat-magic-impact-bosnow-1-00-01.wav"))
      areSFXInitialized = true
      
      println(s"🎵 AudioManager initialisé - Musique: ${if (musicEnabled) "ON" else "OFF"}, SFX: ${if (sfxEnabled) "ON" else "OFF"}")
      
    } catch {
      case e: Exception =>
        println(s"Erreur lors du chargement audio: ${e.getMessage}")
        isMusicInitialized = false
        areSFXInitialized = false
    }
  }
  
  /**
   * Démarre la musique du menu - seulement si activée
   */
  def startMenuMusic(): Unit = {
    // Vérifier si la musique est activée
    if (!musicEnabled) {
      println("🔇 Musique désactivée - pas de démarrage")
      currentMusicType = "menu" // Mémoriser le type pour plus tard
      return
    }
    
    // Ne redémarrer que si la musique du menu n'est pas déjà en cours
    if (currentMusicType != "menu") {
      stopCurrentMusic() // Arrêter toute autre musique
      
      if (isMusicInitialized && menuMusic != null) {
        currentMusic = menuMusic
        currentMusicType = "menu"
        lastMusicType = "menu" // Mémoriser
        currentMusic.setVolume(0.7f) // Volume fixe raisonnable
        currentMusic.loop()
        println("🎵 Musique du menu démarrée - Volume fixe 70%")
      } else {
        println("🎵 *Musique du menu* (fichier non trouvé)")
      }
    } else {
      println("🎵 Musique du menu déjà en cours, continuité préservée")
    }
  }
  
  /**
   * Démarre la musique de gameplay - seulement si activée
   */
  def startGameplayMusic(): Unit = {
    // Vérifier si la musique est activée
    if (!musicEnabled) {
      println("🔇 Musique désactivée - pas de démarrage")
      currentMusicType = "gameplay" // Mémoriser le type pour plus tard
      return
    }
    
    // Ne redémarrer que si la musique de gameplay n'est pas déjà en cours
    if (currentMusicType != "gameplay") {
      stopCurrentMusic() // Arrêter toute autre musique
      
      if (isMusicInitialized && gameplayMusic != null) {
        currentMusic = gameplayMusic
        currentMusicType = "gameplay"
        lastMusicType = "gameplay" // Mémoriser
        currentMusic.setVolume(0.7f) // Volume fixe raisonnable
        currentMusic.loop()
        println("🎵 Musique de gameplay démarrée - Volume fixe 70%")
      } else {
        println("🎵 *Musique de gameplay* (fichier non trouvé)")
      }
    } else {
      println("🎵 Musique de gameplay déjà en cours, continuité préservée")
    }
  }
  
  /**
   * Arrête la musique en cours
   */
  def stopCurrentMusic(): Unit = {
    if (currentMusic != null && currentMusic.isPlaying) {
      currentMusic.stop()
      println("🎵 Musique arrêtée")
    }
    currentMusic = null
    // NE PAS réinitialiser lastMusicType pour le toggle
    currentMusicType = "none"
  }
  
  /**
   * Active ou désactive la musique
   */
  def toggleMusic(enabled: Boolean): Unit = {
    musicEnabled = enabled
    if (!enabled) {
      // Mute: arrêter la musique
      stopCurrentMusic()
      println("🔇 Musique désactivée (MUTE)")
    } else {
      // Unmute: redémarrer la musique selon le dernier type connu
      println(s"🔊 Tentative de réactivation - Dernier type: $lastMusicType")
      if (lastMusicType == "menu") {
        println("🔄 Redémarrage musique menu...")
        startMenuMusic()
      } else if (lastMusicType == "gameplay") {
        println("🔄 Redémarrage musique gameplay...")
        startGameplayMusic()
      } else {
        // Par défaut, démarrer la musique du menu
        println("🔄 Type inconnu, démarrage musique menu par défaut...")
        startMenuMusic()
      }
      println("🔊 Musique réactivée")
    }
  }
  
  /**
   * Active ou désactive les effets sonores
   */
  def toggleSFX(enabled: Boolean): Unit = {
    sfxEnabled = enabled
    if (enabled) {
      println("🔊 Effets sonores activés")
    } else {
      println("🔇 Effets sonores désactivés (MUTE)")
    }
  }
  
  /**
   * Vérifie si la musique est activée
   */
  def isMusicEnabled: Boolean = musicEnabled
  
  /**
   * Vérifie si les SFX sont activés
   */
  def isSFXEnabled: Boolean = sfxEnabled
  
  // Sons d'effets - vérifient si SFX activés
  def playKillSound(): Unit = {
    if (sfxEnabled && areSFXInitialized && killSound != null) {
      try {
        killSound.play(0.4f) // Volume à 40% pour bien entendre les kills
      } catch {
        case e: Exception =>
          println(s"Erreur lors du son de kill: ${e.getMessage}")
      }
    }
  }
  
  def playUltimateSound(): Unit = {
    if (sfxEnabled && areSFXInitialized && ultimateSound != null) {
      try {
        ultimateSound.play(0.6f) // Volume à 60% pour l'ultimate (plus fort)
      } catch {
        case e: Exception =>
          println(s"Erreur lors du son d'ultimate: ${e.getMessage}")
      }
    }
  }
  
  def playBonusSound(): Unit = {
    if (sfxEnabled) {
      println("🎵 *Son de bonus* (pas de fichier SFX)")
    }
  }
  
  def playDamageSound(): Unit = {
    if (sfxEnabled) {
      println("🎵 *Son de dégâts* (pas de fichier SFX)")
    }
  }
  
  def playMenuSelectSound(): Unit = {
    if (sfxEnabled) {
      println("🎵 *Son de sélection menu* (pas de fichier SFX)")
    }
  }
  
  def playShootSound(): Unit = {
    if (sfxEnabled && areSFXInitialized && shootSound != null) {
      try {
        shootSound.play(0.3f) // Volume à 30% pour ne pas couvrir la musique
      } catch {
        case e: Exception =>
          println(s"Erreur lors du son de tir: ${e.getMessage}")
      }
    }
  }
  
  /**
   * Nettoie les ressources audio
   */
  def dispose(): Unit = {
    try {
      stopCurrentMusic()
      
      if (menuMusic != null) {
        menuMusic.dispose()
        menuMusic = null
      }
      if (gameplayMusic != null) {
        gameplayMusic.dispose()
        gameplayMusic = null
      }
      
      if (shootSound != null) {
        shootSound.dispose()
        shootSound = null
      }
      if (killSound != null) {
        killSound.dispose()
        killSound = null
      }
      if (ultimateSound != null) {
        ultimateSound.dispose()
        ultimateSound = null
      }
      
      currentMusic = null
      currentMusicType = "none"
      isMusicInitialized = false
      areSFXInitialized = false
      
      println("🎵 AudioManager nettoyé")
    } catch {
      case e: Exception =>
        println(s"Erreur lors du nettoyage audio: ${e.getMessage}")
    }
  }
}