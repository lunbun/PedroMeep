package io.github.lunbun.pedromeep

import io.github.anyilin.pedropathing.path.Point
import org.rowlandhall.meepmeep.MeepMeep

object PedroMeepExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val meepMeep = MeepMeep(800)

        val robot = PedroBotBuilder(meepMeep).build()

        val builder = robot.pathBuilder()
        builder
            .addBezierCurve(
                Point(7.701 - 72.0, 85.476 - 72.0, Point.CARTESIAN),
                Point(31.572 - 72.0, 120.590 - 72.0, Point.CARTESIAN),
                Point(75.465 - 72.0, 8.317 - 72.0, Point.CARTESIAN),
                Point(3.465 - 72.0, 87.317 - 72.0, Point.CARTESIAN),
                Point(57.138 - 72.0, 97.335 - 72.0, Point.CARTESIAN)
            )
            .setTangentHeadingInterpolation()
            .addBezierLine(
                Point(57.138 - 72.0, 97.335 - 72.0, Point.CARTESIAN),
                Point(102.263 - 72.0, 19.713 - 72.0, Point.CARTESIAN)
            )
            .setTangentHeadingInterpolation()
        val pathChain = builder.build()

        robot.followPath(pathChain)

        meepMeep.setBackground(MeepMeep.Background.FIELD_INTOTHEDEEP_JUICE_DARK).setDarkMode(true)
            .setBackgroundAlpha(0.95f).addEntity(robot).start()
    }
}
