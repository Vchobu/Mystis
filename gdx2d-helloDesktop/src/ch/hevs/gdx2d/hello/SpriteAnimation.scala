package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.math.Vector2

class SpriteAnimation(
                       pathTemplate: String,
                       startFrame: Int,
                       endFrame: Int,
                       scale: Float,
                       frameRate: Float,
                       loop: Boolean
                     ) {

  private val frames: Array[BitmapImage] =
    (startFrame to endFrame).map(i => new BitmapImage(pathTemplate.format(i))).toArray

  private val mirroredFrames: Array[BitmapImage] =
    (startFrame to endFrame).map { i =>
      val img = new BitmapImage(pathTemplate.format(i))
      img.mirrorLeftRight()
      img
    }.toArray

  private val totalFrames = frames.length
  private var elapsedTime = 0f

  def updateAnimation(dt: Float): Unit = {
    elapsedTime += dt
  }

  def drawAnimation(g: GdxGraphics, position: Vector2, flipX: Boolean): Unit = {
    val index =
      if (loop) (elapsedTime * frameRate).toInt % totalFrames
      else Math.min((elapsedTime * frameRate).toInt, totalFrames - 1)

    val image = if (flipX) mirroredFrames(index) else frames(index)

    g.drawTransformedPicture(position.x, position.y, 0, scale, image)
  }

  def isFinished(): Boolean = {
    if (loop) false
    else elapsedTime >= totalFrames / frameRate
  }

  def reset(): Unit = {
    elapsedTime = 0f
  }

  def dispose(): Unit = {
    frames.foreach(_.dispose())
    mirroredFrames.foreach(_.dispose())
  }
}
