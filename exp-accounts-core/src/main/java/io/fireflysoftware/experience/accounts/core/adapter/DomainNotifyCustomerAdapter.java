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

import io.fireflysoftware.domain.common.notifications.sdk.api.NotificationsApi;
import io.fireflysoftware.domain.common.notifications.sdk.model.SendNotificationCommand;
import io.fireflysoftware.experience.accounts.core.ports.NotifyCustomer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "firefly.experience.accounts.ports.notify-customer", havingValue = "domain", matchIfMissing = true)
public class DomainNotifyCustomerAdapter implements NotifyCustomer {

    private static final String CHANNEL = "AUTO";

    private final NotificationsApi notificationsApi;

    public DomainNotifyCustomerAdapter(NotificationsApi notificationsApi) {
        this.notificationsApi = notificationsApi;
    }

    @Override
    public Mono<Void> send(UUID partyId, String templateCode, String subject, String email, String phone) {
        SendNotificationCommand command = new SendNotificationCommand()
                .partyId(partyId)
                .channel(CHANNEL)
                .templateCode(templateCode)
                .subject(subject);
        if (email != null) {
            command.recipientEmail(email);
        }
        if (phone != null) {
            command.recipientPhone(phone);
        }
        return notificationsApi.sendNotification(command, UUID.randomUUID().toString()).then();
    }
}
