package io.jn.knodeflow.port

data class PortValidation(
    val validator: ((Any?) -> Boolean)? = null,
    val errorMessage: String = "Invalid value",
    val acceptedTypes: Set<String> = emptySet()
)