package com.example.firemenu

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class FloatingMenuService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: View
    private lateinit var params: WindowManager.LayoutParams
    private var startX = 0
    private var startY = 0
    private var touchX = 0f
    private var touchY = 0f
    private var isDragging = false

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        view = LayoutInflater.from(this).inflate(R.layout.floating_menu, null)
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100
        wm.addView(view, params)

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - touchX
                    val dy = event.rawY - touchY
                    if (Math.hypot(dx.toDouble(), dy.toDouble()) > 8) {
                        isDragging = true
                        params.x = startX + dx.toInt()
                        params.y = startY + dy.toInt()
                        wm.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> true
                else -> false
            }
        }

        view.findViewById<Button>(R.id.btn_close).setOnClickListener { stopSelf() }
        view.findViewById<Button>(R.id.btn_action).setOnClickListener {
            Toast.makeText(this, "Hành động", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onDestroy() {
        super.onDestroy()
        try { wm.removeView(view) } catch (_: Exception) {}
    }
}
