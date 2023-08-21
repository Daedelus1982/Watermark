package watermark

import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Input the image filename:")
    val filename = readln()
    if (File(filename).exists()) {
        val image = ImageIO.read(File(filename))
        val transparency = when (image.transparency) {
            1 -> "OPAQUE"
            3 -> "TRANSLUCENT"
            else -> "BITMASK"
        }
        println("Image file: $filename")
        println("Width: ${image.width}")
        println("Height: ${image.height}")
        println("Number of components: ${image.colorModel.numComponents}")
        println("Number of color components: ${image.colorModel.numColorComponents}")
        println("Bits per pixel: ${image.colorModel.pixelSize}")
        println("Transparency: $transparency")
    }
    else {
        println("The file $filename doesn't exist.")
    }
}