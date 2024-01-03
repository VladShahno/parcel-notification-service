package ua.com.hookahcat.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import io.micrometer.common.util.StringUtils;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import ua.com.hookahcat.reststarter.exception.RestException;

@RequiredArgsConstructor
@Slf4j
public abstract class ProxyService {

    private final WebClient webClient;

    protected <T> T get(
        String uri,
        Map<String, String> headers,
        Class<T> tClass,
        String exceptionMessage
    ) {
        return webClient.get()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, exceptionMessage))
            .bodyToMono(tClass)
            .block();
    }

    protected <T> T get(
        String uri,
        Map<String, String> headers,
        Class<T> tClass
    ) {
        return get(uri, headers, tClass, EMPTY);
    }

    protected <T> T get(
        String uri,
        Map<String, String> headers,
        Class<T> tClass,
        MultiValueMap<String, String> queryParams
    ) {
        String uriWithParameters = UriComponentsBuilder.fromUriString(uri)
            .queryParams(queryParams)
            .toUriString();
        return webClient.get()
            .uri(uriWithParameters)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(tClass)
            .block();
    }

    protected <T> T get(String uri, Map<String, String> headers,
        ParameterizedTypeReference<T> typeReference) {
        return webClient.get()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(typeReference)
            .block();
    }

    protected <T> void put(
        String uri,
        Map<String, String> headers, T tBody) {
        webClient.put()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .bodyValue(tBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .toBodilessEntity()
            .block();
    }

    protected <T> void patch(
        String uri,
        Map<String, String> headers,
        T tBody) {
        webClient.patch()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .bodyValue(tBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .toBodilessEntity()
            .block();
    }

    protected <T, S> T post(
        String uri,
        Map<String, String> headers,
        S tBody,
        Class<T> tClass) {
        return webClient.post()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(tClass)
            .block();
    }

    protected <T, S> T post(
        String uri,
        Map<String, String> headers,
        S tBody,
        Class<T> tClass,
        MediaType mediaType) {
        return webClient.post()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .contentType(mediaType)
            .bodyValue(tBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(tClass)
            .block();
    }

    protected <T, S> S post(
        String uri,
        Map<String, String> headers,
        T tBody,
        ParameterizedTypeReference<S> typeReference) {
        return post(uri, headers, tBody, typeReference, 0);
    }

    protected <T, S> S post(
        String uri,
        Map<String, String> headers,
        T tBody,
        ParameterizedTypeReference<S> typeReference,
        long timeout) {
        return webClient.post()
            .uri(uri)
            .httpRequest(httpRequest -> {
                if (timeout > 0) {
                    HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofSeconds(timeout));
                }
            })
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(typeReference)
            .block();
    }

    protected <T> T delete(
        String uri,
        Map<String, String> headers,
        Class<T> tClass) {
        return webClient.delete()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(tClass)
            .block();
    }

    protected <S> S delete(
        String uri,
        Map<String, String> headers,
        ParameterizedTypeReference<S> typeReference) {
        return webClient.delete()
            .uri(uri)
            .headers(this::buildHeadersForRequest)
            .headers(customHeaders -> headers.forEach(customHeaders::add))
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                clientResponse -> setErrorToResponse(clientResponse, EMPTY))
            .bodyToMono(typeReference)
            .block();
    }

    protected Mono<Throwable> setErrorToResponse(ClientResponse response, String exceptionMessage) {
        return response
            .bodyToMono(Exception.class)
            .switchIfEmpty(Mono.error(new RestException((HttpStatus) response.statusCode())))
            .flatMap(error -> {
                var message = StringUtils.isNotEmpty(exceptionMessage)
                    ? exceptionMessage
                    : error.getMessage();
                log.error(response.statusCode() + message);
                return Mono.error(new RestException((HttpStatus) response.statusCode(), message));
            });
    }

    protected String buildUri(String serviceRequestApi, Map<String, String> pathParams) {
        return UriComponentsBuilder.fromUriString(serviceRequestApi)
            .buildAndExpand(pathParams)
            .toUriString();
    }

    protected String buildUri(
        String serviceRequestApi, Map<String, String> pathParams,
        MultiValueMap<String, String> queryParams) {
        return UriComponentsBuilder.fromUriString(serviceRequestApi)
            .queryParams(queryParams)
            .buildAndExpand(pathParams)
            .toUriString();
    }

    protected abstract void buildHeadersForRequest(HttpHeaders httpHeaders);
}
