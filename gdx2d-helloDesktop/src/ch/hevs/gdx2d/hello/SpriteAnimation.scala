package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2

/**
 * Sprite animation system for handling frame-based animations with support for:
 * - Automatic frame sequencing based on time
 * - Horizontal mirroring for directional sprites
 * - Looping and non-looping animations
 * - Scalable rendering
 * 
 * Used extensively for character animations (idle, walk, death) and projectile effects.
 * 
 * @param pathTemplate String template with %d placeholder for frame numbers (e.g., "sprites/walk_%d.png")
 * @param startFrame First frame number in the sequence
 * @param endFrame Last frame number in the sequence
 * @param scale Scaling factor for sprite rendering
 * @param frameRate Animation speed in frames per second
 * @param loop Whether animation should repeat infinitely or play once
 */
class SpriteAnimation(
                       pathTemplate: String,
                       startFrame: Int,
                       endFrame: Int,
                       scale: Float,
                       frameRate: Float,
                       loop: Boolean
                     ) {

  // Load all frames of the animation sequence from files
  private val frames: Array[BitmapImage] =
    (startFrame to endFrame).map(i => new BitmapImage(pathTemplate.format(i))).toArray

  // Pre-create horizontally mirrored versions for left-facing animations
  private val mirroredFrames: Array[BitmapImage] =
    (startFrame to endFrame).map { i =>
      val img = new BitmapImage(pathTemplate.format(i))
      img.mirrorLeftRight() // Flip horizontally for directional sprites
      img
    }.toArray

  private val totalFrames = frames.length
  private var elapsedTime = 0f // Tracks animation progress

  /**
   * Update animation timer to advance through frames.
   * Must be called every frame for smooth animation playback.
   * 
   * @param dt Delta time since last frame update
   */
  def updateAnimation(dt: Float): Unit = {
    elapsedTime += dt
  }

  /**
   * Render the current animation frame at the specified position.
   * Automatically selects the correct frame based on elapsed time and frame rate.
   * 
   * @param g Graphics context for rendering
   * @param position World position to draw the sprite
   * @param flipX Whether to use horizontally mirrored frames (for left-facing)
   */
  def drawAnimation(g: GdxGraphics, position: Vector2, flipX: Boolean): Unit = {
    // Calculate which frame to display based on elapsed time
    val index =
      if (loop) (elapsedTime * frameRate).toInt % totalFrames // Loop back to start
      else Math.min((elapsedTime * frameRate).toInt, totalFrames - 1) // Stop at last frame

    // Select normal or mirrored frame based on direction
    val image = if (flipX) mirroredFrames(index) else frames(index)

    // Render the sprite at the specified position with scaling
    g.drawTransformedPicture(position.x, position.y, 0, scale, image)
  }

  /**
   * Check if a non-looping animation has completed all frames.
   * Used for death animations, hit effects, etc. that should play once.
   * 
   * @return true if animation is finished, false if still playing or looping
   */
  def isFinished(): Boolean = {
    if (loop) false // Looping animations never finish
    else elapsedTime >= totalFrames / frameRate // Check if reached end
  }

  /**
   * Reset animation to the beginning.
   * Useful for restarting animations or changing states.
   */
  def reset(): Unit = {
    elapsedTime = 0f
  }

  /**
   * Clean up memory by disposing of all loaded image resources.
   * Should be called when animation is no longer needed.
   */
  def dispose(): Unit = {
    frames.foreach(_.dispose()) // Dispose normal frames
    mirroredFrames.foreach(_.dispose()) // Dispose mirrored frames
  }
}
