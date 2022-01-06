package com.github.maly7.helloworldapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloworldAppApplication

fun main(args: Array<String>) {
	runApplication<HelloworldAppApplication>(*args)
}
