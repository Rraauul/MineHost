package com.example.APIGateWay.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
public class RestAPI1 {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://servicerest1:8080";

    public RestAPI1(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private ResponseEntity<String> safeCall(Supplier<ResponseEntity<String>> request) {
        try {
            return request.get();
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erreur réseau : " + ex.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> createWorld(String name, String template, Integer ram, Integer userId,
            Integer serverId) {
        String url = baseUrl
                + "/createWorld?name={name}&template={template}&ram={ram}&userId={userId}&serverId={serverId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class, name, template, ram,
                userId, serverId));
    }

    public ResponseEntity<String> startWorld(Integer worldId) {
        String url = baseUrl + "/startWorld?worldId={worldId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class, worldId));
    }

    public ResponseEntity<String> stopWorld(Integer worldId) {
        String url = baseUrl + "/stopWorld?worldId={worldId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class, worldId));
    }

    public ResponseEntity<String> deleteWorld(Integer worldId) {
        String url = baseUrl + "/deleteWorld?worldId={worldId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class, worldId));
    }

    public ResponseEntity<String> getInfoWorld(Integer worldId) {
        String url = baseUrl + "/infoWorld?worldId={worldId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class, worldId));
    }

    public ResponseEntity<byte[]> downloadLog(Integer worldId) {
        String url = baseUrl + "/downloadLog?worldId={worldId}";
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class, worldId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(response.getHeaders().getContentType());
            String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (disposition != null) {
                headers.setContentDisposition(ContentDisposition.parse(disposition));
            }
            headers.setContentLength(response.getBody() != null ? response.getBody().length : 0);

            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(null);
        } catch (RestClientException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    public ResponseEntity<String> getServerInfo(Integer serverId) {
        String url = baseUrl + "/infoServer?serverId={serverId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class, serverId));
    }

    public ResponseEntity<String> getWorldsByUserId(Integer userIdByUsername) {
        String url = baseUrl + "/getWorldsByUserId?userId={userId}";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class, userIdByUsername));
    }

    public ResponseEntity<String> getTemplates() {
        String url = baseUrl + "/getTemplates";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class));
    }
}
