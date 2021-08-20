/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.integration.kundportalen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.inera.intyg.webcert.integration.kundportalen.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.kundportalen.enumerations.AuthenticationMethodEnum;

@Service
public class SubscriptionRestServiceImpl implements SubscriptionRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionRestServiceImpl.class);

    @Value("${kundportalen.access.token}")
    private String kundportalenAccessToken;

    @Value("${kundportalen.subscriptions.url}")
    private String kundportalenSubscriptionServiceUrl;

    @Value("${kundportalen.subscriptions.service}")
    private String kundportalenSubscriptionService;

    @Value("#{${kundportalen.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${kundportalen.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    private static final ParameterizedTypeReference<List<OrganizationResponse>> LIST_ORGANIZATION_RESPONSE
        = new ParameterizedTypeReference<>() { };

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionRestServiceImpl(@Qualifier("subscriptionServiceRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> getMissingSubscriptions(Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var organizationResponse = getSubscriptionServiceResponse(organizationNumberHsaIdMap.keySet());
        final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody());
        return getCareProvidersMissingSubscription(organizationInfo, organizationNumberHsaIdMap, authMethod);
    }

    @Override
    public boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        final var organizationResponse = getSubscriptionServiceResponse(Set.of(organizationNumber));
        final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody());
        return missingSubscription(organizationInfo.get(0).getServiceCodes(), AuthenticationMethodEnum.ELEG);
    }

    private ResponseEntity<List<OrganizationResponse>> getSubscriptionServiceResponse(Set<String> organizationNumbers) {
        final var requestEntity = getRequestEntity(organizationNumbers);
        final var requestUrl = getRequestUrlWithParams();
        return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, LIST_ORGANIZATION_RESPONSE);
    }

    private HttpEntity<String> getRequestEntity(Set<String> organizationNumbers) {
        final var organizationNumbersJson = getOrganizationNumbersJson(organizationNumbers);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", kundportalenAccessToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(Objects.requireNonNull(organizationNumbersJson), httpHeaders);
    }

    private String getOrganizationNumbersJson(Set<String> organizationNumbers) {
        try {
            return objectMapper.writeValueAsString(organizationNumbers);
        } catch (JsonProcessingException e) {
            LOG.error("Failure parsing organization numbers as json for kunpdportalen subscription service request.", e);
            return null;
        }
    }

    private String getRequestUrlWithParams() {
        return UriComponentsBuilder.fromHttpUrl(kundportalenSubscriptionServiceUrl)
            .queryParam("service", kundportalenSubscriptionService)
            .toUriString();
    }

    private List<String> getCareProvidersMissingSubscription(List<OrganizationResponse> organizations,
        Map<String, List<String>> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var careProvidersMissingSubscription = new ArrayList<String>();

        for (var organization : organizations) {
            final var serviceCodes = organization.getServiceCodes();
            if (missingSubscription(serviceCodes, authMethod)) {
                careProvidersMissingSubscription.addAll(organizationNumberHsaIdMap.get(organization.getOrganizationNumber()));
            }
        }
        return careProvidersMissingSubscription;
    }

    private boolean missingSubscription(List<String> activeServiceCodes, AuthenticationMethodEnum authMethod) {
        if (activeServiceCodes.isEmpty()) {
            return true;
        }
        if (authMethod == AuthenticationMethodEnum.ELEG) {
            return activeServiceCodes.stream().noneMatch(serviceCode -> elegServiceCodes.contains(serviceCode));
        }
        return activeServiceCodes.stream().noneMatch(serviceCode -> sithsServiceCodes.contains(serviceCode));
    }
}
