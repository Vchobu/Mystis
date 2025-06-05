package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Projectile(var pos: Vector2, val velocity: Vector2) {
  val size = GameSettings.projectileSize

  def update(): Unit = {
    pos.add(velocity)
  }

  def isCollidingWith(enemy: Enemy): Boolean = {
    pos.dst(enemy.getCenter) < GameSettings.enemySize / 2
  }

  def draw(g: GdxGraphics): Unit = {
    g.drawFilledCircle(pos.x, pos.y, size, new Color(Color.GREEN))
  }
}
