package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.IPv4Address
import io.github.brendocosta.openshare.net.IPv4AddressRange

public class IPv4AddressIterator(val range: IPv4AddressRange): Iterator<IPv4Address> {

    private var currentAddress: IPv4Address = range.start

    public override fun hasNext(): Boolean {

        return currentAddress < range.end

    }

    public override fun next(): IPv4Address {

        val nextAddress: IPv4Address = this.currentAddress + 1u
        this.currentAddress = nextAddress
        return nextAddress

    }

}