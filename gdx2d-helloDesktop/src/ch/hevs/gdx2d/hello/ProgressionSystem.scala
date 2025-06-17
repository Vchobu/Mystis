package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}

/**
 * Carte de bonus représentant une amélioration disponible lors d'un level up.
 * Chaque carte contient un nom, un type d'effet, et une valeur numérique.
 * 
 * @param name Nom descriptif de l'amélioration
 * @param bonusType Type d'effet (damage, speed, health, crit, regen, etc.)
 * @param value Valeur numérique de l'amélioration
 */
case class BonusCard(name: String, bonusType: String, value: Float) {
  /**
   * Génère une description formatée de l'effet du bonus.
   * Convertit les valeurs en pourcentages ou points selon le type.
   */
  def description: String = bonusType match {
    case "damage" => s"+${(value * 100).toInt}% damage"
    case "speed" => s"+${(value * 100).toInt}% attack speed"
    case "health" => s"+${value.toInt} max health"
    case "crit" => s"+${(value * 100).toInt}% critical chance"
    case "regen" => s"+${value}/s health regen"
    case "range" => s"+${(value * 100).toInt}% range"
    case "projectileSpeed" => s"+${(value * 100).toInt}% projectile speed"
    case "movementSpeed" => s"+${(value * 100).toInt}% movement speed"
    case "multiShot" => "Double shot enabled"
    case _ => "Mysterious bonus"
  }
}

/**
 * Système de progression du joueur gérant l'expérience, les niveaux, et les améliorations.
 * 
 * Fonctionnalités principales:
 * - Calcul automatique de l'XP et des level ups
 * - Système de cartes de bonus à choisir lors des montées de niveau
 * - Gestion des charges d'ultimate (maximum 4)
 * - Interface utilisateur pour les barres de progression
 * - Écran de sélection des bonus avec pause du jeu
 * - Application automatique des améliorations sélectionnées
 * 
 * Le système suit une progression exponentielle avec un multiplicateur de 1.5x
 * pour l'XP requise à chaque niveau.
 */
class ProgressionSystem {
  private var currentLevel: Int = 1
  private var currentXP: Float = 0.0f
  private var xpToNextLevel: Float = 100.0f
  private val xpMultiplier: Float = 1.5f
  private var ultimateCharges: Int = math.min(1, 0)

  var isLevelUpPaused: Boolean = false
  var currentBonusCards: List[BonusCard] = List.empty

  private val enemyXPValues = Map(
    "basicEnemy" -> 5.0f,   // Enemy type 1 = 5xp
    "eliteEnemy" -> 10.0f,  // Enemy type 2 = 10xp
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
    ultimateCharges = math.min(ultimateCharges + 1, 4) // Max 4 charges
    xpToNextLevel = (xpToNextLevel * xpMultiplier).toFloat
    
    // Double enemy count each level (but cap at reasonable limit)
    GameSettings.enemyLimit = math.min(GameSettings.enemyLimit * 2, 200) // Max 200 enemies
    println(s"Level up! New level: $currentLevel, New enemy limit: ${GameSettings.enemyLimit}")
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
      BonusCard("Attack Speed +15%", "speed", 0.15f),
      BonusCard("Max Health +20", "health", 20.0f),
      BonusCard("Critical Chance +5%", "crit", 0.05f),
      BonusCard("Health Regen +2/s", "regen", 2.0f),
      BonusCard("Range +20%", "range", 0.2f),
      BonusCard("Projectile Speed +25%", "projectileSpeed", 0.25f),
      BonusCard("Movement Speed +15%", "movementSpeed", 0.15f),
      BonusCard("Double Shot", "multiShot", 1.0f)
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
        GameSettings.criticalChance += card.value
        println(s"Critical chance increased by ${(card.value * 100).toInt}%! New crit: ${(GameSettings.criticalChance * 100).toInt}%")
      case "regen" =>
        GameSettings.healthRegenRate += card.value
        println(s"Regeneration increased by ${card.value}/s! New regen: ${GameSettings.healthRegenRate}/s")
      case "range" =>
        GameSettings.projectileShootRange *= (1 + card.value)
        println(s"Range increased by ${(card.value * 100).toInt}%! New range: ${GameSettings.projectileShootRange}")
      case "projectileSpeed" =>
        GameSettings.projectileSpeed *= (1 + card.value)
        println(s"Projectile speed increased by ${(card.value * 100).toInt}%! New speed: ${GameSettings.projectileSpeed}")
      case "movementSpeed" =>
        GameSettings.playerSpeed *= (1 + card.value)
        println(s"Movement speed increased by ${(card.value * 100).toInt}%! New speed: ${GameSettings.playerSpeed}")
      case "multiShot" =>
        GameSettings.multiShotEnabled = true
        println("Double shot unlocked!")
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
    drawHealthBar(g, 15, screenHeight - 40, 200, 20, playerCurrentHP, playerMaxHP)
    drawProgressBar(g, 15, screenHeight - 100, 200, 20)
    drawUltimateBar(g, 15, screenHeight - 180, 200, 20)
  }

  def drawHealthBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float,
                    currentHP: Float, maxHP: Float): Unit = {
    val healthPercentage = if (maxHP > 0) currentHP / maxHP else 0f

    // Background bar (centered)
    g.setColor(MystisColors.MYSTIS_BROWN)
    g.drawFilledRectangle(x + width/2, y, width, height, 0)

    // Filled health bar (from left to right)
    if (healthPercentage > 0) {
      g.setColor(MystisColors.RED)
      val filledWidth = width * healthPercentage
      g.drawFilledRectangle(x + filledWidth/2, y, filledWidth, height, 0)
    }

    // Border (centered)
    g.setColor(MystisColors.GOLD)
    g.drawRectangle(x + width/2, y, width, height, 0)
    g.drawString(15, y - 20, s"Health: ${currentHP.toInt}/${maxHP.toInt}")
  }

  def drawProgressBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float): Unit = {
    val progress = getProgressPercentage

    // Background bar (centered)
    g.setColor(MystisColors.MYSTIS_BROWN)
    g.drawFilledRectangle(x + width/2, y, width, height, 0)

    // Filled XP bar (from left to right)
    if (progress > 0) {
      g.setColor(MystisColors.GREEN)
      val filledWidth = width * progress
      g.drawFilledRectangle(x + filledWidth/2, y, filledWidth, height, 0)
    }

    // Border (centered)
    g.setColor(MystisColors.GOLD)
    g.drawRectangle(x + width/2, y, width, height, 0)
    g.drawString(15, y - 20, s"Level: $currentLevel")
    g.drawString(15, y - 40, s"XP: ${currentXP.toInt}/${xpToNextLevel.toInt}")
  }

  def drawUltimateBar(g: GdxGraphics, x: Float, y: Float, width: Float, height: Float): Unit = {
    val maxCharges = 4
    val chargeWidth = width / maxCharges
    for (i <- 0 until maxCharges) {
      val chargeX = x + i * chargeWidth + chargeWidth/2 // Align with other bars
      g.setColor(MystisColors.MYSTIS_BROWN)
      g.drawFilledRectangle(chargeX, y, chargeWidth, height, 0)
      if (i < ultimateCharges) {
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

    // Draw semi-transparent background overlay covering entire screen
    g.setColor(MystisColors.MYSTIS_BROWN)
    g.drawFilledRectangle(screenWidth/2, screenHeight/2, screenWidth, screenHeight, 0)

    g.setColor(Color.YELLOW)
    g.drawString(screenWidth / 2 - 100, screenHeight / 2 + 150, s"LEVEL ${currentLevel}!", 0)
    g.setColor(Color.WHITE)
    g.drawString(screenWidth / 2 - 120, screenHeight / 2 + 120, "Choose your bonus:", 0)

    val cardWidth = 300
    val cardHeight = 100
    val spacing = 50f
    val totalWidth = 3 * cardWidth + 2 * spacing
    val startX = (screenWidth - totalWidth) / 2

    for (i <- currentBonusCards.indices) {
      val card = currentBonusCards(i)
      val cardX = startX + i * (cardWidth + spacing) + cardWidth/2 // Center X position
      val cardY = screenHeight / 2 // Center Y position

      // Draw filled card background (centered)
      g.setColor(MystisColors.DARK)
      g.drawFilledRectangle(cardX, cardY, cardWidth, cardHeight, 0)
      
      // Draw card border (centered) 
      g.setColor(Color.WHITE)
      g.drawRectangle(cardX, cardY, cardWidth, cardHeight, 0)
      // Calculate text positions relative to centered card
      val textX = cardX - cardWidth/2 + 10 // Left edge of card + margin
      val textTopY = cardY + cardHeight/2 - 20 // Top area of card
      val textMiddleY = cardY + cardHeight/2 - 45 // Middle area of card
      val textLowerY = cardY + cardHeight/2 - 70 // Lower area of card
      val textBottomY = cardY - cardHeight/2 + 15 // Bottom area of card
      
      // Draw card number
      g.setColor(Color.YELLOW)
      g.drawString(textX, textTopY, s"${i + 1}")
      
      // Draw card name
      g.setColor(Color.WHITE)
      g.drawString(textX, textMiddleY, card.name)
      
      // Draw card description
      g.setColor(Color.LIGHT_GRAY)
      g.drawString(textX, textLowerY, card.description)
      
      // Draw instruction
      g.setColor(Color.CYAN)
      g.drawString(textX, textBottomY, s"Press ${i + 1}")
    }
  }

  def useUltimate(): Boolean = {
    if (ultimateCharges > 0) {
      ultimateCharges -= 1
      println(s"ULTIMATE USED! Remaining charges: $ultimateCharges")
      true
    } else {
      println("No ultimate charge available!")
      false
    }
  }
}

