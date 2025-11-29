import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.transforms.transform
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val size: Double = 10.0
const val speed: Double = 120.0
const val groupRadius: Double = size * 10
const val seperationRadius: Double = size * 6
const val avoidFactor: Double = 0.05
const val matchingFactor: Double = 0.05
const val centeringFactor: Double = 0.0005

class Boid(_pos: Vector2) {
    var position: Vector2 = _pos
    var rotation: Double = Random.nextDouble(0.0, 360.0)
    var friendCount: Int = 0
    var currentColor = ColorRGBa.RED

    fun Draw(drawer: Drawer){
        drawer.fill = currentColor
        drawer.contour(CreateRectangle().transform(transform {
            translate(position)
            rotate(rotation)
        }))
    }

    fun CollectFlock(
        visited: MutableSet<Boid>,
        neighborsCache: Map<Boid, List<Boid>>
    ) {
        val friends = neighborsCache[this] ?: emptyList()

        for (boid in friends) {
            if (boid !in visited) {
                visited.add(boid)
                boid.CollectFlock(visited, neighborsCache)
            }
        }
    }

    fun GetFlock(): Set<Boid> {
        val neighborsCache = boids.associateWith { it.GetFriends(groupRadius) }

        val visited = mutableSetOf<Boid>()
        visited.add(this)

        this.CollectFlock(visited, neighborsCache)

        return visited
    }

    fun GroupColor(): ColorRGBa {
        var transition = GetFlock().size / 50.0
        transition = transition.clamp(0.0, 1.0)
        return ColorHSVa(transition * 360, 1.0, 1.0).toRGBa()
    }

    fun CreateRectangle(): ShapeContour{
        return contour {
            moveTo(Vector2(-size, size / 1.1))
            lineTo(Vector2(size, 0.0))
            lineTo(Vector2(-size, -size / 1.1))
            close()
        }
    }

    fun Update(deltaTime: Double){
        val color = GroupColor()
        val red = lerp(currentColor.r, color.r, deltaTime * 2)
        val green = lerp(currentColor.g, color.g, deltaTime * 2)
        val blue = lerp(currentColor.b, color.b, deltaTime * 2)
        currentColor = ColorRGBa(red, green, blue)
        friendCount = GetFriends(groupRadius).size
        val angle = Math.toRadians(rotation)
        var dir = Vector2(cos(angle), sin(angle))
        dir = Seperation(dir)
        dir = Alignment(dir)
        dir = Cohesion(dir)
        //dir = RunFromEdge(dir)
        val newPos = position + (dir * speed * deltaTime)
        position = newPos
        Wraparound()
        val rot = -atan2(-dir.y, dir.x)
        if(!rot.isNaN())
            rotation = rot * (180 / PI)
    }

    fun Seperation(dir: Vector2): Vector2{
        val friends = GetFriends(seperationRadius)
        var diff = Vector2.ZERO
        for(boid in friends){
            if(boid == this) continue
            val boidDiff = position - boid.position
            diff += boidDiff
        }
        return dir + SeperationAvoid(diff.normalized * avoidFactor)
    }

    fun SeperationAvoid(dir: Vector2): Vector2{
        val friends = GetAvoids(seperationRadius)
        var diff = Vector2.ZERO
        for(avoid in friends){
            val boidDiff = position - avoid.position
            diff += boidDiff
        }
        return dir + (diff.normalized * (avoidFactor * 3))
    }

    fun Alignment(dir: Vector2): Vector2{
        val friends = GetFriends(groupRadius)
        if(friends.isEmpty()) return dir
        var avg_angle = 0.0
        for(boid in friends){
            avg_angle += boid.rotation
        }
        avg_angle = avg_angle / friends.size
        val angle = Math.toRadians(avg_angle)
        val angleDir = Vector2(cos(angle), sin(angle))
        val newDir = Vector2(lerp(dir.x, angleDir.x, matchingFactor), lerp(dir.y, angleDir.y, matchingFactor))
        return newDir
    }

    fun Cohesion(dir: Vector2): Vector2 {
        val friends = GetFriends(groupRadius)
        if(friends.isEmpty()) return dir
        var center = Vector2.ZERO
        for(boid in friends){
            center += boid.position
        }
        center = Vector2(center.x / friends.size, center.y / friends.size)
        val newDir = (center - position) * centeringFactor
        return dir + newDir
    }

    fun Wraparound(){
        position = Vector2(position.x % (screenWidth + size), position.y % (screenHeight + size))
        if(position.x < -size)
            position = Vector2(position.x + screenWidth, position.y)
        if(position.y < -size)
            position = Vector2(position.x, position.y + screenHeight)
    }

    fun GetFriends(radius: Double): List<Boid> {
        val friends = mutableListOf<Boid>()
        for(boid in boids){
            if(boid == this) continue
            if(position.distanceTo(boid.position) < radius){
                friends.add(boid)
            }
        }
        return friends
    }


    fun GetAvoids(radius: Double): List<Avoid> {
        val friends = mutableListOf<Avoid>()
        for(avoid in avoids){
            if(position.distanceTo(avoid.position) < radius){
                friends.add(avoid)
            }
        }
        return friends
    }
}