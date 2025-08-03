package com.palmace.angulosapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class VistaAngulo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Variables de estado
    var anguloObjetivo: Float = 0f
    var anguloActual: Float = 0f
    private var arrastrando: Boolean = false
    private var centroCirculo: PointF = PointF()
    private var radioCirculo: Float = 0f
    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinturaTexto = Paint(Paint.ANTI_ALIAS_FLAG)

    // Callback para comunicar eventos al MainActivity
    interface CallbackAngulo {
        fun alCambiarAngulo(feedback: String, puntuacion: Int, intentos: Int, anguloActual: Float, diferencia: Float)
    }
    private var callback: CallbackAngulo? = null

    init {
        configurarPinturas()
        generarNuevoObjetivo()
    }

    private fun configurarPinturas() {
        pintura.color = Color.BLUE
        pintura.style = Paint.Style.STROKE
        pintura.strokeWidth = 8f

        pinturaTexto.color = Color.BLACK
        pinturaTexto.textSize = 48f
        pinturaTexto.textAlign = Paint.Align.CENTER
    }

    fun establecerCallback(callback: CallbackAngulo) {
        this.callback = callback
    }

    private fun generarNuevoObjetivo() {
        anguloObjetivo = (0..360).random().toFloat()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centroCirculo = PointF(w / 2f, h / 2f)
        radioCirculo = min(w, h) * 0.4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dibujarCirculo(canvas)
        dibujarAguja(canvas)
        dibujarTexto(canvas)
    }

    private fun dibujarCirculo(canvas: Canvas) {
        canvas.drawCircle(centroCirculo.x, centroCirculo.y, radioCirculo, pintura)
    }

    private fun dibujarAguja(canvas: Canvas) {
        val radianes = Math.toRadians(anguloActual.toDouble())
        val finX = centroCirculo.x + radioCirculo * sin(radianes).toFloat()
        val finY = centroCirculo.y - radioCirculo * cos(radianes).toFloat()
        canvas.drawLine(centroCirculo.x, centroCirculo.y, finX, finY, pintura)
    }

    private fun dibujarTexto(canvas: Canvas) {
        canvas.drawText("Objetivo: ${anguloObjetivo.toInt()}°", centroCirculo.x, centroCirculo.y - 50, pinturaTexto)
        canvas.drawText("Actual: ${anguloActual.toInt()}°", centroCirculo.x, centroCirculo.y + 50, pinturaTexto)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (estaTocandoAguja(event.x, event.y)) {
                    arrastrando = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (arrastrando) {
                    anguloActual = calcularAngulo(event.x, event.y)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (arrastrando) {
                    arrastrando = false
                    verificarPrecision()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun estaTocandoAguja(x: Float, y: Float): Boolean {
        val distancia = sqrt((x - centroCirculo.x).pow(2) + (y - centroCirculo.y).pow(2))
        return distancia <= radioCirculo * 1.1f
    }

    private fun calcularAngulo(x: Float, y: Float): Float {
        val dx = x - centroCirculo.x
        val dy = centroCirculo.y - y
        // Corrección de sintaxis: se cierra el 'let' con su propia llave
        return (Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())).toFloat().let {
            if (it < 0) it + 360f else it
        })
    }

    private fun verificarPrecision() {
        val diferencia = min(abs(anguloActual - anguloObjetivo), 360 - abs(anguloActual - anguloObjetivo))
        val feedback = when {
            diferencia <= 5f -> "¡Perfecto! Ángulo exacto"
            diferencia <= 15f -> "Cerca, desviación de ${diferencia.toInt()}°"
            else -> "Sigue intentando (${diferencia.toInt()}° de diferencia)"
        }
        // Llama al callback si está configurado
        callback?.alCambiarAngulo(feedback, 0, 0, anguloActual, diferencia)
    }
}
