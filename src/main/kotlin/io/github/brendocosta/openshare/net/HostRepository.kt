package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.Host
import io.github.brendocosta.openshare.net.IHostRepository
import io.github.brendocosta.openshare.net.IPv4Address
import io.github.brendocosta.openshare.net.IPv4AddressFactory
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

public open class HostRepository {

    companion object: IHostRepository {

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class)
        protected fun getLocalHostIpAddress(): IPv4Address {

            val enumNetInterfaces: Enumeration<NetworkInterface>

            try { enumNetInterfaces = NetworkInterface.getNetworkInterfaces() }
            catch (e: SocketException) { throw NetworkSocketException(e) }
            
            while (enumNetInterfaces.hasMoreElements()) {

                val netIntfc: NetworkInterface = enumNetInterfaces.nextElement()

                for (intfcAddr in netIntfc.getInterfaceAddresses()) {

                    val inetAddr: InetAddress = intfcAddr.getAddress()

                    if (!inetAddr.isLoopbackAddress()
                        && !inetAddr.isLinkLocalAddress()
                        && inetAddr.isSiteLocalAddress()
                        && inetAddr is Inet4Address
                    ) {

                        if (!inetAddr.getHostAddress().isNullOrBlank()) {

                            val hostAddress: ByteArray = inetAddr.getAddress()
                            val hostPrefixLength = intfcAddr.getNetworkPrefixLength().toInt()

                            Log.debug("FOUND LOCAL HOST IP ADDRESS: ${inetAddr.getHostAddress()}/$hostPrefixLength")

                            return IPv4Address(hostAddress, hostPrefixLength)

                        } else { throw NetworkException("Got a either null or empty device's IP address") }

                    }

                }

            }

            throw NetworkException("Unable to find any IP address in device")

        }

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class, NetworkSecurityException::class)
        public override fun getLocalHost(): Host {

            try {

                val hostIpAddress: IPv4Address = getLocalHostIpAddress()
                val hostName: String = InetAddress.getByName(hostIpAddress.toString()).getHostName()
                val debugHost: Host = Host(hostIpAddress, hostName)

                return Host(hostIpAddress, hostName)

            }
            catch (e: NetworkException)         { throw e /* getLocalHostIpAddress */ }
            catch (e: NetworkSocketException)   { throw e /* getLocalHostIpAddress */ }
            catch (e: SocketException)          { throw NetworkSocketException(e) /* InetAddress.getByName */ }
            catch (e: SecurityException)        { throw NetworkSecurityException(e) /* InetAddress.getByName */ }

        }

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class, NetworkSecurityException::class)
        public override fun getReachableHosts(): List<Host> {

            val localHost: Host

            try { localHost = getLocalHost() }
            catch (e: NetworkException)         { throw e }
            catch (e: NetworkSocketException)   { throw e }
            catch (e: NetworkSecurityException) { throw e }

            val networkAddress: IPv4Address = localHost.address.getNetworkAddress() as IPv4Address
            val searchTimeout: Int = 50 // 50 ms
            val avaliableCores: Int = Runtime.getRuntime().availableProcessors()
            val avaliableThreads: Int = avaliableCores * 2
            val executorService: ExecutorService = Executors.newFixedThreadPool(avaliableThreads)
            val reachableHostsList: MutableList<Host> = mutableListOf<Host>()

            Log.debug("${networkAddress.toString()}")

            return reachableHostsList.toList()

            Log.debug("$avaliableCores CORES AVALIABLE")
            Log.debug("$avaliableThreads THREADS AVALIABLE")
            
            for (i in 0..254) {

                // @TODO: Repair
                val ipAddressToReach: String = networkAddress.toString().split(".").take(3).joinToString(".") + i
                val addressToReach: InetAddress

                try { addressToReach = InetAddress.getByName(ipAddressToReach) }
                catch (e: UnknownHostException) { throw NetworkException("Can't search host $ipAddressToReach", e) }

                try {

                    executorService.execute(Runnable {

                        if (addressToReach.isReachable(searchTimeout)) {
        
                            val reachableHostIpAddress: IPv4Address = IPv4AddressFactory.fromStringAddress(addressToReach.getHostAddress() ?: "0.0.0.0")
                            val reachableHostName: String = addressToReach.getHostName()
        
                            reachableHostsList.add(Host(reachableHostIpAddress, reachableHostName))
        
                            Log.debug("ADDRESS ${reachableHostIpAddress.toString()} (${reachableHostName}) IS REACHABLE!")
        
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

}