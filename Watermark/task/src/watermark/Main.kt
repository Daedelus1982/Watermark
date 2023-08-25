package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val image = getImage(isWatermark = false) ?: return
    val watermarkImage = getImage(isWatermark = true) ?: return
    if (image.width != watermarkImage.width || image.height != watermarkImage.height) {
        println("The image and watermark dimensions are different.")
        return
    }
    val weight = getWeight() ?: return
    println("Input the output image filename (jpg or png extension):")
    val outfile = readln()
    if (!(outfile.endsWith("jpg") || outfile.endsWith("png"))) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }
    val finalImage = getWatermarkedImage(image, watermarkImage, weight)
    val imageFile = File(outfile)
    ImageIO.write(finalImage, imageFile.extension, imageFile)
    println("The watermarked image $outfile has been created.")
}

fun getImage(isWatermark: Boolean): BufferedImage? {
    println("Input the ${if (isWatermark) "watermark " else ""}image filename:")
    val filename = readln()
    if (!File(filename).exists()) {
        println("The file $filename doesn't exist.")
        return null
    }
    val image = ImageIO.read(File(filename))
    val watermarkStr = if (isWatermark) "watermark" else "image"
    when {
        image.colorModel.numColorComponents != 3 -> {
            println("The number of $watermarkStr color components isn't 3.")
            return null
        }
        !(image.colorModel.pixelSize == 24 ||  image.colorModel.pixelSize == 32) -> {
            println("The $watermarkStr isn't 24 or 32-bit.")
            return null
        }
    }
    return image
}

fun getWeight(): Int? {
    println("Input the watermark transparency percentage (Integer 0-100):")
    return when (val num = readln().toIntOrNull()) {
        null -> {
            println("The transparency percentage isn't an integer number.")
            null
        }
        !in 0..100 -> {
            println("The transparency percentage is out of range.")
            null
        }
        else -> num
    }
}

fun getWatermarkedImage(image: BufferedImage, watermarkImage: BufferedImage, weight: Int): BufferedImage {
    val cmbImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val i = Color(image.getRGB(x, y))
            val w = Color(watermarkImage.getRGB(x, y))
            val color = Color(
                (weight * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            cmbImage.setRGB(x, y, color.rgb)
        }
    }
    return cmbImage
}