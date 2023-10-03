package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.IPAddress

public abstract class Host<P: IPAddress> {

    public abstract fun getAddress(): P
    public abstract fun getName(): String

}