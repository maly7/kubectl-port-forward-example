package com.github.maly7.helloworldapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HelloworldController {

    @GetMapping("/hello")
    fun hello(): Mono<String> = Mono.just("Hello, world!")
}