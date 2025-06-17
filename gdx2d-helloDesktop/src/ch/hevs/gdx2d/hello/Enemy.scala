package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2

/**
 * Classe représentant les ennemis dans le jeu Mystis.
 * 
 * Supporte deux types d'ennemis avec des sprites et comportements différents:
 * - Type 0: Agis (ennemis terrestres avec animations de marche)
 * - Type 1: Floating Skull (crânes volants avec animations flottantes)
 * 
 * Fonctionnalités:
 * - IA de poursuite du joueur avec évitement entre ennemis
 * - Système de santé et de dégâts
 * - Animations d'état (vivant, mort)
 * - Collision avec le joueur et les projectiles
 * - Système de répulsion pour éviter l'accumulation
 * 
 * @param pos Position initiale de l'ennemi dans le monde
 * @param enemyType Type d'ennemi (0 = Agis, 1 = Floating Skull)
 */
class Enemy(val pos: Vector2, val enemyType: Int) {

  // === PROPRIÉTÉS DE BASE ===
  var hp: Float = _                           // Points de vie actuels
  var velocity: Vector2 = new Vector2(0, 0)   // Vecteur de déplacement
  var floatAnim: SpriteAnimation = _          // Animation principale (vivant)
  var deathAnim: SpriteAnimation = _          // Animation de mort
  var state: String = "alive"                 // État actuel ("alive", "dead")
  var flipX: Boolean = _                      // Direction d'affichage (gauche/droite)

  enemyType match {
    case 0 =>
      hp = GameSettings.enemyHP
      floatAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/floatingSkull/separated_frames/skull_%d.png", 1, 10, 2f, 10f, true)
      deathAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/floatingSkull/separated_frames/skull_%d.png", 11, 17, 2f, 7f, false)
      flipX = false

    case 1 =>
      hp = GameSettings.enemyHP * 1.5f
      floatAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/agis/separated_frames/agis_%d.png", 1, 17, 0.5f, 8.5f, true)
      deathAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/agis/separated_frames/agisDeath_%d.png", 1, 65, 0.5f, 32.5f, false)
      flipX = false
  }

  def isDead: Boolean = hp <= 0

  def takeDamage(): Unit = {
    if(state == "alive") {
      hp -= GameSettings.playerDamage
      if (hp <= 0) {
        state = "dying"
        deathAnim.reset()
      }
    }
  }

  def update(playerPos: Vector2, dt: Float): Unit = {
    state match {
      case "alive" =>
        val direction = playerPos.cpy().sub(getCenter)
        flipX = direction.x < 0
        val moveDir = direction.nor().scl(GameSettings.enemySpeed)
        pos.add(moveDir)
        pos.add(velocity)
        velocity.scl(0.9f)
        floatAnim.updateAnimation(dt)
      case "dying" =>
        deathAnim.updateAnimation(dt)
        if (deathAnim.isFinished()) {
          state = "dead"
        }
      case "dead" => ()
    }
  }

  /**
   * Apply repulsion force from another enemy to prevent overlap.
   * Used for enemy-to-enemy collision avoidance.
   * 
   * @param other Position of the other enemy
   * @param strength Strength of the repulsion force
   */
  def repelFromEachOther(other: Vector2, strength: Float): Unit = {
    val dir = getCenter.cpy().sub(other)
    val distance = dir.len()
    if (distance > 0 && distance < GameSettings.enemySize) {
      // Apply repulsion force inversely proportional to distance
      dir.nor().scl(strength * (1f - distance / GameSettings.enemySize))
      pos.add(dir)
    }
  }

  /**
   * Apply repulsion force when hit by player or player abilities.
   * Pushes enemy away from player position.
   * 
   * @param playerPos Player's current position
   */
  def repelFromPlayer(playerPos: Vector2): Unit = {
    val dir = getCenter.cpy().sub(playerPos)
    val distance = dir.len()
    if (distance < GameSettings.repelFromPlayerRadius && distance > 0.01f) {
      // Calculate repulsion force based on distance to player
      dir.nor().scl((1 - distance / GameSettings.repelFromPlayerRadius) * GameSettings.repelFromPlayerStrength)
      println(dir) // Debug output for repulsion vector
      velocity.add(dir) // Add to velocity for smooth movement
    }
  }

  /**
   * Render the enemy sprite based on current state.
   * Displays appropriate animation for alive, dying, or dead states.
   * 
   * @param g Graphics context for rendering
   */
  def draw(g: GdxGraphics): Unit = {
    state match {
      case "alive" => floatAnim.drawAnimation(g, getCenter, flipX) // Draw floating animation
      case "dying" => deathAnim.drawAnimation(g, getCenter, flipX) // Draw death animation
      case "dead" => () // No rendering for dead enemies
    }
  }

  /**
   * Get the center position of the enemy for collision detection and AI calculations.
   * 
   * @return Vector2 representing the enemy's center position
   */
  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.enemySize / 2, pos.y + GameSettings.enemySize / 2)

  /**
   * Check if this enemy is colliding with the player.
   * Uses circular collision detection based on combined radii.
   * 
   * @param player Player instance to check collision against
   * @return true if colliding, false otherwise
   */
  def isCollidingWith(player: Player): Boolean = {
    val dist = getCenter.dst(player.getCenter) // Calculate distance between centers
    dist < (GameSettings.enemySize + GameSettings.playerSize) / 2 // Check if distance is less than combined radii
  }
}

