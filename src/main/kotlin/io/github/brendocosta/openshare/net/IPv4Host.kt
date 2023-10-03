package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.Host
import io.github.brendocosta.openshare.net.HostStaticFactory
import io.github.brendocosta.openshare.net.IPv4Address
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

public class IPv4Host: Host<IPv4Address> {

    private val address: IPv4Address

    constructor(address: IPv4Address) {

        this.address = address

    }

    companion object: HostStaticFactory<IPv4Host> {

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class)
        public override fun fromLocalHost(): IPv4Host {

            return IPv4Host(IPv4Address.fromLocalHost())

        }

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class, NetworkSecurityException::class)
        public override fun getReachableNetworkHosts(): List<IPv4Host> {

            val localHost: IPv4Host

            try { localHost = IPv4Host.fromLocalHost() }
            catch (e: NetworkException)         { throw e }
            catch (e: NetworkSocketException)   { throw e }

            if (localHost.address.prefixLength < 19) { throw NetworkException("Unable to search hosts on networks whose prefix length is less than 19 (received ${localHost.address.prefixLength})") }

            val startAddress: IPv4Address = localHost.address.getSubnetworkLowermostAddress()
            val finalAddress: IPv4Address = localHost.address.getSubnetworkUppermostAddress()
            val searchTimeout: Int = 50 // 50 ms
            val avaliableCores: Int = Runtime.getRuntime().availableProcessors()
            val avaliableThreads: Int = avaliableCores * 2
            val executorService: ExecutorService = Executors.newFixedThreadPool(avaliableThreads)
            val reachableHostsList: MutableList<IPv4Host> = mutableListOf<IPv4Host>()
            
            /*for (i in startAddress..finalAddress) {

                Log.debug("ITERATE OVER: ${i.toString()}")

            }*/

            executorService.shutdown()
            executorService.awaitTermination(1, TimeUnit.MINUTES)

            return reachableHostsList.toList()

        }

    }

    public override fun getAddress(): IPv4Address {

        return this.address

    }

    public override fun getName(): String {

        return ""

    }

}