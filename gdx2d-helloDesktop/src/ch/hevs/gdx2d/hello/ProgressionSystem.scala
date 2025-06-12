package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.Color

case class BonusCard(name: String, bonusType: String, value: Float) {
  def description: String = bonusType match {
    case "damage" => s"+${(value * 100).toInt}% damage"
    case "speed" => s"+${(value * 100).toInt}% vitesse"
    case "health" => s"+${value.toInt} points de vie"
    case "crit" => s"+${(value * 100).toInt}% chance de critique"
    case "regen" => s"+${value} regeneration per second"
    case _ => "Mysterious bonus"
  }
}

class ProgressionSystem {
  private var currentLevel: Int = 1
  private var currentXP: Float = 0.0f
  private var xpToNextLevel: Float = 100.0f
  private val xpMultiplier: Float = 1.5f
  private var ultimateCharges: Int = 0

  var isLevelUpPaused: Boolean = false
  var currentBonusCards: List[BonusCard] = List.empty

  private val enemyXPValues = Map(
    "basicEnemy" -> 10.0f,
    "eliteEnemy" -> 25.0f,
    "miniBoss" -> 50.0f,
    "bossEnemy" -> 100.0f,
  )

  def onEnemyKilled(enemyType: String): Unit = {
    val xpGained = enemyXPValues.getOrElse(enemyType, 5.0f)
    addXP(xpGained)
  }

  private def addXP(xp: Float): Unit = {
    currentXP += xp
    while (currentXP >= xpToNextLevel) {
      levelUp()
    }
  }

  private def levelUp(): Unit = {
    currentXP -= xpToNextLevel
    currentLevel += 1
    ultimateCharges += 1
    xpToNextLevel = (xpToNextLevel * xpMultiplier).toFloat
    triggerLevelUpRewards()
    println(s"Level Up! New level: $currentLevel")
  }

  private def triggerLevelUpRewards(): Unit = {
    currentBonusCards = generateBonusCards(3)
    isLevelUpPaused = true
    println("LEVEL UP! Choose your bonus (keys 1, 2, or 3)")
  }

  private def generateBonusCards(count: Int): List[BonusCard] = {
    val availableBonuses = List(
      BonusCard("Damage +10%", "damage", 0.1f),
      BonusCard("Vitesse d'Attaque +15%", "speed", 0.15f),
      BonusCard("Vie Maximum +20", "health", 20.0f),
      BonusCard("Chance Critique +5%", "crit", 0.05f),
      BonusCard("Regeneration +2/s", "regen", 2.0f),
      BonusCard("Range +20%", "range", 0.2f),
      BonusCard("Vitesse Projectile +25%", "projectileSpeed", 0.25f),
      BonusCard("Double Tir", "multiShot", 1.0f)
    )
    scala.util.Random.shuffle(availableBonuses).take(count)
  }

  def chooseBonusCard(cardIndex: Int): Unit = {
    if (cardIndex >= 0 && cardIndex < currentBonusCards.length) {
      val chosenCard = currentBonusCards(cardIndex)
      
      // Jouer le son de sélection de bonus
      AudioManager.playBonusSound()
      
      applyBonusCard(chosenCard)
      isLevelUpPaused = false
      currentBonusCards = List.empty
      println(s"Bonus chosen: ${chosenCard.name}")
    }
  }

  private def applyBonusCard(card: BonusCard): Unit = {
    card.bonusType match {
      case "damage" => 
        GameSettings.playerDamage *= (1 + card.value)
        println(s"Damage increased by ${(card.value * 100).toInt}%! New damage: ${GameSettings.playerDamage}")
      case "speed" => 
        GameSettings.projectileCooldown *= (1 - card.value)
        println(s"Attack speed increased by ${(card.value * 100).toInt}%! New cooldown: ${GameSettings.projectileCooldown}")
      case "health" => 
        GameSettings.playerMaxHP += card.value
        GameSettings.playerCurrentHP += card.value
        println(s"Maximum health increased by ${card.value.toInt}! New max health: ${GameSettings.playerMaxHP}")
      case "crit" => 
        println(s"Critical chance increased by ${(card.value * 100).toInt}%!")
        // TODO: Implémenter système de critique
      case "regen" => 
        println(s"Regeneration increased by ${card.value}/s!")
        // TODO: Implémenter régénération
      case "range" => 
        GameSettings.projectileShootRange *= (1 + card.value)
        println(s"Range increased by ${(card.value * 100).toInt}%! New range: ${GameSettings.projectileShootRange}")
      case "projectileSpeed" => 
        GameSettings.projectileSpeed *= (1 + card.value)
        println(s"Projectile speed increased by ${(card.value * 100).toInt}%! New speed: ${GameSettings.projectileSpeed}")
      case "multiShot" => 
        println("Double shot unlocked!")
        // TODO: Implémenter double tir
      case _ => println("Mysterious bonus applied!")
    }
  }

  // GETTERS
  def getCurrentLevel: Int = currentLevel
  def getCurrentXP: Float = currentXP
  def getXPToNextLevel: Float = xpToNextLevel
  def getProgressPercentage: Float = currentXP / xpToNextLevel
  def getUltimateCharges: Int = ultimateCharges

  // UI METHODS
  def drawAllUI(g: GdxGraphics, screenWidth: Float, screenHeight: Float,
                playerCurrentHP: Float, playerMaxHP: Float): Unit = {
    drawHealthBar(g, 110, screenHeight - 40, 200, 20, playerCurrentHP, playerMaxHP)
    drawProgressBar(g, 110, screenHeight - 100, 200, 20)
    drawUltimateBar(g, 110, screenHeight - 180, 200, 20)
  }

  def drawHealthBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float,
                    currentHP: Float, maxHP: Float): Unit = {
    val healthPercentage = if (maxHP > 0) currentHP / maxHP else 0f
    g.setColor(MystisColors.MYSTIS_BROWN)
    g.drawFilledRectangle(x, y, width, height, 0)
    if (healthPercentage > 0) {
      g.setColor(MystisColors.RED)
      g.drawFilledRectangle(x, y, width * healthPercentage, height, 0)
    }
    g.setColor(MystisColors.GOLD)
    g.drawRectangle(x, y, width, height, 0)
    g.drawString(15, y - 20, s"Health: ${currentHP.toInt}/${maxHP.toInt}")
  }

  def drawProgressBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float): Unit = {
    val progress = getProgressPercentage
      g.setColor(MystisColors.MYSTIS_BROWN)
    g.drawFilledRectangle(x, y, width, height, 0)
    if (progress > 0) {
      g.setColor(MystisColors.GREEN)
      g.drawFilledRectangle(x, y, width * progress, height, 0)
    }
    g.setColor(MystisColors.GOLD)
    g.drawRectangle(x, y, width, height, 0)
    g.drawString(15, y - 20, s"Level: $currentLevel")
    g.drawString(15, y - 40, s"XP: ${currentXP.toInt}/${xpToNextLevel.toInt}")
  }

  def drawUltimateBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float): Unit = {
    val maxCharges = 4
    val chargeWidth = width / maxCharges
    for (i <- 1 to maxCharges) {
      val chargeX = i * chargeWidth - 10
      g.setColor(MystisColors.MYSTIS_BROWN)
      g.drawFilledRectangle(chargeX, y, chargeWidth, height, 0)
      if (i <= ultimateCharges) {
        g.setColor(MystisColors.DARK_ORANGE)
        g.drawFilledRectangle(chargeX, y, chargeWidth, height, 0)
      }
      g.setColor(MystisColors.GOLD)
      g.drawRectangle(chargeX, y, chargeWidth, height, 0)
    }
    g.setColor(MystisColors.GOLD)
    g.drawString(15, y - 20, s"Ultimate: ${ultimateCharges}/${maxCharges}")
  }

  def drawLevelUpScreen(g: GdxGraphics, screenWidth: Float, screenHeight: Float): Unit = {
    if (!isLevelUpPaused || currentBonusCards.isEmpty) return

    g.setColor(new Color(0, 0, 0, 0.8f))
    g.drawFilledRectangle(0, 0, screenWidth, screenHeight, 0)

    g.setColor(Color.YELLOW)
    g.drawString(screenWidth / 2 - 100, screenHeight / 2 + 150, s"LEVEL ${currentLevel}!", 0)
    g.setColor(Color.WHITE)
    g.drawString(screenWidth / 2 - 120, screenHeight / 2 + 120, "Choose your bonus:", 0)

    val cardWidth = 300
    val cardHeight = 500f
    val spacing = 50f
    val startX = (screenWidth - (3 * cardWidth + 2 * spacing)) / 2

    for (i <- currentBonusCards.indices) {
      val card = currentBonusCards(i)
      val cardX = startX + i * (cardWidth + spacing)
      val cardY = screenHeight / 2 - cardHeight / 2

      g.setColor(Color.DARK_GRAY)
      g.drawFilledRectangle(cardX, cardY, cardWidth, cardHeight, 0)
      g.setColor(Color.WHITE)
      g.drawRectangle(cardX, cardY, cardWidth, cardHeight, 0)
      g.setColor(Color.YELLOW)
      g.drawString(cardX + 10, cardY + cardHeight - 20, s"${i + 1}")
      g.setColor(Color.WHITE)
      g.drawString(cardX + 10, cardY + cardHeight - 45, card.name)
      g.setColor(Color.LIGHT_GRAY)
      g.drawString(cardX + 10, cardY + cardHeight - 70, card.description)
      g.setColor(Color.CYAN)
      g.drawString(cardX + 10, cardY + 15, s"Press ${i + 1}")
    }
  }

  def useUltimate(): Boolean = {
    if (ultimateCharges > 0) {
      ultimateCharges -= 1
      println(s"ULTIMATE USED! Remaining charges: $ultimateCharges")
      return true
    } else {
      println("No ultimate charge available!")
      false
    }
  }
}

