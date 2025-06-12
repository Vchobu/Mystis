package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Projectile(var pos: Vector2, val velocity: Vector2) {
  val anim = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/projectiles/PNG/light/light_%d.png", 1, 3, 1f, 4f, true)
  val animHit = new SpriteAnimation("gdx2d-helloDesktop/data/sprites/projectiles/PNG/light_hit/light_hit_%d.png", 1, 5, 1f, 5f, false)

  def update(dt: Float): Unit = {
    pos.add(velocity)
    anim.updateAnimation(dt)
  }

  def isCollidingWith(g: GdxGraphics, enemy: Enemy, dt: Float): Boolean = {
    if (pos.dst(enemy.getCenter) < GameSettings.enemySize / 2) {
      animHit.drawAnimation(g, getCenter, false)
      animHit.updateAnimation(dt)
      true
    } else false
  }

  def draw(g: GdxGraphics): Unit = {
    anim.drawAnimation(g, getCenter, false)
  }

  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.enemySize / 2, pos.y + GameSettings.enemySize / 2)
}
