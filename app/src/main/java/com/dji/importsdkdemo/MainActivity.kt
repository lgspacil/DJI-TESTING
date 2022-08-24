package com.dji.importsdkdemo

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager

class MainActivity : AppCompatActivity(), DJISDKManager.SDKManagerCallback {

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.READ_PHONE_STATE
                ), 1)
        }

        mHandler = Handler(Looper.getMainLooper())
        DJISDKManager.getInstance().registerApp(this, this)

    }

    override fun onRegister(error: DJIError?) {
        if (error == DJISDKError.REGISTRATION_SUCCESS) {
            Log.i(TAG, "onRegister: Registration Successful")
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show()
            DJISDKManager.getInstance().startConnectionToProduct()
        } else {
            Log.i(TAG, "onRegister: Registration Failed - ${error?.description}")
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onProductDisconnect() {
        Log.i(TAG, "onProductDisconnect: Product Disconnected")
        Toast.makeText(this,  "Product Disconnected", Toast.LENGTH_LONG).show()
        notifyStatusChanged()
    }

    override fun onProductConnect(baseProduct: BaseProduct?) {
        Log.i(TAG, "onProductConnect: Product Connected")
        Toast.makeText(this,  "Product Connected", Toast.LENGTH_LONG).show()
        notifyStatusChanged()
    }

    override fun onProductChanged(baseProduct: BaseProduct?) {
        Log.i(TAG, "onProductChanged: Product Changed - $baseProduct")
        Toast.makeText(this,  "Product Changed", Toast.LENGTH_LONG).show()
        notifyStatusChanged()
    }

    override fun onComponentChange(
        componentKey: BaseProduct.ComponentKey?,
        oldComponent: BaseComponent?,
        newComponent: BaseComponent?
    ) {
        Log.i(TAG, "onComponentChange key: $componentKey, oldComponent: $oldComponent, newComponent: $newComponent")
        newComponent?.let { component ->
            component.setComponentListener { connected ->
                Log.i(TAG, "onComponentConnectivityChange: $connected")
            }
        }
    }

    override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {}

    override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {}

    private fun notifyStatusChanged() {
        mHandler.removeCallbacks(updateRunnable)
        mHandler.postDelayed(updateRunnable, 500)
    }

    private val updateRunnable = Runnable {
        val intent = Intent(FLAG_CONNECTION_CHANGE)
        sendBroadcast(intent)
    }

    companion object {
        const val TAG = "MainActivity"
        const val FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change"
    }

}