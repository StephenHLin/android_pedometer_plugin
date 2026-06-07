package com.example.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot

class PedometerPlugin(godot: Godot) : GodotPlugin(godot) {

    private var sensorManager: SensorManager? = null
    private var stepCount: Int = 0

    override fun getPluginName() = "PedometerPlugin"

    override fun onMainResume() {
    val ctx = activity ?: return
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        if (ctx.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ctx.requestPermissions(
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                1001
            )
        }
    }
    val intent = android.content.Intent(ctx, PedometerService::class.java)
    ctx.startForegroundService(intent)
}

	override fun onMainPause() {
		// Don't stop the service on pause — let it keep running
	}

	@UsedByGodot
	fun getStepCount(): Int = PedometerService.stepCount

}