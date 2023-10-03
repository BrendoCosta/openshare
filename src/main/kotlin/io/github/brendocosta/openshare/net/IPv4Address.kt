package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.IPAddress
import io.github.brendocosta.openshare.net.IPv4AddressRange
import io.github.brendocosta.openshare.net.IPAddressStaticFactory
import io.github.brendocosta.openshare.net.NetworkException
import io.github.brendocosta.openshare.net.NetworkSocketException
import io.github.brendocosta.openshare.util.Log
import java.net.InetAddress
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration

public class IPv4Address: IPAddress, Comparable<IPv4Address> {

    protected val address: UByteArray
    public override val prefixLength: Int

    companion object: IPAddressStaticFactory<IPv4Address> {

        const val MAXIMUM_ADDRESS: UInt = 0xFFFFFFFFu
        const val MAXIMUM_PREFIX_LENGTH: Int = 32
        const val DEFAULT_PREFIX_LENGTH: Int = MAXIMUM_PREFIX_LENGTH

        @JvmStatic
        @Throws(NetworkException::class, NetworkSocketException::class)
        public override fun fromLocalHost(): IPv4Address {

            val enumNetInterfaces: Enumeration<NetworkInterface>

            try { enumNetInterfaces = NetworkInterface.getNetworkInterfaces() }
            catch (e: SocketException) { throw NetworkSocketException(e) }

            if (!enumNetInterfaces.hasMoreElements()) { throw NetworkException("Can't find any network interface from this device") }

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

                            try { return IPv4Address.fromStringAddress(inetAddr.getHostAddress(), intfcAddr.getNetworkPrefixLength().toInt()) }
                            catch (e: IllegalArgumentException) { throw NetworkException("Failed to return device's IP address", e) }

                        } else { throw NetworkException("Got a either null or empty device's IP address") }

                    }

                }

            }

            throw NetworkException("Unable to find any IP address in device")
    
        }

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public override fun fromUInt(address: UInt): IPv4Address {

            return fromUInt(address, IPv4Address.DEFAULT_PREFIX_LENGTH)
    
        }
        
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public override fun fromUInt(address: UInt, prefixLength: Int): IPv4Address {

            val byteArray = UByteArray(4)
    
            byteArray[0] = (((address shr 24) and 0xFFu).toUByte())
            byteArray[1] = (((address shr 16) and 0xFFu).toUByte())
            byteArray[2] = (((address shr 8) and 0xFFu).toUByte())
            byteArray[3] = (((address shr 0) and 0xFFu).toUByte())

            try { return IPv4Address(byteArray, prefixLength) }
            catch (e: IllegalArgumentException) { throw IllegalArgumentException("Unable to create IPv4 address", e) }
    
        }

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public override fun fromStringAddress(address: String): IPv4Address {

            return fromStringAddress(address, IPv4Address.DEFAULT_PREFIX_LENGTH)

        }

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public override fun fromStringAddress(address: String, prefixLength: Int): IPv4Address {

            val byteArray = UByteArray(4)
            val tokenList = address.split(".")
    
            if (tokenList.size == 4) {

                for (i in 0..3) {

                    val value: UInt

                    try {value = Integer.parseInt(tokenList.get(i)).toUInt()}
                    catch (e: NumberFormatException) { throw IllegalArgumentException("Byte does not contain a parsable integer (received ${tokenList.get(i)})", e) }

                    if (value >= 0u && value <= 255u) {

                        byteArray.set(i, value.toUByte())

                    } else { throw IllegalArgumentException("Invalid IPv4 byte integer value (received ${value})") }

                }

            } else { throw IllegalArgumentException("IPv4 address must cointain 4 bytes (received ${tokenList.size})") }

            try { return IPv4Address(byteArray, prefixLength) }
            catch (e: IllegalArgumentException) { throw IllegalArgumentException("Unable to create IPv4 address", e) }
    
        }

    }

    @Throws(IllegalArgumentException::class)
    constructor(address: UByteArray, prefixLength: Int) {

        if (prefixLength > 0 && prefixLength <= IPv4Address.MAXIMUM_PREFIX_LENGTH) {

            if (address.size == 4) {

                this.address = address
                this.prefixLength = prefixLength

            } else { throw IllegalArgumentException("IPv4 address must contain 4 bytes (received ${address.size})") }

        } else { throw IllegalArgumentException("IPv4 address prefix length must be greater than zero and less than ${IPv4Address.MAXIMUM_PREFIX_LENGTH} (received ${prefixLength})") }

    }
    
    public override fun toString(): String {

        return this.address.joinToString(".")

    }

    public override fun toUInt(): UInt {

        return (this.address.get(0).toUInt() shl 24) + (this.address.get(1).toUInt() shl 16) + (this.address.get(2).toUInt() shl 8) + (this.address.get(3).toUInt() shl 0)

    }

    public override fun getSubnetworkMaskAddress(): IPv4Address {

        val maskAddress: UInt = IPv4Address.MAXIMUM_ADDRESS shl (IPv4Address.MAXIMUM_PREFIX_LENGTH - this.prefixLength)

        return IPv4Address.fromUInt(maskAddress, this.prefixLength)

    }
    
    public override fun getNetworkBroadcastAddress(): IPv4Address {

        val broadcastAddress: UInt = this.getNetworkAddress().toUInt() or this.getSubnetworkMaskAddress().toUInt().inv()

        return IPv4Address.fromUInt(broadcastAddress, IPv4Address.MAXIMUM_PREFIX_LENGTH)

    }

    public override fun getNetworkAddress(): IPv4Address {

        // Calcular quantidade de IPs numa subnet: (2^(32-N))-2 sendo N o número de bits disponiveis
        // EX: (2^(32-24))-2 = 254 endereços
        //
        // Range = Pega IP network base, transforma em int, soma com a quantidade de ips na subnet, pega ip maximo

        val subNetworkMask: UInt = IPv4Address.MAXIMUM_ADDRESS shl (IPv4Address.MAXIMUM_PREFIX_LENGTH - this.prefixLength)
        val networkAddress: UInt = this.toUInt() and subNetworkMask

        return IPv4Address.fromUInt(networkAddress, this.prefixLength)

    }

    public fun getSubnetworkUsableAddressesCount(): UInt {

        return (Math.pow(2.0, (IPv4Address.MAXIMUM_PREFIX_LENGTH - this.getNetworkAddress().prefixLength).toDouble()).toInt() - 2).toUInt()

    }

    public fun getSubnetworkUppermostAddress(): IPv4Address {

        return this.getNetworkBroadcastAddress() - 1u

    }

    public fun getSubnetworkLowermostAddress(): IPv4Address {

        return this.getNetworkAddress() + 1u

    }

    public operator fun dec(): IPv4Address { return this - 1u }
    public operator fun inc(): IPv4Address { return this + 1u }
    public override operator fun minus(other: UInt): IPv4Address { return IPv4Address.fromUInt(this.toUInt() - other, this.prefixLength) }
    public override operator fun plus(other: UInt): IPv4Address { return IPv4Address.fromUInt(this.toUInt() + other, this.prefixLength) }
    public override operator fun compareTo(other: IPv4Address): Int {

        if (this.toUInt() < other.toUInt()) return -1
        if (this.toUInt() == other.toUInt()) return 0
        if (this.toUInt() > other.toUInt()) return 1

        return 0

    }
    
    public operator fun rangeTo(other: IPv4Address) = IPv4AddressRange(this, other)

}