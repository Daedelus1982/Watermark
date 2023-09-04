package watermark

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val image = getImage(isWatermark = false) ?: return
    val watermarkImage = getImage(isWatermark = true) ?: return
    if (image.width < watermarkImage.width || image.height < watermarkImage.height) {
        println("The watermark's dimensions are larger.")
        return
    }
    val transparencyColor = if (!watermarkImage.colorModel.hasAlpha()) {
        println("Do you want to set a transparency color?")
        if (readln() == "yes") {
            watermarkTransparencyColor() ?: return
        } else null
    } else null
    val useAlpha = if (watermarkImage.colorModel.hasAlpha()) {
        println("Do you want to use the watermark's Alpha channel?")
        readln() == "yes"
    } else false
    val weight = getWeight() ?: return
    println("Choose the position method (single, grid):")
    val pos = readln()
    if (!(pos == "single" || pos == "grid")) {
        println("The position method input is invalid.")
        return
    }
    val point = if (pos == "single") {
        getWatermarkPosition(image, watermarkImage) ?: return
    } else Point(0, 0)
    println("Input the output image filename (jpg or png extension):")
    val outfile = readln()
    if (!(outfile.endsWith("jpg") || outfile.endsWith("png"))) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }
    val finalImage = if (pos == "single") {
        getWatermarkedImage(image, watermarkImage, weight, useAlpha, transparencyColor, point)
    } else {
        val xs = (0..image.width step watermarkImage.width).toList()
        val ys = (0..image.height step watermarkImage.height).toList()

        var wmi = image
        ys.forEach {yit ->
            xs.forEach {xit ->
                wmi = getWatermarkedImage(wmi, watermarkImage, weight, useAlpha, transparencyColor, Point(xit, yit))
            }
        }
        wmi
    }
    val imageFile = File(outfile)
    ImageIO.write(finalImage, imageFile.extension, imageFile)
    println("The watermarked image $outfile has been created.")
}

fun getWatermarkPosition(image: BufferedImage, watermarkImage: BufferedImage): Point? {
    println("Input the watermark position " +
            "([x 0-${image.width - watermarkImage.width}] [y 0-${image.height - watermarkImage.height}]):")
    try {
        val (x, y) = readln().split(" ").map { it.toInt() }
        if (!(x in 0..(image.width - watermarkImage.width) && y in 0.. (image.height-watermarkImage.height))) {
            println("The position input is out of range.")
            return null
        }
        return Point(x, y)
    } catch (e: Exception) {
        println("The position input is invalid.")
        return null
    }
}

fun watermarkTransparencyColor(): Color? {
    println("Input a transparency color ([Red] [Green] [Blue]):")
    return try {
        val rgb = readln().split(" ").map { it.toInt() }
        if (rgb.count() != 3) throw IllegalStateException("wrong number of arguments")
        rgb.forEach {if (it !in 0..255) throw IllegalStateException("values not in range")}
        Color(rgb[0], rgb[1], rgb[2])
    } catch(ex: Exception) {
        println("The transparency color input is invalid.")
        null
    }
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

fun getWatermarkedImage(image: BufferedImage, watermarkImage: BufferedImage,
                        weight: Int, useAlpha: Boolean, transparencyColor: Color?, point: Point): BufferedImage {
    val cmbImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val i = Color(image.getRGB(x, y))
            val w = if ((x >= point.x && x < point.x + watermarkImage.width) &&
                (y >= point.y && y < point.y + watermarkImage.height)) {
                Color(watermarkImage.getRGB(x - point.x, y - point.y), useAlpha)
            } else i
            val color = if (w.alpha == 0 || w == transparencyColor) i else Color(
                (weight * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            cmbImage.setRGB(x, y, color.rgb)
        }
    }
    return cmbImage
}