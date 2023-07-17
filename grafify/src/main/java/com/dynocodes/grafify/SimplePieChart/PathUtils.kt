package com.dynocodes.grafify

import android.graphics.Path
import android.graphics.RectF
import com.dynocodes.grafify.MathUtils.getPointX
import com.dynocodes.grafify.MathUtils.getPointY


internal object PathUtils {
    fun getSolidArcPath(
        solidArcPath: Path,
        outerCircleBounds: RectF, innerCircleBounds: RectF,
        startAngle: Float, sweepAngle: Float
    ): Path {
        solidArcPath.reset()

        // Move to start point
        val startX = getPointX(
            innerCircleBounds.centerX(),
            innerCircleBounds.width() / 2f,
            startAngle
        )
        val startY = getPointY(
            innerCircleBounds.centerY(),
            innerCircleBounds.height() / 2f,
            startAngle
        )
        solidArcPath.moveTo(startX, startY)


        // Add inner hole arc
        solidArcPath.addArc(innerCircleBounds, startAngle, sweepAngle)


        // Line from inner to outer arc
        solidArcPath.lineTo(
            getPointX(
                outerCircleBounds.centerX(),
                outerCircleBounds.width() / 2f,
                startAngle + sweepAngle
            ),
            getPointY(
                outerCircleBounds.centerY(),
                outerCircleBounds.height() / 2f,
                startAngle + sweepAngle
            )
        )

        // Add outer arc
        solidArcPath.addArc(outerCircleBounds, startAngle + sweepAngle, -sweepAngle)

        // Close (drawing last line and connecting arcs)
        solidArcPath.lineTo(startX, startY)
//        solidArcPath.arcTo(RectF(), startAngle, 180f);


        return solidArcPath
    }
}