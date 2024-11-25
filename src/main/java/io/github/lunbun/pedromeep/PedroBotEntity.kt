package io.github.lunbun.pedromeep

import com.acmerobotics.roadrunner.geometry.Pose2d
import io.github.anyilin.pedropathing.localization.Pose
import io.github.anyilin.pedropathing.path.PathBuilder
import io.github.anyilin.pedropathing.path.PathChain
import io.github.anyilin.pedropathing.tuning.FollowerConstants
import org.rowlandhall.meepmeep.MeepMeep
import org.rowlandhall.meepmeep.core.anim.Ease
import org.rowlandhall.meepmeep.core.colorscheme.ColorScheme
import org.rowlandhall.meepmeep.core.entity.BotEntity
import org.rowlandhall.meepmeep.core.entity.EntityEventListener

class PedroBotBuilder(private val meepMeep: MeepMeep) {
    /** The constants for the bot's trajectory. */
    private var constants = FollowerConstants()

    /** The width of the bot. */
    private var width = 18.0

    /** The height of the bot. */
    private var height = 18.0

    /** The starting pose of the bot. */
    private var startPose = Pose()

    /** The color scheme of the bot. */
    private var colorScheme: ColorScheme? = null

    /** The opacity of the bot. */
    private var opacity = 0.8

    /** The time per path of the bot. */
    private var timePerPath = 2.0

    /**
     * Sets the dimensions of the bot.
     *
     * @param width The width of the bot.
     * @param height The height of the bot.
     * @return The current instance of [PedroBotBuilder] for chaining.
     */
    fun setDimensions(width: Double, height: Double): PedroBotBuilder {
        this.width = width
        this.height = height
        return this
    }

    /**
     * Sets the starting pose of the bot.
     *
     * @param pose The [Pose] object representing the starting pose.
     * @return The current instance of [PedroBotBuilder] for chaining.
     */
    fun setStartPose(pose: Pose): PedroBotBuilder {
        this.startPose = pose
        return this
    }

    /**
     * Sets the constants for the bot's trajectory.
     *
     * @param constants The [FollowerConstants] object containing the trajectory
     *    constants.
     * @return The current instance of [PedroBotBuilder] for chaining.
     */
    fun setConstants(constants: FollowerConstants): PedroBotBuilder {
        this.constants = constants
        return this
    }

    /**
     * Sets the color scheme of the bot.
     *
     * @param scheme The [ColorScheme] to set for the bot.
     * @return The current instance of [PedroBotBuilder] for chaining.
     */
    fun setColorScheme(scheme: ColorScheme): PedroBotBuilder {
        this.colorScheme = scheme
        return this
    }

    /**
     * Sets the time per path of the bot.
     *
     * @param time The time per path of the bot.
     * @return The current instance of [PedroBotBuilder] for chaining.
     */
    fun setTimePerPath(time: Double): PedroBotBuilder {
        this.timePerPath = time
        return this
    }

    /**
     * Builds a new instance of [PedroBotEntity] using the current
     * configuration of the [PedroBotBuilder].
     *
     * @return A new [PedroBotEntity] instance.
     */
    fun build() = PedroBotEntity(
        meepMeep,
        constants,
        width,
        height,
        startPose,
        colorScheme ?: meepMeep.colorManager.theme,
        opacity,
        timePerPath
    )
}

class PedroBotEntity(
    meepMeep: MeepMeep,
    private val constants: FollowerConstants,
    width: Double,
    height: Double,
    pose: Pose,
    private val colorScheme: ColorScheme,
    opacity: Double,
    private val timePerPath: Double
) : BotEntity(meepMeep, width, height, Pose2d(pose.x, pose.y, pose.heading), colorScheme, opacity),
    EntityEventListener {
    /** Tag for the bot entity. */
    override val tag = "RR_BOT_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    companion object {
        /** Number of loops to skip initially to avoid startup issues. */
        const val SKIP_LOOPS = 2
    }

    /** The current path chain the bot is following. */
    var currentPathChain: PathChain? = null

    /** Entity representing the path chain. */
    var pathChainEntity: PathChainEntity? = null

    /** Flag indicating if the bot should loop the trajectory sequence. */
    var looping = true

    /** Flag indicating if the bot is currently running. */
    private var isExecutingPath = false

    /** Elapsed time for the current path chain. */
    private var pathChainElapsedTime = 0.0

    /** Flag indicating if the path chain is paused. */
    var isPathPaused = false

    /** Counter for the number of skipped loops. */
    private var skippedLoops = 0

    override fun update(deltaTime: Long) {
        if (!isExecutingPath || currentPathChain == null) return

        // Skip initial loops to avoid startup issues
        if (skippedLoops++ < SKIP_LOOPS) return

        if (!isPathPaused) pathChainElapsedTime += deltaTime / 1e9

        val index = (pathChainElapsedTime / timePerPath).toInt()
        if (index < 0 || index >= currentPathChain!!.size()) {
            pathChainElapsedTime = 0.0
            if (looping) {
                // Reset markers and elapsed time for looping
            } else {
                // Stop running when the sequence is done
                isExecutingPath = false
            }
            return
        }

        val path = currentPathChain!!.getPath(index)
        pathChainEntity!!.updateCurrentPath(path)

        val t = Ease.EASE_IN_OUT_CUBIC((pathChainElapsedTime % timePerPath) / timePerPath)
        val position = path!!.getPoint(t)
        val heading = path.getHeadingGoal(t)
        pose = Pose2d(position.x, position.y, heading)
    }

    fun pathBuilder() = PathBuilder(constants)

    fun followPath(pathChain: PathChain) {
        currentPathChain = pathChain
        pathChainEntity = PathChainEntity(meepMeep, pathChain, colorScheme)

        isExecutingPath = true
        pathChainElapsedTime = 0.0
    }

    /** Called when the bot is added to the entity list. */
    override fun onAddToEntityList() {
        pathChainEntity?.let { meepMeep.requestToAddEntity(it) }
    }

    /** Called when the bot is removed from the entity list. */
    override fun onRemoveFromEntityList() {
        pathChainEntity?.let { meepMeep.requestToRemoveEntity(it) }
    }
}
