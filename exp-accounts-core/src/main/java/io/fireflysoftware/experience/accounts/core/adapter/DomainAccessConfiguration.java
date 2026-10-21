/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.accounts.core.adapter;

import io.fireflysoftware.experience.accounts.core.ports.AccessDecision;
import io.fireflysoftware.experience.accounts.core.ports.AccessPort;
import io.fireflysoftware.experience.accounts.core.ports.AccessRequest;
import io.fireflysoftware.experience.accounts.core.ports.SecurityCenterClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Calls domain-core-security-center permission check. Selected with
 * {@code firefly.experience.access.adapter=domain}.
 */
@Configuration
@ConditionalOnProperty(name = "firefly.experience.access.adapter", havingValue = "domain")
public class DomainAccessConfiguration {

    @Bean
    public SecurityCenterClient securityCenterClient(
            @Value("${api-configuration.domain-platform.security-center.base-path:http://localhost:8094}")
            String basePath) {
        WebClient webClient = WebClient.builder().baseUrl(basePath).build();
        return new HttpSecurityCenterClient(webClient);
    }

    @Bean
    public AccessPort domainAccessPort(SecurityCenterClient securityCenterClient) {
        return new DomainAccessAdapter(securityCenterClient);
    }

    static final class HttpSecurityCenterClient implements SecurityCenterClient {

        private final WebClient webClient;

        HttpSecurityCenterClient(WebClient webClient) {
            this.webClient = webClient;
        }

        @Override
        public Mono<AccessDecision> check(AccessRequest request) {
            UUID partyId = request.actorId();
            UUID productId = request.resourceId() == null ? partyId : request.resourceId();
            return webClient.get()
                    .uri(builder -> builder.path("/api/v1/sessions/permission-check")
                            .queryParam("partyId", partyId)
                            .queryParam("productId", productId)
                            .queryParam("actionType", request.action().name())
                            .queryParam("resourceType", request.resourceType())
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .map(allowed -> Boolean.TRUE.equals(allowed)
                            ? AccessDecision.permit("SECURITY_CENTER_ALLOW")
                            : AccessDecision.denied("SECURITY_CENTER_DENIED"));
        }
    }
}
