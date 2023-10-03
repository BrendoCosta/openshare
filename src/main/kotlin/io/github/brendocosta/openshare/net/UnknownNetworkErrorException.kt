package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.net.NetworkException

class UnknownNetworkErrorException: NetworkException {

    companion object {

        val defaultMessage: String = "An unknown network error has occurred"

    }
    
    constructor(message: String = defaultMessage): super(message)
    constructor(cause: Throwable): super(defaultMessage, cause)

}