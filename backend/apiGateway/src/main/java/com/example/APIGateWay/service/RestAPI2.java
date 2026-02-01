package com.example.APIGateWay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class RestAPI2 {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://servicerest2:8080";

    public RestAPI2(RestTemplate restTemplate) {
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

    public ResponseEntity<String> getInfoOffers() {
        String url = baseUrl + "/infoOffers";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class));
    }

    public ResponseEntity<String> getOfferByClient(Integer pkUser) {
        String url = baseUrl + "/OfferByClient?pkUser=" + pkUser;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class));
    }

    public ResponseEntity<String> addUser(String nom, Integer nbEmeraudes, String motDePasse, Integer FK_abo) {
        final String url;
        if (FK_abo != null) {
            url = baseUrl + "/addUser?nom=" + nom + "&nbEmeraudes=" + nbEmeraudes +
                    "&motDePasse=" + motDePasse + "&FK_abo=" + FK_abo;
        } else {
            url = baseUrl + "/addUser?nom=" + nom + "&nbEmeraudes=" + nbEmeraudes +
                    "&motDePasse=" + motDePasse;
        }
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> addEmeraude(Integer pkUser, Integer emeraudes) {
        String url = baseUrl + "/addEmeraude?pkUser=" + pkUser + "&emeraudes=" + emeraudes;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> acheterAbo(Integer pkUser, Integer Fkabonnement) {
        String url = baseUrl + "/acheterAbo?pkUser=" + pkUser + "&Fkabonnement=" + Fkabonnement;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> login(String name, String motDePasse) {
        String url = baseUrl + "/login";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("motDePasse", motDePasse);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> getAllUsers() {
        String url = baseUrl + "/getAllUsers";
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class));
    }

    public ResponseEntity<String> changeName(Integer PK, String newName) {
        String url = baseUrl + "/changeName?PK=" + PK + "&newName=" + newName;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.PUT, entity, String.class));
    }

    public ResponseEntity<String> changePassword(Integer PK, String password) {
        String url = baseUrl + "/changePassword?PK=" + PK + "&password=" + password;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.PUT, entity, String.class));
    }

    public ResponseEntity<String> addAbo(String name, Integer prix, Integer stockage, Integer ram) {
        String url = baseUrl + "/addAbo?name=" + name + "&prix=" + prix + "&stockage=" + stockage + "&ram=" + ram;
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());
        return safeCall(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> getUserByUsername(String username) {
        String url = baseUrl + "/getUserByUsername?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        HttpEntity<?> entity = new HttpEntity<>(jsonHeaders());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'appel au service: " + e.getMessage());
        }
    }

    public Integer getUserIdByUsername(String username) {
        ResponseEntity<String> response = getUserByUsername(username);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("id").asInt();
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la lecture de la réponse JSON", e);
            }
        } else {
            throw new RuntimeException("Utilisateur introuvable ou erreur dans la réponse");
        }
    }
}