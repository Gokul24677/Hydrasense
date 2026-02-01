package com.example.hydrasense

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.util.Log

class Device : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000

    private var lastPh: Double = 0.0
    private var lastColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_device)
        supportActionBar?.hide()

        val pulseRing1 = findViewById<ImageView>(R.id.pulseRing1)
        val pulseRing2 = findViewById<ImageView>(R.id.pulseRing2)
        val pulseRing3 = findViewById<ImageView>(R.id.pulseRing3)
        val deviceBubble = findViewById<android.view.View>(R.id.deviceBubble)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnDevice = findViewById<Button>(R.id.btnDevice)

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        startPulseAnimation(pulseRing1, 0)
        startPulseAnimation(pulseRing2, 500)
        startPulseAnimation(pulseRing3, 1000)

        // Start scanning automatically
        startBleScan()

        deviceBubble.setOnClickListener {
            showPairingDialog()
        }
        
        btnDevice.setOnClickListener {
            if (!isScanning) {
                startBleScan()
            }
        }

        NavigationHelper.setupBottomNavigation(this, R.id.nav_device)
    }

    private fun startBleScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val scanner = bluetoothAdapter!!.bluetoothLeScanner
        if (scanner == null) {
            Toast.makeText(this, "BLE Scanner not available", Toast.LENGTH_SHORT).show()
            return
        }

        isScanning = true
        findViewById<TextView>(R.id.tvStatus).text = "Scanning for Hydra..."
        
        // Stop scanning after a pre-defined scan period.
        handler.postDelayed({
            if (isScanning) {
                stopBleScan()
            }
        }, SCAN_PERIOD)

        scanner.startScan(leScanCallback)
    }

    private fun stopBleScan() {
        isScanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: ""
            if (deviceName.contains("hydra", ignoreCase = true)) {
                parseHydraData(result)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                val deviceName = result.device.name ?: ""
                if (deviceName.contains("hydra", ignoreCase = true)) {
                    parseHydraData(result)
                    break
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed with error: $errorCode")
        }
    }

    private fun parseHydraData(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return
        val serviceData = scanRecord.serviceData
        
        // Search for our specific UUID prefix or match exactly
        // UUID: 6a3f8c20-9b6a-4f10-bbfa-01c92b7a1234
        val uuid = android.os.ParcelUuid.fromString("6a3f8c20-9b6a-4f10-bbfa-01c92b7a1234")
        val data = serviceData[uuid]

        if (data != null && data.size >= 2) {
            val rawPh = data[0].toInt() and 0xFF
            val rawColor = data[1].toInt() and 0xFF
            
            lastPh = rawPh / 10.0
            lastColor = rawColor
            
            stopBleScan()
            runOnUiThread {
                showDeviceFound(lastPh, lastColor)
            }
        } else {
            // Found hydra but no service data yet
            runOnUiThread {
                findViewById<TextView>(R.id.tvStatus).text = "Hydra Found (Syncing...)"
            }
        }
    }

    private fun showDeviceFound(ph: Double, color: Int) {
        val deviceBubble = findViewById<android.view.View>(R.id.deviceBubble)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        
        deviceBubble.visibility = View.VISIBLE
        deviceBubble.alpha = 0f
        deviceBubble.animate().alpha(1f).setDuration(500).start()
        
        tvStatus.text = "Hydra Ready! (pH: $ph)"
        Toast.makeText(this, "Data received from Hydra Beacon", Toast.LENGTH_SHORT).show()
    }

    private fun startPulseAnimation(view: android.view.View, delay: Long) {
        val scaleX = android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f)
        val alpha = android.animation.ObjectAnimator.ofFloat(view, "alpha", 0.6f, 0.2f, 0.6f)

        android.animation.AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 2000
            startDelay = delay
            scaleX.repeatCount = android.animation.ValueAnimator.INFINITE
            scaleY.repeatCount = android.animation.ValueAnimator.INFINITE
            alpha.repeatCount = android.animation.ValueAnimator.INFINITE
            scaleX.repeatMode = android.animation.ValueAnimator.REVERSE
            scaleY.repeatMode = android.animation.ValueAnimator.REVERSE
            alpha.repeatMode = android.animation.ValueAnimator.REVERSE
            start()
        }
    }

    private fun showPairingDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hydra Connected")
            .setMessage("Received pH: $lastPh. Would you like to view full analysis?")
            .setPositiveButton("Analysis") { dialog, _ ->
                saveDevicePairing()
                dialog.dismiss()
                redirectToPHReadings()
            }
            .setNegativeButton("Scan Again") { dialog, _ ->
                dialog.dismiss()
                startBleScan()
            }
            .create()
            .show()
    }

    private fun saveDevicePairing() {
        val prefs = getSharedPreferences("HydraSensePrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("isDevicePaired", true).apply()
    }

    private fun redirectToPHReadings() {
        val intent = Intent(this, PH_Readings::class.java).apply {
            putExtra("LIVE_PH", lastPh)
            putExtra("LIVE_COLOR", lastColor)
            putExtra("IS_LIVE", true)
        }
        startActivity(intent)
        finish()
    }
}
