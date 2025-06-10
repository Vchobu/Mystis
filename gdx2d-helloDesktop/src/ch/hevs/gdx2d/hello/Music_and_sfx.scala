package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.audio.MusicPlayer

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
      menuMusic = new MusicPlayer("gdx2d-helloDesktop/data/Soundtrack/Ori and the Will of the Wisps - Soundtrack - Main Theme_and_A Yearning for the Sky.mp3.wav")
      gameplayMusic = new MusicPlayer("gdx2d-helloDesktop/data/Soundtrack/Ori and the Will of the Wisps - Soundtrack - Escaping a Foul Presence.mp3_and_Shriek and Ori.wav")
      isMusicInitialized = true
      
      println(s"🎵 AudioManager initialisé - Musique: ${if (musicEnabled) "ON" else "OFF"}, SFX: ${if (sfxEnabled) "ON" else "OFF"}")
      
    } catch {
      case e: Exception =>
        println(s"Erreur lors du chargement audio: ${e.getMessage}")
        isMusicInitialized = false
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
    if (sfxEnabled) {
      println("🎵 *Son de kill* (pas de fichier SFX)")
    }
  }
  
  def playUltimateSound(): Unit = {
    if (sfxEnabled) {
      println("🎵 *Son d'ultimate* (pas de fichier SFX)")
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
      
      currentMusic = null
      currentMusicType = "none"
      isMusicInitialized = false
      
      println("🎵 AudioManager nettoyé")
    } catch {
      case e: Exception =>
        println(s"Erreur lors du nettoyage audio: ${e.getMessage}")
    }
  }
}