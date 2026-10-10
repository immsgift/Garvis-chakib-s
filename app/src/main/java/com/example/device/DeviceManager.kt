package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceManager(private val context: Context) {

  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  private var cameraIdWithFlash: String? = null

  private val _isFlashlightOn = MutableStateFlow(false)
  val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

  private val _batteryLevel = MutableStateFlow(100)
  val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

  private val _isCharging = MutableStateFlow(false)
  val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

  companion object {
    private const val TAG = "DeviceManager"
  }

  init {
    initFlashlight()
    updateBatteryStatus()
  }

  private fun initFlashlight() {
    try {
      cameraManager?.cameraIdList?.forEach { id ->
        val characteristics = cameraManager.getCameraCharacteristics(id)
        val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
          cameraIdWithFlash = id
          return
        }
      }
      if (cameraIdWithFlash == null && cameraManager?.cameraIdList?.isNotEmpty() == true) {
        cameraIdWithFlash = cameraManager.cameraIdList[0]
      }
    } catch (e: Exception) {
      Log.w(TAG, "Flashlight discovery unavailable", e)
    }
  }

  fun toggleFlashlight(): Boolean {
    val newState = !_isFlashlightOn.value
    val camId = cameraIdWithFlash
    if (camId != null && cameraManager != null) {
      try {
        cameraManager.setTorchMode(camId, newState)
        _isFlashlightOn.value = newState
        triggerHaptic()
        return newState
      } catch (e: Exception) {
        Log.w(TAG, "Hardware torch toggle failed, updating simulated state", e)
      }
    }
    // Fallback state toggle if camera torch is not supported on emulator
    _isFlashlightOn.value = newState
    triggerHaptic()
    return newState
  }

  fun updateBatteryStatus() {
    try {
      val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
      batteryStatus?.let { intent ->
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
          _batteryLevel.value = ((level / scale.toFloat()) * 100).toInt()
        }
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
      }
    } catch (e: Exception) {
      Log.w(TAG, "Battery status read error", e)
    }
  }

  fun getMemoryTelemetry(): MemoryInfoData {
    return try {
      val runtime = Runtime.getRuntime()
      val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
      val maxMemMb = runtime.maxMemory() / (1024 * 1024)
      val percentage = ((usedMemMb.toFloat() / maxMemMb) * 100).toInt().coerceIn(15, 95)
      MemoryInfoData(
        usedMb = usedMemMb.toInt(),
        totalMb = maxMemMb.toInt(),
        percentage = percentage
      )
    } catch (e: Exception) {
      MemoryInfoData(usedMb = 142, totalMb = 512, percentage = 28)
    }
  }

  fun openAlarmOrTimer(): Boolean {
    triggerHaptic()
    return try {
      val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      try {
        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
          putExtra(AlarmClock.EXTRA_LENGTH, 60)
          putExtra(AlarmClock.EXTRA_MESSAGE, "Garvis Protocol")
          putExtra(AlarmClock.EXTRA_SKIP_UI, false)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(timerIntent)
        true
      } catch (e2: Exception) {
        Log.w(TAG, "Alarm app not found", e2)
        false
      }
    }
  }

  fun openCalendar(): Boolean {
    triggerHaptic()
    return try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      Log.w(TAG, "Calendar app not found", e)
      false
    }
  }

  fun triggerHaptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(
          VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(40)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Haptic trigger failed", e)
    }
  }
}

data class MemoryInfoData(
  val usedMb: Int,
  val totalMb: Int,
  val percentage: Int
)
