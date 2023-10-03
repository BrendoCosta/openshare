package io.github.brendocosta.openshare.net

import io.github.brendocosta.openshare.util.ApplicationDefaultErrorMessage
import io.github.brendocosta.openshare.util.Log

public open class NetworkException: Exception {

    companion object { val defaultMessage: String = ApplicationDefaultErrorMessage.NETWORK_UNKNOWN_ERROR }

    constructor(): super() { Log.debug(defaultMessage) }
    constructor(message: String = defaultMessage): super(message) { Log.debug(message) }
    constructor(message: String = defaultMessage, cause: Throwable): super(message, cause) { Log.debug(message, cause) }
    constructor(cause: Throwable): super(cause) { Log.debug(defaultMessage, cause) }

}