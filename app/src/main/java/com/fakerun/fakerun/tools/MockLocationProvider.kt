package com.fakerun.fakerun.tools

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock

class MockLocationProvider(name: String, context: Context) {
    private val providerName: String = name
    private val context: Context = context

    private val isGps = (providerName == LocationManager.GPS_PROVIDER)
    private val isNetwork = (providerName == LocationManager.NETWORK_PROVIDER)

    init {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 先移除可能残留的测试provider。若上次运行时app被系统异常终止
        // 而未执行清理，测试provider仍会残留在系统中（清除数据/重装app
        // 无法移除它），此时直接调用 addTestProvider 会抛出
        // "Provider xxx already exists" 异常。
        try {
            lm.removeTestProvider(providerName)
        } catch (e: IllegalArgumentException) {
            // provider不存在，或不是测试provider，忽略。
        }

        lm.addTestProvider(providerName, isNetwork, isGps, isNetwork, false, true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE)
        lm.setTestProviderEnabled(providerName, true)
    }

    /**
     * Push the location to system (mock).
     *
     * @param lat latitude
     * @param lon longitude
     */
    fun pushLocation(lat: Double, lon: Double) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val mockLocation = Location(providerName)
        mockLocation.latitude = lat
        mockLocation.longitude = lon
        mockLocation.altitude = 2.8 + ((920..1220).random() / 1000.0 - 1)
        mockLocation.time = System.currentTimeMillis()
        mockLocation.speed = 2.5F + ((780..1260).random() / 1000.0F - 1)
        mockLocation.bearing = 1F
        mockLocation.accuracy = 3F

        if (isGps) {
            val bundle = Bundle()
            bundle.putInt("satellites", 7)
            mockLocation.extras = bundle
        }

        mockLocation.bearingAccuracyDegrees = 0.1F
        mockLocation.verticalAccuracyMeters = 0.1F
        mockLocation.speedAccuracyMetersPerSecond = 0.01F
        mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        lm.setTestProviderLocation(providerName, mockLocation)
    }

    /**
     * Remove the provider.
     */
    fun shutdown() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.removeTestProvider(providerName)
    }
}
