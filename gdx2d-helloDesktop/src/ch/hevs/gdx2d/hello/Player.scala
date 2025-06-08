package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

class Player(var pos: Vector2) {

  val idleAnim = new SpriteAnimation("sprites/mage/animations/wizard/PNG/idle/idle_%d.png", 1, 8, 2f, 8f, true)
  val walkAnim = new SpriteAnimation("sprites/mage/animations/wizard/PNG/walk/walk_%d.png", 1, 10, 2f, 10f, true)

  var currentAnim: SpriteAnimation = idleAnim
  var facingLeft = false

  def isDead: Boolean = GameSettings.playerHP <= 0

  def takeDamage(): Unit = {
    GameSettings.playerHP -= GameSettings.enemyDamage
  }

  def canShoot: Boolean = GameSettings.projectileTimeSinceLastShot >= GameSettings.projectileCooldown

  def resetShootTimer(): Unit = {
    GameSettings.projectileTimeSinceLastShot = 0f
  }

  def updateShootTimer(dt: Float): Unit = {
    GameSettings.projectileTimeSinceLastShot += dt
  }

  def update(dt: Float): Unit = {
    val direction = new Vector2(0, 0)

    if (Gdx.input.isKeyPressed(Input.Keys.W)) direction.y += 1
    if (Gdx.input.isKeyPressed(Input.Keys.S)) direction.y -= 1
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      direction.x -= 1
      facingLeft = true
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
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

  def draw(g: GdxGraphics): Unit = {
    currentAnim.drawAnimation(g, getCenter, facingLeft)
  }

  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.playerSize / 2, pos.y + GameSettings.playerSize / 2)
}
