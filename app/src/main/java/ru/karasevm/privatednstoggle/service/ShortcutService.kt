package ru.karasevm.privatednstoggle.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.karasevm.privatednstoggle.PrivateDNSApp
import ru.karasevm.privatednstoggle.data.DnsServerRepository
import ru.karasevm.privatednstoggle.util.PreferenceHelper
import ru.karasevm.privatednstoggle.util.PrivateDNSUtils
import ru.karasevm.privatednstoggle.util.PrivateDNSUtils.DNS_MODE_AUTO
import ru.karasevm.privatednstoggle.util.PrivateDNSUtils.DNS_MODE_OFF
import ru.karasevm.privatednstoggle.util.PrivateDNSUtils.DNS_MODE_PRIVATE
import ru.karasevm.privatednstoggle.util.PrivateDNSUtils.checkForPermission

class ShortcutService : Service() {

    private val repository: DnsServerRepository by lazy { (application as PrivateDNSApp).repository }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val ACTION_SWITCH_MODE = "myapp://switch_mode"
        private const val ACTION_TOGGLE_MODE = "myapp://toggle_mode"
    }

    private fun setDnsModeAndShowToast(dnsMode: String) {
        PrivateDNSUtils.setPrivateMode(contentResolver, dnsMode)
        val text = when (dnsMode) {
            DNS_MODE_OFF -> "DNS set to Off"
            DNS_MODE_AUTO -> "DNS set to Auto"
            DNS_MODE_PRIVATE -> "DNS set to Private Provider"
            else -> "Unknown"
        }
        scope.launch {
            launch(Dispatchers.Main) {
                Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.data.toString()
        val sharedPrefs = PreferenceHelper.defaultPreference(this)
        if(checkForPermission(this)) {
            if (data == ACTION_SWITCH_MODE) {
                PrivateDNSUtils.getNextProvider(
                    sharedPrefs,
                    scope,
                    repository,
                    contentResolver,
                    skipProvider = true,
                    onNext = { dnsMode, _ ->
                        setDnsModeAndShowToast(dnsMode)
                    })
            } else if(data == ACTION_TOGGLE_MODE) {
                val dnsMode = PrivateDNSUtils.getPrivateMode(contentResolver)
                when (dnsMode) {
                    DNS_MODE_OFF -> {
                        PrivateDNSUtils.getNextProvider(
                            sharedPrefs,
                            scope,
                            repository,
                            contentResolver,
                            skipProvider = true,
                            onNext = { dnsMode, _ ->
                                setDnsModeAndShowToast(dnsMode)
                            })
                    }
                    else -> {
                        PrivateDNSUtils.setPrivateMode(contentResolver, DNS_MODE_OFF)
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}