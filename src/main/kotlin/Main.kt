import org.openrndr.MouseButton
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        val font = loadFont("data/fonts/default.otf", 64.0)
        mouse.buttonDown.listen {
            if(it.button == MouseButton.LEFT)
                boids.add(Boid(it.position))
            if(it.button == MouseButton.RIGHT)
                avoids.add(Avoid(it.position))
        }
        var previousUpdate = 0.0
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
                boid.Draw(drawer, deltaTime)
            }
            for(avoid in avoids){
                drawer.fill = ColorRGBa.BLUE
                drawer.circle(avoid.position, size)
            }

        }
    }
}
