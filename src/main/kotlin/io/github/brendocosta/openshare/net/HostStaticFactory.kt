package io.github.brendocosta.openshare.net


public interface HostStaticFactory<H> {

    fun fromLocalHost(): H
    fun getReachableNetworkHosts(): List<H>

}