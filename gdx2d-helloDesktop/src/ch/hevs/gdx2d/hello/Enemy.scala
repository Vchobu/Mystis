package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Enemy(val pos: Vector2) {

  var hp: Int = GameSettings.enemyHP
  def isDead: Boolean = hp <= 0
  def takeDamage(): Unit = {
    hp -= GameSettings.playerDamage
  }

  def update(playerCenter: Vector2): Unit = {
    val dir = playerCenter.cpy().sub(getCenter)
    dir.nor().scl(GameSettings.enemySpeed)
    pos.add(dir)
  }

  def repelFromEachOther(other: Vector2, strength: Float): Unit = {
    val dir = getCenter.cpy().sub(other)
    val distance = dir.len()
    if (distance > 0 && distance < GameSettings.enemySize) {
      dir.nor().scl(strength * (1f - distance / GameSettings.enemySize))
      pos.add(dir)
    }
  }

  def repelFromPlayer(playerCenter: Vector2, radius: Float, strength: Float): Unit = {
    val dist = getCenter.dst(playerCenter)
    if (dist < radius && dist > 0) {
      val dir = getCenter.cpy().sub(playerCenter).nor()
      val factor = 1f - (dist / radius)
      pos.add(dir.scl(strength * factor))
    }
  }

  def draw(g: GdxGraphics): Unit = {
    g.drawFilledRectangle(pos.x, pos.y, GameSettings.enemySize, GameSettings.enemySize, 0, new Color(1, 0, 0, 1))
  }

  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.enemySize / 2, pos.y + GameSettings.enemySize / 2)

  def isCollidingWith(player: Player): Boolean = {
    val dist = getCenter.dst(player.getCenter)
    dist < (GameSettings.enemySize + GameSettings.playerSize) / 2
  }
}

