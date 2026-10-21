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


package io.fireflysoftware.experience.accounts.core.ports;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Runs a domain call only after {@link AccessPort} allows it.
 * The supplier is not invoked when access is denied.
 */
public final class AccessGuard {

    private AccessGuard() {
    }

    public static <T> Mono<T> require(AccessPort port, AccessRequest request, Supplier<Mono<T>> domainCall) {
        return port.check(request).flatMap(decision -> decision.allowed()
                ? domainCall.get()
                : Mono.error(new AccessDeniedException(decision.reason())));
    }

    public static <T> Flux<T> requireMany(AccessPort port, AccessRequest request, Supplier<Flux<T>> domainCall) {
        return port.check(request).flatMapMany(decision -> decision.allowed()
                ? domainCall.get()
                : Flux.error(new AccessDeniedException(decision.reason())));
    }
}
