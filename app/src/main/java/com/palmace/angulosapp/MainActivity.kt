package com.palmace.angulosapp

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.*
import java.util.*

class MainActivity : AppCompatActivity() {
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

// Inicializar Firebase
FirebaseDatabase.getInstance().setPersistenceEnabled(true) // Para offline
database = FirebaseDatabase.getInstance().reference

// Configurar botones
findViewById<Button>(R.id.btnLike).setOnClickListener {
enviarVoto("like")
}

findViewById<Button>(R.id.btnDislike).setOnClickListener {
enviarVoto("dislike")
}

// Resto de tu inicialización existente...
try {
mediaPlayer = MediaPlayer.create(this, R.raw.campana).apply {
setVolume(0.8f, 0.8f)
}
} catch (e: Exception) {
Log.e("MediaPlayer", "Error inicializando sonido", e)
}

vistaAngulo = findViewById(R.id.vistaAngulo)
textoFeedback = findViewById(R.id.textoFeedback)
textoPuntuacion = findViewById(R.id.textoPuntuacion)
textoIntentos = findViewById(R.id.textoIntentos)
textoObjetivo = findViewById(R.id.textoObjetivo)
textoActual = findViewById(R.id.textoActual)
textoDiferencia = findViewById(R.id.textoDiferencia)
textoAciertos = findViewById(R.id.textoAciertos)

vistaAngulo.establecerCallback(object : VistaAngulo.CallbackAngulo {
override fun alCambiarAngulo(feedback: String, puntuacion: Int, intentos: Int, anguloActual: Float, diferencia: Float) {
// Tu lógica existente...
}
})
}

private fun enviarVoto(tipo: String) {
val votoData = hashMapOf(
"tipo" to tipo,
"fecha" to System.currentTimeMillis(),
"appVersion" to BuildConfig.VERSION_NAME
)

database.child("votos").push().setValue(votoData)
.addOnSuccessListener {
val mensaje = if (tipo == "like") "¡Gracias por tu voto positivo!"
else "Tomaremos en cuenta tu feedback"
Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
}
.addOnFailureListener { e ->
Toast.makeText(this, "Error al enviar voto: ${e.message}", Toast.LENGTH_SHORT).show()
}
}

override fun onDestroy() {
super.onDestroy()
mediaPlayer.release()
}
}

// La clase VistaAngulo permanece exactamente igual
class VistaAngulo(context: Context, attrs: AttributeSet?) : View(context, attrs) {
// ... (todo tu código existente) ...
}