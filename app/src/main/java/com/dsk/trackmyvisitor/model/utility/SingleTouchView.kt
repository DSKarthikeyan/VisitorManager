package com.dsk.trackmyvisitor.model.utility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


/**
 * @author Karthikeyan D S Create the new view in the path and
 */
class SingleTouchView(
    context: Context?,
    attrs: AttributeSet?
) :
    View(context, attrs) {
    private val paint: Paint
    private var path = Path()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Log.d("VisitorAgreement", "OnDraw");
        canvas.drawPath(path, paint)
    }

    /**
     * Created by Karthikeyan This fuction reset the path.
     */
    fun signAgain() {
        val canvas = Canvas()
        // mPath.lineTo(mX, mY);
        // Log.d("VisitorAgreement", "Sign:" +path.isEmpty());
        path.reset()
        path = Path()
        // Log.d("VisitorAgreement", "Sign:" +path.isEmpty());
        // commit the path to our offscreen
        canvas.drawPath(path, paint)
        // kill this so we don't double draw
        invalidate()

        // Canvas canvas = new Canvas();

        // canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    val isPathNotEmpty: Boolean
        get() = path.isEmpty

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                return true
            }
            MotionEvent.ACTION_MOVE -> path.lineTo(eventX, eventY)
            MotionEvent.ACTION_UP -> {
            }
            else -> return false
        }

        // public void reset() {
        // this.drawCanvas.drawColor(0, Mode.CLEAR);
        // invalidate();
        // }
        // Schedules a repaint.
        invalidate()
        return true
    }

    // create Constructor
    init {
        paint = Paint()
        paint.isAntiAlias = true
        val dpSize = 2
        val dm = resources.displayMetrics
        val strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpSize.toFloat(), dm)
        paint.strokeWidth = strokeWidth
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
    }
}