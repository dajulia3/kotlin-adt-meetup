package com.djulia.transactions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

fun makeDefaultObjectMapper() : ObjectMapper {
    return ObjectMapper().registerKotlinModule()
}