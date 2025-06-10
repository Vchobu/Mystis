package ch.hevs.gdx2d.hello

/**
 * Central configuration object containing all game settings, constants, and runtime state.
 * Includes player stats, enemy properties, projectile settings, audio/video options,
 * and the unified control system.
 */
object GameSettings {
  val width: Int = 1920
  val height: Int = 1080

  var playerCurrentHP: Float = 100f
  var playerMaxHP: Float = 100f
  var playerDamage: Float = 2.0f
  var playerSpeed: Float = 3f
  var playerSize: Float = 20f

  var enemyHP: Float = 3.0f
  var enemyDamage: Float = 1.5f
  var enemySpeed: Float = 1f
  var enemySize: Float = 30f
  var enemyLimit: Int = 10
  var enemySpawnMinDistance: Float = 800f

  var repelFromPlayerRadius: Float = 50f
  var repelFromPlayerStrength: Float = 20f

  var projectileCooldown: Float = 0.5f
  var projectileSpeed: Float = 6f
  var projectileSize: Float = 5f
  var projectileShootRange: Float = 300f
  var projectileTimeSinceLastShot: Float = 0f

  // === OPTIONS PARAMETERS ===
  var masterVolume: Float = 0.8f
  var musicVolume: Float = 0.7f
  var sfxVolume: Float = 0.9f
  var subtitlesEnabled: Boolean = true
  
  // === AUDIO ON/OFF SETTINGS ===
  var musicEnabled: Boolean = true
  var sfxEnabled: Boolean = true
  
  // === GAME STATE ===
  var isGamePaused: Boolean = false
  var previousState: String = "MENU" // Pour se rappeler d'où on vient

  // === CONTROLS SYSTEM ===
  object Controls {
    import com.badlogic.gdx.Input
    
    // Key mappings - primary and secondary for each action
    var moveUp: (Int, Int) = (Input.Keys.W, Input.Keys.UP)
    var moveDown: (Int, Int) = (Input.Keys.S, Input.Keys.DOWN)
    var moveLeft: (Int, Int) = (Input.Keys.A, Input.Keys.LEFT)
    var moveRight: (Int, Int) = (Input.Keys.D, Input.Keys.RIGHT)
    var jump: (Int, Int) = (Input.Keys.SPACE, Input.Keys.NUMPAD_0)
    var attack: (Int, Int) = (-1, Input.Keys.CONTROL_LEFT) // -1 = left mouse
    var magic: (Int, Int) = (-2, Input.Keys.ALT_LEFT) // -2 = right mouse
    var inventory: (Int, Int) = (Input.Keys.I, Input.Keys.TAB)
    var menu: (Int, Int) = (Input.Keys.ESCAPE, Input.Keys.B)
    var map: (Int, Int) = (Input.Keys.M, Input.Keys.F1)
    
    // Bonus selection keys
    var bonus1: (Int, Int) = (Input.Keys.NUM_1, Input.Keys.NUMPAD_1)
    var bonus2: (Int, Int) = (Input.Keys.NUM_2, Input.Keys.NUMPAD_2)
    var bonus3: (Int, Int) = (Input.Keys.NUM_3, Input.Keys.NUMPAD_3)
    
    // Helper functions
    def isKeyPressed(action: String, keycode: Int): Boolean = {
      action match {
        case "moveUp" => keycode == moveUp._1 || keycode == moveUp._2
        case "moveDown" => keycode == moveDown._1 || keycode == moveDown._2
        case "moveLeft" => keycode == moveLeft._1 || keycode == moveLeft._2
        case "moveRight" => keycode == moveRight._1 || keycode == moveRight._2
        case "jump" => keycode == jump._1 || keycode == jump._2
        case "attack" => keycode == attack._2 // keyboard only for this check
        case "magic" => keycode == magic._2 // keyboard only for this check
        case "inventory" => keycode == inventory._1 || keycode == inventory._2
        case "menu" => keycode == menu._1 || keycode == menu._2
        case "map" => keycode == map._1 || keycode == map._2
        case "bonus1" => keycode == bonus1._1 || keycode == bonus1._2
        case "bonus2" => keycode == bonus2._1 || keycode == bonus2._2
        case "bonus3" => keycode == bonus3._1 || keycode == bonus3._2
        case _ => false
      }
    }
    
    def isMousePressed(action: String, button: Int): Boolean = {
      action match {
        case "attack" => button == 0 || attack._1 == -1 // left mouse
        case "magic" => button == 1 || magic._1 == -2 // right mouse
        case _ => false
      }
    }
    
    def updateKeyBinding(action: String, primary: Boolean, keycode: Int): Unit = {
      action match {
        case "moveUp" => if (primary) moveUp = (keycode, moveUp._2) else moveUp = (moveUp._1, keycode)
        case "moveDown" => if (primary) moveDown = (keycode, moveDown._2) else moveDown = (moveDown._1, keycode)
        case "moveLeft" => if (primary) moveLeft = (keycode, moveLeft._2) else moveLeft = (moveLeft._1, keycode)
        case "moveRight" => if (primary) moveRight = (keycode, moveRight._2) else moveRight = (moveRight._1, keycode)
        case "jump" => if (primary) jump = (keycode, jump._2) else jump = (jump._1, keycode)
        case "attack" => if (primary) attack = (keycode, attack._2) else attack = (attack._1, keycode)
        case "magic" => if (primary) magic = (keycode, magic._2) else magic = (magic._1, keycode)
        case "inventory" => if (primary) inventory = (keycode, inventory._2) else inventory = (inventory._1, keycode)
        case "menu" => if (primary) menu = (keycode, menu._2) else menu = (menu._1, keycode)
        case "map" => if (primary) map = (keycode, map._2) else map = (map._1, keycode)
        case _ =>
      }
    }
    
    def getKeyName(keycode: Int): String = {
      keycode match {
        case -1 => "Click_G"
        case -2 => "Click_D"
        case Input.Keys.W => "W"
        case Input.Keys.A => "A"
        case Input.Keys.S => "S"
        case Input.Keys.D => "D"
        case Input.Keys.SPACE => "SPACE"
        case Input.Keys.ESCAPE => "ESC"
        case Input.Keys.TAB => "TAB"
        case Input.Keys.CONTROL_LEFT => "Ctrl"
        case Input.Keys.ALT_LEFT => "Alt"
        case Input.Keys.M => "M"
        case Input.Keys.I => "I"
        case Input.Keys.F1 => "F1"
        case Input.Keys.NUM_1 => "1"
        case Input.Keys.NUM_2 => "2"
        case Input.Keys.NUM_3 => "3"
        case Input.Keys.NUMPAD_0 => "Num0"
        case Input.Keys.NUMPAD_1 => "Num1"
        case Input.Keys.NUMPAD_2 => "Num2"
        case Input.Keys.NUMPAD_3 => "Num3"
        case Input.Keys.NUMPAD_4 => "Num4"
        case Input.Keys.NUMPAD_5 => "Num5"
        case Input.Keys.NUMPAD_6 => "Num6"
        case Input.Keys.NUMPAD_8 => "Num8"
        case _ => Input.Keys.toString(keycode)
      }
    }
  }
}
