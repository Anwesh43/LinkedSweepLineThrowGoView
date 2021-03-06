package com.example.sweeplinethrowgoview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Canvas
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#01579B",
    "#00C853",
    "#FFC107",
    "#2962FF"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val sizeFactor : Float = 5.9f
val delay : Long = 20
val deg : Float = 90f
val rFactor : Float = 5.6f
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawSweepLineThrowGo(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sc1 : Float = scale.divideScale(0, parts)
    val sc2 : Float = scale.divideScale(1, parts)
    val sc3 : Float = scale.divideScale(2, parts)
    val sc4 : Float = scale.divideScale(3, parts)
    val sc5 : Float = scale.divideScale(4, parts)
    val r : Float = size / rFactor
    if (sc1 <= 0) {
        return
    }
    save()
    translate(w / 2, h / 2)
    save()
    translate(0f, (h / 2 + r) * sc5)
    drawArc(RectF(-r, -r, r, r), 0f, 360f * sc1, true, paint)
    restore()
    for (j in 0..1) {
        save()
        rotate(-deg * (1 - 2 * j) * sc3)
        translate(0f, (h / 2 + size) * sc4)
        drawLine(0f, 0f, 0f, size * sc2, paint)
        restore()
    }
    restore()
}

fun Canvas.drawSLTGNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawSweepLineThrowGo(scale, w, h, paint)
}

class SweepLineThrowGoView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SLTGNode(var i : Int, val state : State = State()) {

        private var next : SLTGNode? = null
        private var prev : SLTGNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SLTGNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSLTGNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SLTGNode {
            var curr : SLTGNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SweepLineThrowGo(var i : Int) {

        private var curr : SLTGNode = SLTGNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SweepLineThrowGoView) {

        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val animator : Animator = Animator(view)
        private val sweepLineThrowGo : SweepLineThrowGo = SweepLineThrowGo(0)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            sweepLineThrowGo.draw(canvas, paint)
            animator.animate {
                sweepLineThrowGo.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sweepLineThrowGo.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : SweepLineThrowGoView {
            val view : SweepLineThrowGoView = SweepLineThrowGoView(activity)
            activity.setContentView(view)
            return view
        }
    }
}