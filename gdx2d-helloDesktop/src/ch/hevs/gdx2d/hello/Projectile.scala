package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * Projectile class representing magic spells fired by the player.
 * Projectiles automatically move towards their target and handle collision detection with enemies.
 * Features animated sprites for both flight and impact effects.
 * 
 * @param pos Initial position vector of the projectile
 * @param velocity Movement vector determining speed and direction
 */
class Projectile(var pos: Vector2, val velocity: Vector2) {
  
  // Animation for projectile in flight (light magic effect, looping)
  val anim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/projectiles/PNG/light/light_%d.png", 1, 3, 1f, 4f, true)
  
  // Animation for projectile impact (light hit effect, plays once)
  val animHit = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/projectiles/PNG/light_hit/light_hit_%d.png", 1, 5, 1f, 5f, false)

  /**
   * Update projectile position and animation each frame.
   * Moves the projectile based on its velocity vector and advances the flight animation.
   * 
   * @param dt Delta time since last frame for smooth movement
   */
  def update(dt: Float): Unit = {
    pos.add(velocity) // Move projectile by velocity vector
    anim.updateAnimation(dt) // Update flight animation frame
  }

  /**
   * Check collision with an enemy and display hit effect if collision occurs.
   * Uses distance calculation between projectile center and enemy center.
   * 
   * @param g Graphics context for rendering hit animation
   * @param enemy Enemy object to check collision against
   * @param dt Delta time for hit animation update
   * @return true if collision detected, false otherwise
   */
  def isCollidingWith(g: GdxGraphics, enemy: Enemy, dt: Float): Boolean = {
    if (pos.dst(enemy.getCenter) < GameSettings.enemySize / 2) {
      // Draw and update hit animation at collision point
      animHit.drawAnimation(g, getCenter, false)
      animHit.updateAnimation(dt)
      true
    } else false
  }

  /**
   * Render the projectile's flight animation at its current position.
   * 
   * @param g Graphics context for drawing
   */
  def draw(g: GdxGraphics): Unit = {
    anim.drawAnimation(g, getCenter, false)
  }

  /**
   * Calculate the center point of the projectile for collision detection and rendering.
   * Uses enemy size for positioning calculations (legacy from collision system).
   * 
   * @return Vector2 representing the center position of the projectile
   */
  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.enemySize / 2, pos.y + GameSettings.enemySize / 2)
}
