package com.palmace.angulosapp

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // Declaración de las variables de la UI y Firebase
    private lateinit var vistaAngulo: VistaAngulo
    private lateinit var textoFeedback: TextView
    private lateinit var textoPuntuacion: TextView
    private lateinit var textoIntentos: TextView
    private lateinit var textoObjetivo: TextView
    private lateinit var textoActual: TextView
    private lateinit var textoDiferencia: TextView
    private lateinit var textoAciertos: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase Realtime Database
        // Nota: setPersistenceEnabled() debe llamarse antes de obtener cualquier referencia
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        database = FirebaseDatabase.getInstance().reference

        // Inicializar los elementos de la UI
        // Se usan genéricos explícitos para evitar errores de Type Mismatch
        vistaAngulo = findViewById(R.id.vistaAngulo)
        textoFeedback = findViewById(R.id.textoFeedback)
        textoPuntuacion = findViewById(R.id.textoPuntuacion)
        textoIntentos = findViewById(R.id.textoIntentos)
        textoObjetivo = findViewById(R.id.textoObjetivo)
        textoActual = findViewById(R.id.textoActual)
        textoDiferencia = findViewById(R.id.textoDiferencia)
        textoAciertos = findViewById(R.id.textoAciertos)

        // Configurar botones de voto
        findViewById<Button>(R.id.btnLike).setOnClickListener {
            enviarVoto("like")
        }
        findViewById<Button>(R.id.btnDislike).setOnClickListener {
            enviarVoto("dislike")
        }

        // Inicializar MediaPlayer para el sonido
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.campana).apply {
                setVolume(0.8f, 0.8f)
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error inicializando sonido", e)
        }

        // Configurar el callback de VistaAngulo
        // Ahora que tenemos la definición completa de VistaAngulo, esto funcionará
        vistaAngulo.establecerCallback(object : VistaAngulo.CallbackAngulo {
            override fun alCambiarAngulo(feedback: String, puntuacion: Int, intentos: Int, anguloActual: Float, diferencia: Float) {
                // Actualiza los TextViews con los datos recibidos del callback
                textoFeedback.text = feedback
                textoPuntuacion.text = "Puntuación: $puntuacion"
                textoIntentos.text = "Intentos: $intentos"
                textoActual.text = "Actual: ${"%.1f".format(anguloActual)}°"
                textoDiferencia.text = "Diferencia: ${"%.1f".format(diferencia)}°"
                // No hay lógica para textoAciertos en el callback, así que lo dejamos como está o puedes añadir la lógica aquí si la tienes
            }
        })
    }

    private fun enviarVoto(tipo: String) {
        val votoData = hashMapOf(
            "tipo" to tipo,
            "fecha" to System.currentTimeMillis(),
            "appVersion" to BuildConfig.VERSION_NAME // 'BuildConfig' es una clase generada y su referencia es válida
        )

        database.child("votos").push().setValue(votoData)
            .addOnSuccessListener {
                val mensaje = if (tipo == "like") "¡Gracias por tu voto positivo!" else "Tomaremos en cuenta tu feedback"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar voto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Es buena práctica verificar si mediaPlayer está inicializado antes de intentar liberarlo
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
