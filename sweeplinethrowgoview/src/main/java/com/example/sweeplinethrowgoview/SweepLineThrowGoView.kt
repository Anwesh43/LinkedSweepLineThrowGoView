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
    val r : Float = size / 9
    save()
    translate(w / 2, h / 2)
    save()
    translate(0f, (h / 2 + r) * sc5)
    drawArc(RectF(-r, -r, r, r), 0f, 360f * sc1, true, paint)
    restore()
    for (j in 0..1) {
        save()
        rotate(-90f * (1 - 2 * j) * sc3)
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
