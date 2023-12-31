package hookahcat.com.ua.configuration;

import static hookahcat.com.ua.common.Constants.WEB_CLIENT_BUFFER_SIZE;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@RequiredArgsConstructor
@Configuration
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
}
