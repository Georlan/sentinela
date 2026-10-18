package dev.georlan.sentinela

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var lastPoint: TextView
    private lateinit var bootButton: Button
    private val mainHandler = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private val prefs by lazy { TrackingPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 32)
        }
        fun label(value: String, size: Float = 16f): TextView = TextView(this).apply {
            text = value
            textSize = size
            setPadding(0, 12, 0, 12)
            root.addView(this)
        }
        label("Sentinela", 28f)
        label("Protótipo offline: coordenadas guardadas somente no celular.", 14f)
        status = label("Verificando permissoes...")
        root.addView(Button(this).apply {
            text = "1. Permitir localizacao"
            setOnClickListener {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION
                )
            }
        })
        root.addView(Button(this).apply {
            text = "2. Iniciar monitoramento"
            setOnClickListener { startTracking() }
        })
        root.addView(Button(this).apply {
            text = "Parar monitoramento"
            setOnClickListener {
                startService(Intent(this@MainActivity, LocationTrackingService::class.java)
                    .setAction(LocationTrackingService.ACTION_STOP))
                prefs.trackingRequested = false
                updateStatus()
            }
        })
        root.addView(Button(this).apply {
            text = "Configurar localizacao em segundo plano"
            setOnClickListener {
                Toast.makeText(this@MainActivity,
                    "Em Permissoes > Localizacao, escolha Permitir o tempo todo.",
                    Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")))
            }
        })
        bootButton = Button(this).apply {
            setOnClickListener {
                if (prefs.bootResumeEnabled) {
                    prefs.bootResumeEnabled = false
                } else if (PermissionChecks.hasBackgroundLocation(this@MainActivity)) {
                    prefs.bootResumeEnabled = true
                } else {
                    Toast.makeText(this@MainActivity,
                        "Primeiro autorize Permitir o tempo todo nas configuracoes.",
                        Toast.LENGTH_LONG).show()
                }
                updateStatus()
            }
            root.addView(this)
        }
        lastPoint = label("Sem posicoes registradas", 15f)
        root.addView(Button(this).apply {
            text = "Atualizar ultima posicao"
            setOnClickListener { showLatest() }
        })
        val scroll = ScrollView(this)
        scroll.addView(root, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        showLatest()
    }

    private fun startTracking() {
        if (!PermissionChecks.hasLocation(this)) {
            Toast.makeText(this, "Autorize a localizacao primeiro", Toast.LENGTH_LONG).show()
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
        try {
            startForegroundService(Intent(this, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START))
            Toast.makeText(this, "Solicitacao de inicio enviada", Toast.LENGTH_SHORT).show()
        } catch (error: RuntimeException) {
            Toast.makeText(this, "Android impediu o inicio: ${error.javaClass.simpleName}",
                Toast.LENGTH_LONG).show()
        }
        mainHandler.postDelayed({ updateStatus() }, 1000L)
    }

    private fun updateStatus() {
        val location = PermissionChecks.hasLocation(this)
        val background = PermissionChecks.hasBackgroundLocation(this)
        status.text = "Localizacao: ${if (location) "permitida" else "pendente"}\n" +
            "Em segundo plano: ${if (background) "permitida" else "pendente"}\n" +
            "Servico nesta execucao: ${if (LocationTrackingService.running) "ativo" else "inativo"}"
        bootButton.text = "Retomar apos reiniciar: ${if (prefs.bootResumeEnabled) "SIM" else "NAO"}"
    }

    private fun showLatest() {
        io.execute {
            val display = try {
                LocationStore(applicationContext).use { db ->
                    val latest = db.latest()
                    if (latest == null) "Nenhuma posicao salva ainda."
                    else {
                        val date = DateFormat.getDateTimeInstance().format(latest.recordedAtMs)
                        String.format(Locale.US,
                            "Ultima posicao: %.6f, %.6f\nPrecisao: ±%.0f m\nData GPS: %s\nRegistros: %d",
                            latest.latitude, latest.longitude, latest.accuracyMeters,
                            date, db.count())
                    }
                }
            } catch (error: Exception) {
                "Falha ao ler dados locais: ${error.javaClass.simpleName}"
            }
            mainHandler.post { lastPoint.text = display }
        }
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_LOCATION = 10
        private const val REQUEST_NOTIFICATIONS = 11
    }
}
