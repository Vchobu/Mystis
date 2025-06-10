package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

/**
 * Player character class that handles movement, animations, health management, and input processing.
 * The player is controlled by keyboard input and can move in all directions, shoot projectiles,
 * and interact with the game world.
 * 
 * @param pos Initial position of the player
 */
class Player(var pos: Vector2) {

  // Animation system
  val idleAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/mage/animations/wizard/PNG/idle/idle_%d.png", 1, 8, 2f, 8f, true) // Idle animation when not moving
  val walkAnim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/mage/animations/wizard/PNG/walk/walk_%d.png", 1, 10, 2f, 10f, true) // Walking animation

  // Animation state
  var currentAnim: SpriteAnimation = idleAnim // Currently playing animation
  var facingLeft = false // Direction the player is facing

  // Health system (local copies for quick access)
  var currentHP: Float = GameSettings.playerCurrentHP // Current health points
  var maxHP: Float = GameSettings.playerMaxHP // Maximum health points


  /**
   * Check if the player is dead (health <= 0).
   * 
   * @return true if player is dead, false otherwise
   */
  def isDead: Boolean = GameSettings.playerCurrentHP <= 0

  /**
   * Apply standard enemy damage to the player.
   * Updates global health setting.
   */
  def takeDamage(): Unit = {
    GameSettings.playerCurrentHP -= GameSettings.enemyDamage
  }

  /**
   * Apply specific amount of damage to the player.
   * Ensures health doesn't go below 0 and synchronizes local health variable.
   * 
   * @param damage Amount of damage to apply
   */
  def takeDamage(damage: Float): Unit = {
    GameSettings.playerCurrentHP = math.max(0, GameSettings.playerCurrentHP - damage)
    currentHP = GameSettings.playerCurrentHP // Synchronize local variable
  }

  /**
   * Check if the player can shoot based on cooldown timer.
   * 
   * @return true if enough time has passed since last shot, false otherwise
   */
  def canShoot: Boolean = GameSettings.projectileTimeSinceLastShot >= GameSettings.projectileCooldown

  /**
   * Reset the shooting cooldown timer.
   * Called after firing a projectile.
   */
  def resetShootTimer(): Unit = {
    GameSettings.projectileTimeSinceLastShot = 0f
  }

  /**
   * Update the shooting cooldown timer.
   * 
   * @param dt Delta time since last frame
   */
  def updateShootTimer(dt: Float): Unit = {
    GameSettings.projectileTimeSinceLastShot += dt
  }

  /**
   * Update player movement, animations, and state.
   * Handles keyboard input for movement and updates appropriate animations.
   * 
   * @param dt Delta time since last frame
   */
  def update(dt: Float): Unit = {
    // Don't move if game is paused
    if (GameSettings.isGamePaused) {
      currentAnim = idleAnim
      currentAnim.updateAnimation(dt)
      return
    }

    val direction = new Vector2(0, 0)

    // Use unified control system for input handling
    if (Gdx.input.isKeyPressed(GameSettings.Controls.moveUp._1) || Gdx.input.isKeyPressed(GameSettings.Controls.moveUp._2)) {
      direction.y += 1
    }
    if (Gdx.input.isKeyPressed(GameSettings.Controls.moveDown._1) || Gdx.input.isKeyPressed(GameSettings.Controls.moveDown._2)) {
      direction.y -= 1
    }
    if (Gdx.input.isKeyPressed(GameSettings.Controls.moveLeft._1) || Gdx.input.isKeyPressed(GameSettings.Controls.moveLeft._2)) {
      direction.x -= 1
      facingLeft = true
    }
    if (Gdx.input.isKeyPressed(GameSettings.Controls.moveRight._1) || Gdx.input.isKeyPressed(GameSettings.Controls.moveRight._2)) {
      direction.x += 1
      facingLeft = false
    }

    if (!direction.isZero) {
      direction.nor().scl(GameSettings.playerSpeed)
      pos.add(direction)
      currentAnim = walkAnim
    } else {
      currentAnim = idleAnim
    }

    currentAnim.updateAnimation(dt)
  }

  /**
   * Render the player sprite with current animation.
   * 
   * @param g Graphics context for rendering
   */
  def draw(g: GdxGraphics): Unit = {
    currentAnim.drawAnimation(g, getCenter, facingLeft) // Draw current animation frame
  }

  /**
   * Get the center position of the player for collision detection and rendering.
   * 
   * @return Vector2 representing the player's center position
   */
  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.playerSize / 2, pos.y + GameSettings.playerSize / 2)
}
