package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.app.App
import io.github.brendocosta.openshare.net.IPv4Host
import io.github.brendocosta.openshare.net.NetworkException
import io.github.brendocosta.openshare.net.NetworkSocketException
import io.github.brendocosta.openshare.net.NetworkSecurityException
import io.github.brendocosta.openshare.util.Log

import java.io.IOException
import java.net.InetAddress
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.net.SocketException
import java.util.Enumeration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object NetworkHost {

    @JvmStatic
    @Throws(NetworkException::class, NetworkSocketException::class)
    public fun getLocalHostIpAddress(): String {

        val enumNetInterfaces: Enumeration<NetworkInterface>

        try { enumNetInterfaces = NetworkInterface.getNetworkInterfaces() }
        catch (e: SocketException) { throw NetworkSocketException(e) }
        
        while (enumNetInterfaces.hasMoreElements()) {

            val intfc: NetworkInterface = enumNetInterfaces.nextElement()
            val enumInetAddr: Enumeration<InetAddress> = intfc.getInetAddresses()
            
            while (enumInetAddr.hasMoreElements()) {
                
                val inetAddr: InetAddress = enumInetAddr.nextElement()
                
                if (!inetAddr.isLoopbackAddress()
                    && !inetAddr.isLinkLocalAddress()
                    && inetAddr.isSiteLocalAddress()
                    && inetAddr is Inet4Address
                ) {

                    val ipv4Addr: String = inetAddr.getHostAddress() ?: ""

                    if (!ipv4Addr.isNullOrBlank()) {

                        return ipv4Addr.toString()

                    } else { throw NetworkException("Got a either null or empty device's IP address") }

                }

            }

        }

        throw NetworkException("Unable to find any IP address in device")

    }

    @JvmStatic
    @Throws(NetworkException::class, NetworkSocketException::class, NetworkSecurityException::class)
    public fun getLocalHost(): IPv4Host {

        val hostIpAddress: String
        val hostName: String
        
        try {

            hostIpAddress = getLocalHostIpAddress()
            hostName = InetAddress.getByName(hostIpAddress).getHostName()

            Log.debug("${hostIpAddress}")

            return IPv4Host(hostIpAddress, hostName)

        }
        catch (e: NetworkException)         { throw e /* getLocalHostIpAddress */ }
        catch (e: NetworkSocketException)   { throw e /* getLocalHostIpAddress */ }
        catch (e: SocketException)          { throw NetworkSocketException(e) /* InetAddress.getByName */ }
        catch (e: SecurityException)        { throw NetworkSecurityException(e) /* InetAddress.getByName */ }

    }

    @JvmStatic
    @Throws(NetworkException::class, NetworkSocketException::class, NetworkSecurityException::class)
    public fun getCurrentNetworkHosts(): List<@JvmSuppressWildcards Host> {

        val localHost: IPv4Host

        try { localHost = this.getLocalHost() }
        catch (e: NetworkException)         { throw e }
        catch (e: NetworkSocketException)   { throw e }
        catch (e: NetworkSecurityException) { throw e }

        val subnetIpAddress: String = localHost.getSubnet()
        val searchTimeout: Int = 50 // 50 ms
        val avaliableCores: Int = Runtime.getRuntime().availableProcessors()
        val avaliableThreads: Int = avaliableCores * 2
        val executorService: ExecutorService = Executors.newFixedThreadPool(avaliableThreads)
        val reachableHostsList: MutableList<IPv4Host> = mutableListOf<IPv4Host>()

        Log.debug("$avaliableCores CORES AVALIABLE")
        Log.debug("$avaliableThreads THREADS AVALIABLE")
        
        for (i in 0..254) {

            val ipAddressToReach: String = subnetIpAddress + "." + i
            val addressToReach: InetAddress

            try { addressToReach = InetAddress.getByName(ipAddressToReach) }
            catch (e: UnknownHostException) { throw NetworkException("Can't search host $ipAddressToReach", e) }

            try {

                executorService.execute(Runnable {

                    if (addressToReach.isReachable(searchTimeout)) {
    
                        val reachableHostIpAddress: String = addressToReach.getHostAddress() ?: ""
                        val reachableHostName: String = addressToReach.getHostName()
    
                        reachableHostsList.add(IPv4Host(reachableHostIpAddress, reachableHostName))
    
                        Log.debug("ADDRESS ${reachableHostIpAddress} (${reachableHostName}) IS REACHABLE!")
    
                    }
                
                })

            } catch (e: IOException) { throw NetworkException("Can't reach host due to a network error", e) }
            catch (e: IllegalArgumentException) { throw NetworkException("Search timeout can't be negative", e) }

        }

        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.MINUTES)

        return reachableHostsList.toList()

    }

}
