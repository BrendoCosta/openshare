package io.github.brendocosta.openshare.net

public abstract class IPAddress {

    public abstract val prefixLength: Int
    
    public abstract fun getNetworkBroadcastAddress(): IPAddress
    public abstract fun getNetworkAddress(): IPAddress
    public abstract fun getSubnetworkMaskAddress(): IPAddress
    public abstract override fun toString(): String
    /**
     * Adds a [member] to this group.
     * @return the new size of the group.
     */
    public abstract fun toUInt(): UInt
    public abstract operator fun minus(other: UInt): IPAddress
    public abstract operator fun plus(other: UInt): IPAddress

}