package com.reliaquest.api.config;

import io.netty.channel.ChannelOption;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}")
    private String mockEmployeeApiBaseUrl;

    @Value("${employee.api.timeout:5000}")
    private int timeoutMs;

    @Value("${employee-v1.api.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Value("${employee-v1.api.retry.initial-delay:500}")
    private long initialDelayMs;

    @Value("${employee-v1.api.retry.max-backoff:10000}")
    private long maxDelayMs;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs));

        return WebClient.builder()
                .baseUrl(mockEmployeeApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public Retry defaultRetrySpec() {
        return Retry.backoff(maxRetryAttempts, Duration.ofMillis(initialDelayMs))
                .maxBackoff(Duration.ofMillis(maxDelayMs))
                .jitter(0.5)
                .filter(this::isRetryableException)
                .doBeforeRetry(retrySignal -> log.warn(
                        "Retrying request (attempt {}/{}) due to: {}",
                        retrySignal.totalRetries() + 1,
                        maxRetryAttempts,
                        retrySignal.failure().getMessage()));
    }

    private boolean isRetryableException(Throwable throwable) {

        if (throwable instanceof WebClientResponseException ex) {
            log.debug("Too many requests retrying ");
            return ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
        }
        return throwable instanceof ConnectException || throwable instanceof TimeoutException;
    }
}
