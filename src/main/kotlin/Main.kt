import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.MouseButton
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.shape.Circle
import org.openrndr.shape.contour

val screenWidth = 1280
val screenHeight = 720

var boids = mutableListOf<Boid>()
var avoids = mutableListOf<Avoid>()

fun main() = application {
    configure {
        width = screenWidth
        height = screenHeight
    }

    program {
        mouse.buttonDown.listen {
            if(it.button == MouseButton.LEFT)
                boids.add(Boid(it.position))
            if(it.button == MouseButton.RIGHT)
                avoids.add(Avoid(it.position))
        }

        keyboard.keyDown.listen {
            if(it.key == KEY_SPACEBAR){
                val c1 = Circle(width.toDouble() / 2, height.toDouble() / 2, 200.0).contour
                val points = c1.equidistantPositions(150)
                for(point in points) avoids.add(Avoid(point))
            }
            if(it.key == KEY_ESCAPE){
                avoids.clear()
            }
            if(it.key == KEY_ENTER){
                val c1 = contour {
                    moveTo(0.0, 0.0)
                    lineTo(width.toDouble(), 0.0)
                }
                val c2 = contour {
                    moveTo(0.0, height.toDouble())
                    lineTo(width.toDouble(), height.toDouble())
                }
                val points = c1.equidistantPositions(150) + c2.equidistantPositions(150)
                for(point in points) avoids.add(Avoid(point))
            }
        }
        var previousUpdate: Double
        var currentUpdate = 0.0
        extend {
            previousUpdate = currentUpdate
            currentUpdate = seconds

            val deltaTime = currentUpdate - previousUpdate

            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))

            drawer.stroke = null

            //drawer.fill = ColorRGBa.PINK
            //drawer.circle(sin(seconds * 100) * width / 2.0 + width / 2.0, sin(0.5 * seconds) * height / 2.0 + height / 2.0, 140.0)
            for(boid in boids){
                boid.Update(deltaTime)
                boid.Draw(drawer)
            }
            for(avoid in avoids){
                drawer.fill = ColorRGBa.BLUE
                drawer.circle(avoid.position, size)
            }

        }
    }
}
