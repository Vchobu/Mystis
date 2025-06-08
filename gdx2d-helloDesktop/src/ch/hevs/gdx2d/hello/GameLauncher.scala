package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.{MathUtils, Vector2}

import scala.util.Random
import scala.collection.mutable.ArrayBuffer

class Game extends PortableApplication(GameSettings.width, GameSettings.height) {

  var player: Player = _
  val enemies: ArrayBuffer[Enemy] = new ArrayBuffer[Enemy]()
  val projectiles: ArrayBuffer[Projectile] = new ArrayBuffer[Projectile]()
  val random: Random = new Random()
  var gameOver: Boolean = false

  var map: TiledMap = _
  var mapRenderer: OrthogonalTiledMapRenderer = _
  var camera: OrthographicCamera = _


  override def onInit(): Unit = {
    setTitle("Mystis")

    camera = new OrthographicCamera()
    camera.setToOrtho(false, GameSettings.width, GameSettings.height)
    //camera.zoom = 4.0f
    map = new TmxMapLoader().load("maps/map.tmx")
    mapRenderer = new OrthogonalTiledMapRenderer(map, 0.3f)

    player = new Player(new Vector2(GameSettings.width / 2, GameSettings.height / 2))
    for (_ <- 0 until GameSettings.enemyLimit) {
      enemies.append(new Enemy(randomEnemyPosition()))
    }
  }

  def randomEnemyPosition(): Vector2 = {
    var pos: Vector2 = null
    do {
      val x = random.nextInt(GameSettings.width)
      val y = random.nextInt(GameSettings.height)
      pos = new Vector2(x, y)
    } while (pos.dst(player.pos) < GameSettings.enemySpawnMinDistance)
    pos
  }

  def createEnemyAwayFromPlayer(playerPos: Vector2): Enemy = {
    var pos = new Vector2()
    do {
      pos = new Vector2(MathUtils.random(0f, getWindowWidth), MathUtils.random(0f, getWindowHeight))
    } while (pos.dst(playerPos) < GameSettings.enemySpawnMinDistance)
    new Enemy(pos)
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()
    camera.update()
    mapRenderer.setView(camera)
    mapRenderer.render()

    if (!gameOver) {

      var playerTouched = false
      val deltaTime = Gdx.graphics.getDeltaTime
      player.update(deltaTime)
      player.updateShootTimer(deltaTime)

      val nearestEnemyToShoot = enemies.minByOption(_.getCenter.dst(player.getCenter))
      nearestEnemyToShoot.foreach { enemy =>
        val distance = enemy.getCenter.dst(player.getCenter)
        if (distance <= GameSettings.projectileShootRange && player.canShoot && enemy.state == "alive") {
          val direction = enemy.getCenter.cpy().sub(player.getCenter).nor()
          val projectile = new Projectile(player.getCenter.cpy(), direction.scl(GameSettings.projectileSpeed))
          projectiles.append(projectile)
          player.resetShootTimer()
        }
      }

      val deadProjectiles = new ArrayBuffer[Projectile]()
      for (p <- projectiles) {
        p.update()
        for (e <- enemies) {
          if (p.isCollidingWith(e)) {
            e.takeDamage()
            deadProjectiles.append(p)
          }
        }
        p.draw(g)
      }
      projectiles --= deadProjectiles

      for (e <- enemies) {
        e.update(player.getCenter, deltaTime)
        if (e.isCollidingWith(player)) {
          playerTouched = true
        }
      }

      val deadEnemies = enemies.filter(e => e.state == "dead")
      for (e <- deadEnemies) {
        enemies -= e
        val newEnemy = createEnemyAwayFromPlayer(player.getCenter)
        enemies.append(newEnemy)
      }

      if (playerTouched) {
        for (e <- enemies) {
          e.repelFromPlayer(player.getCenter)
        }
      }

      for (e <- enemies) {
        for (other <- enemies) {
          if (e != other && e.getCenter.dst(other.getCenter) < GameSettings.enemySize) {
            e.repelFromEachOther(other.getCenter, 1.5f)
          }
        }

        e.draw(g)
      }
    }

    player.draw(g)
  }
}

object GameLauncher extends App {
  new Game()
}

