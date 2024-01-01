package hookahcat.com.ua.configuration;

import static hookahcat.com.ua.common.Constants.WEB_CLIENT_BUFFER_SIZE;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class CustomWebClientConfiguration {

    @Bean
    public WebClient webClientConfiguration(WebClientProperties webClientProperties) {
        HttpClient client = HttpClient.create(createCustomProvider(webClientProperties))
            .responseTimeout(Duration.ofSeconds(webClientProperties.getHttpClientResponseTimeout()))
            .compress(true);

        return WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(WEB_CLIENT_BUFFER_SIZE))
                .build())
            .clientConnector(new ReactorClientHttpConnector(client))
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    private ConnectionProvider createCustomProvider(WebClientProperties webClientProperties) {
        return ConnectionProvider.builder("customConnectionProvider")
            .maxIdleTime(Duration.ofSeconds(webClientProperties.getConnectionProviderMaxIdleTime()))
            .maxLifeTime(Duration.ofSeconds(webClientProperties.getConnectionProviderMaxLifeTime()))
            .pendingAcquireTimeout(Duration.ofSeconds(
                webClientProperties.getConnectionProviderPendingAcquireTimeout()))
            .evictInBackground(Duration.ofSeconds(
                webClientProperties.getConnectionProviderEvictInBackgroundTimeout()))
            .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                values.forEach(value -> log.debug("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders()
                .forEach((name, values) -> values.forEach(value -> log.debug("{}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }
}
