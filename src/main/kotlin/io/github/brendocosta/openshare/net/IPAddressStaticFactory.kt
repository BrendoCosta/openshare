package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.AbstractFactory

public interface IPAddressStaticFactory<T>: AbstractFactory {

    fun fromLocalHost(): T
    fun fromStringAddress(address: String): T
    fun fromStringAddress(address: String, prefixLength: Int): T
    fun fromUInt(address: UInt): T
    fun fromUInt(address: UInt, prefixLength: Int): T

}