package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Player(var pos: Vector2) {

  def isDead: Boolean = GameSettings.playerHP <= 0
  def takeDamage(): Unit = {
    GameSettings.playerHP -= GameSettings.enemyDamage
  }

  def canShoot: Boolean = GameSettings.projectileTimeSinceLastShot >= GameSettings.projectileCooldown
  def resetShootTimer(): Unit = {
    GameSettings.projectileTimeSinceLastShot = 0f
  }

  def updateShootTimer(delta: Float): Unit = {
    GameSettings.projectileTimeSinceLastShot += delta
  }

  def update(): Unit = {
    val direction = new Vector2(0, 0)

    if (Gdx.input.isKeyPressed(Input.Keys.W)) direction.y += 1
    if (Gdx.input.isKeyPressed(Input.Keys.S)) direction.y -= 1
    if (Gdx.input.isKeyPressed(Input.Keys.A)) direction.x -= 1
    if (Gdx.input.isKeyPressed(Input.Keys.D)) direction.x += 1

    if (!direction.isZero) {
      direction.nor().scl(GameSettings.playerSpeed)
      pos.add(direction)
    }
  }

  def draw(g: GdxGraphics): Unit = {
    g.drawFilledRectangle(pos.x, pos.y, GameSettings.playerSize, GameSettings.playerSize, 0, new Color(1, 1, 1, 1))
  }

  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.playerSize / 2, pos.y + GameSettings.playerSize / 2)
}

