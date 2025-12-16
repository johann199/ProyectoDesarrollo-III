package com.sgl.backend.service;

import com.sgl.backend.exception.SglException;
import com.sgl.backend.dto.OpacUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpacService {

    private final RestTemplate restTemplate;

    @Value("${opac.base-url}")
    private String opacBaseUrl;

    public OpacUserInfo fetchUserInfo(String code) {
        try {
            String url = opacBaseUrl + "/student/preview/" + code;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<OpacUserInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OpacUserInfo.class);

            if (response.getBody() == null) {
                throw new SglException("OPAC user info failed: No response body", HttpStatus.BAD_GATEWAY);
            }

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            throw new SglException("OPAC user not found: " + code, e, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new SglException("Unauthorized access to OPAC service", e, HttpStatus.UNAUTHORIZED);
        } catch (HttpClientErrorException e) {
            throw new SglException("OPAC user info failed: Invalid code " + code, e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new SglException("OPAC user info error: " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
