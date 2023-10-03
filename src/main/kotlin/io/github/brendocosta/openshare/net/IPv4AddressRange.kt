package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.IPv4Address
import io.github.brendocosta.openshare.net.IPv4AddressIterator

public class IPv4AddressRange(
    val start: IPv4Address,
    val end: IPv4Address
): Iterable<IPv4Address> {

    override operator fun iterator(): Iterator<IPv4Address> = IPv4AddressIterator(this)

}