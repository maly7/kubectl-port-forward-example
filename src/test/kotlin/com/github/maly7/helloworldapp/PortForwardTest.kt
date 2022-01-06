package com.github.maly7.helloworldapp

import io.kubernetes.client.extended.kubectl.Kubectl
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.ClientBuilder
import okhttp3.Protocol
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.util.SocketUtils
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.concurrent.TimeUnit

import org.hamcrest.Matchers.equalTo


class PortForwardTest {
    private val webClient: WebClient = WebClient.builder().build()
    private val localPort: Int = SocketUtils.findAvailableTcpPort()

    @Test
    fun `Fetch the hello world endpoint using KubectlPortForward`() {
        // Depends on running:
        // ./gradlew bootBuildImage
        // kind load docker-image docker.io/library/helloworld-app:0.0.1-SNAPSHOT
        // kubectl run helloworld --image=docker.io/library/helloworld-app:0.0.1-SNAPSHOT --port=8080

        val apiClient = initClientApi()
        val portForward = Kubectl.portforward()
            .apiClient(apiClient)
            .namespace("default")
            .name("helloworld")
            .ports(localPort, 8080)

        Thread {
            portForward.execute()
        }.start()

        println("Forwarding on port ${localPort}!")

        Thread.sleep(30000) // Open http://localhost/$port in browser or curl now works, but causes the following assertion to fail

        await()
            .pollDelay(5, TimeUnit.SECONDS)
            .atMost(30, TimeUnit.SECONDS)
            .until(this::fetchHello, equalTo("Hello, world!"))

        portForward.shutdown()
    }


    private fun initClientApi(): ApiClient? {
        val apiClient = ClientBuilder.defaultClient()
        apiClient.httpClient = apiClient.httpClient
            .newBuilder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .readTimeout(Duration.ZERO)
            .pingInterval(1, TimeUnit.MINUTES)
            .build()
        Configuration.setDefaultApiClient(apiClient)
        return apiClient
    }

    fun fetchHello() = webClient.get()
        .uri("http://localhost:${localPort}/hello")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()

}