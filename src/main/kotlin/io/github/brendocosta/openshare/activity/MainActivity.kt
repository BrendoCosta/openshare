package io.github.brendocosta.openshare.activity

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.content.Context
import android.os.Bundle;
import com.getcapacitor.BridgeActivity
import io.github.brendocosta.openshare.app.App
import io.github.brendocosta.openshare.EchoPlugin
import io.github.brendocosta.openshare.util.Log

public class MainActivity: BridgeActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {

        val SERVICE_NAME: String = ""
        val SERVICE_TYPE: String = "_services._dns-sd._udp."

        // Instantiate a new DiscoveryListener
        val nsdManager: NsdManager = this.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        val resolveListener = object : NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Called when the resolve fails. Use the error code to debug.
                Log.debug("Service Resolve failed: $errorCode")
            }
    
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.debug("Service Resolve Succeeded. $serviceInfo")
    
                //if (serviceInfo.serviceName == mServiceName) {
                //    Log.debug("Service Same IP.")
                //    return
                //}
                //MSERVICE = serviceInfo
                //val port: Int = serviceInfo.port
                //val host: InetAddress = serviceInfo.host
            }
        }
        
        val discoveryListener = object : NsdManager.DiscoveryListener {

            // Called as soon as service discovery begins.
            override fun onDiscoveryStarted(regType: String) {
                Log.debug("Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                // A service was found! Do something with it.
                Log.debug("Service discovery success. $service")
                when {

                    service.serviceType != SERVICE_TYPE -> Log.debug("service type doesn't match: ${service.serviceType}")
                    service.serviceName == SERVICE_NAME -> Log.debug("service same machine: ${service.serviceName}")
                    service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(service, resolveListener)

                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.debug("service lost: $service")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.debug("Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.debug("Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.debug("Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }
        }

        registerPlugin(EchoPlugin::class.java)
        super.onCreate(savedInstanceState)

        val app: App = App.getInstance()
        app.context = this

        //nsdManager.discoverServices("_device-info._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        //nsdManager.discoverServices("_services._dns-sd._udp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

    }

}