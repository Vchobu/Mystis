package ch.hevs.gdx2d.hello

object GameSettings {
  val width: Int = 1920
  val height: Int = 1080

  var playerHP: Int = 10
  var playerDamage: Int = 2
  var playerSpeed: Float = 3f
  var playerSize: Float = 20f

  var enemyHP: Int = 3
  var enemyDamage: Int = 1
  var enemySpeed: Float = 1f
  var enemySize: Float = 20f
  var enemyLimit: Int = 20
  var enemySpawnMinDistance: Float = 800f

  var projectileCooldown: Float = 0.5f
  var projectileSpeed: Float = 6f
  var projectileSize: Float = 5f
  var projectileShootRange: Float = 300f
  var projectileTimeSinceLastShot = 0f
}
