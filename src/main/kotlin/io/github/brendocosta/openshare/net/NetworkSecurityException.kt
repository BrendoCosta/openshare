package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.util.ApplicationDefaultErrorMessage
import io.github.brendocosta.openshare.net.NetworkException

public class NetworkSecurityException: NetworkException {

    companion object { val defaultMessage: String = ApplicationDefaultErrorMessage.NETWORK_SECURITY_ERROR }

    constructor(): super()
    constructor(message: String = defaultMessage): super(message)
    constructor(message: String = defaultMessage, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)

}