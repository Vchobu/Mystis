package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2

class Enemy(val pos: Vector2) {

  var hp: Int = GameSettings.enemyHP
  var velocity: Vector2 = new Vector2(0, 0)
  val floatAnim = new SpriteAnimation("sprites/floatingSkull/separated_frames/skull_%d.png", 1, 10, 2f, 10f, true)
  val deathAnim = new SpriteAnimation("sprites/floatingSkull/separated_frames/skull_%d.png", 11, 17, 2f, 7f, false)
  var state: String = "alive"
  var flipX: Boolean = false

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

  def repelFromEachOther(other: Vector2, strength: Float): Unit = {
    val dir = getCenter.cpy().sub(other)
    val distance = dir.len()
    if (distance > 0 && distance < GameSettings.enemySize) {
      dir.nor().scl(strength * (1f - distance / GameSettings.enemySize))
      pos.add(dir)
    }
  }

  def repelFromPlayer(playerPos: Vector2): Unit = {
    val dir = getCenter.cpy().sub(playerPos)
    val distance = dir.len()
    if (distance < GameSettings.repelFromPlayerRadius && distance > 0.01f) {
      dir.nor().scl((1 - distance / GameSettings.repelFromPlayerRadius) * GameSettings.repelFromPlayerStrength)
      println(dir)
      velocity.add(dir)
    }
  }

  def draw(g: GdxGraphics): Unit = {
    state match {
      case "alive" => floatAnim.drawAnimation(g, getCenter, flipX)
      case "dying" => deathAnim.drawAnimation(g, getCenter, flipX)
      case "dead" => ()
    }
  }

  def getCenter: Vector2 = new Vector2(pos.x + GameSettings.enemySize / 2, pos.y + GameSettings.enemySize / 2)

  def isCollidingWith(player: Player): Boolean = {
    val dist = getCenter.dst(player.getCenter)
    dist < (GameSettings.enemySize + GameSettings.playerSize) / 2
  }
}

