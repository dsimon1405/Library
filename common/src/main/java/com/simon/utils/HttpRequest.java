package com.simon.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.exception.ExistsException;
import com.simon.exception.ServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class HttpRequest {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpRequest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> request(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity,
                                         Class<T> responseType, Object... uriVariables) {
        try {
            return checkBody(restTemplate.exchange(url, httpMethod, httpEntity, responseType, uriVariables), responseType);
        } catch (HttpStatusCodeException ex) {
            throw extractServiceException(ex);
        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage());
        }
    }

    public <T> ResponseEntity<T> request(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity,
                                         Class<T> responseType, Map<String, ?> uriVariables) {
        try {
            return checkBody(restTemplate.exchange(url, httpMethod, httpEntity, responseType, uriVariables), responseType);
        } catch (HttpStatusCodeException ex) {
            throw extractServiceException(ex);
        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage());
        }
    }

    public <T> ResponseEntity<T> request(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity,
                                         ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
        try {
            return uriVariables == null ? restTemplate.exchange(url, httpMethod, httpEntity, responseType)
                    : restTemplate.exchange(url, httpMethod, httpEntity, responseType, uriVariables);
        } catch (HttpStatusCodeException ex) {
            throw extractServiceException(ex);
        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage());
        }
    }


    private ServiceException extractServiceException(HttpStatusCodeException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            JsonNode json = objectMapper.readTree(responseBody);

            JsonNode errorsNode = json.get("errors");
            if (errorsNode == null) return new ServiceException(ex.getMessage());
            if (errorsNode.isObject()) return new ServiceException(errorsNode.toString());
            if (errorsNode.isTextual()) return new ServiceException(errorsNode.asText());

            return new ServiceException(errorsNode.toString());

        } catch (Exception parseEx) {
            return new ServiceException(ex.getMessage());
        }
    }

    private <T> ResponseEntity<T> checkBody(ResponseEntity<T> re, Class<T> responseType) {
        if (!Void.class.equals(responseType) && re.getBody() == null)
            throw new ExistsException("There's no response body");
        return re;
    }
}
