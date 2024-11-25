package io.github.lunbun.pedromeep

import com.acmerobotics.roadrunner.geometry.Vector2d
import io.github.anyilin.pedropathing.path.Path
import io.github.anyilin.pedropathing.path.PathChain
import org.rowlandhall.meepmeep.MeepMeep
import org.rowlandhall.meepmeep.core.colorscheme.ColorScheme
import org.rowlandhall.meepmeep.core.entity.ThemedEntity
import org.rowlandhall.meepmeep.core.toScreenCoord
import org.rowlandhall.meepmeep.core.util.FieldUtil
import org.rowlandhall.meepmeep.roadrunner.entity.TrajectorySequenceEntity.Companion.PATH_INNER_STROKE_WIDTH
import org.rowlandhall.meepmeep.roadrunner.entity.TrajectorySequenceEntity.Companion.PATH_OUTER_OPACITY
import org.rowlandhall.meepmeep.roadrunner.entity.TrajectorySequenceEntity.Companion.PATH_OUTER_STROKE_WIDTH
import org.rowlandhall.meepmeep.roadrunner.entity.TrajectorySequenceEntity.Companion.PATH_UNFOCUSED_OPACITY
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage

class PathChainEntity(
    override val meepMeep: MeepMeep,
    private val pathChain: PathChain,
    private var colorScheme: ColorScheme
) :
    ThemedEntity {
    /** Tag for the trajectory sequence entity. */
    override val tag = "TRAJECTORY_SEQUENCE_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** Buffered image for rendering the trajectory sequence. */
    private lateinit var baseBufferedImage: BufferedImage

    /** Buffered image for rendering the current segment. */
    private var currentSegmentImage: BufferedImage? = null

    /** Current path of the path chain. */
    private var currentPath: Path? = null

    /** Initializes the trajectory sequence entity and draws the path. */
    init {
        redrawPath()
    }

    fun updateCurrentPath(path: Path?) {
        if (path != currentPath) {
            currentPath = path
            redrawCurrentSegment()
        }
    }

    private fun drawPath(path: Path, drawnPath: Path2D.Double) {
        val start = path.getPoint(0.0)
        val screenStart = Vector2d(start.x, start.y).toScreenCoord()
        drawnPath.moveTo(screenStart.x, screenStart.y)

        for (i in 0..100) {
            val point = path.getPoint(i / 100.0)
            val screenPoint = Vector2d(point.x, point.y).toScreenCoord()
            drawnPath.lineTo(screenPoint.x, screenPoint.y)
        }
    }

    /** Redraws the entire trajectory path. */
    private fun redrawPath() {
        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the trajectory sequence
        baseBufferedImage =
            config.createCompatibleImage(
                canvasWidth.toInt(),
                canvasHeight.toInt(),
                Transparency.TRANSLUCENT,
            )
        val gfx = baseBufferedImage.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Create a path for the trajectory sequence
        val drawnPath = Path2D.Double()
        for (i in 0..<pathChain.size()) {
            drawPath(pathChain.getPath(i), drawnPath)
        }

        // Create strokes for the inner path
        val innerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )

        gfx.stroke = innerStroke
        gfx.color = colorScheme.trajectoryPathColor
        gfx.color =
            Color(
                colorScheme.trajectoryPathColor.red,
                colorScheme.trajectoryPathColor.green,
                colorScheme.trajectoryPathColor.blue,
                (PATH_UNFOCUSED_OPACITY * 255).toInt(),
            )
        gfx.draw(drawnPath)
    }

    /** Redraws the current segment of the trajectory. */
    private fun redrawCurrentSegment() {
        // If there is no current segment, clear the current segment image and return
        if (currentPath == null) {
            currentSegmentImage = null
            return
        }

        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the current segment
        currentSegmentImage =
            config.createCompatibleImage(
                canvasWidth.toInt(),
                canvasHeight.toInt(),
                Transparency.TRANSLUCENT,
            )
        val gfx = currentSegmentImage!!.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Create a path for the trajectory segment
        val drawnPath = Path2D.Double()
        drawPath(currentPath!!, drawnPath)

        // Create stroke for the outer and inner paths
        val outerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )
        val innerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )

        // Draw the outer path with the specified opacity and color
        gfx.stroke = outerStroke
        gfx.color =
            Color(
                colorScheme.trajectoryPathColor.red,
                colorScheme.trajectoryPathColor.green,
                colorScheme.trajectoryPathColor.blue,
                (PATH_OUTER_OPACITY * 255).toInt(),
            )
        gfx.draw(drawnPath)

        // Draw the inner path with the full color
        gfx.stroke = innerStroke
        gfx.color = colorScheme.trajectoryPathColor
        gfx.draw(drawnPath)
    }

    override fun update(deltaTime: Long) { }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        // Draw the base buffered image
        gfx.drawImage(baseBufferedImage, null, 0, 0)

        // Draw the current segment image if it exists
        if (currentSegmentImage != null) gfx.drawImage(currentSegmentImage, null, 0, 0)
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        // Check if the canvas dimensions have changed
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) {
            // Redraw the path if the dimensions have changed
            redrawPath()
        }
        // Update the canvas dimensions
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        // Check if the new color scheme is different from the current one
        if (this.colorScheme != scheme) {
            // Update the color scheme
            this.colorScheme = scheme
            // Redraw the path with the new color scheme
            redrawPath()
        }
    }
}
