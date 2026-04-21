package com.firefly.experience.accounts.core.opening.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateAccountOpeningCommand {
    private UUID customerId;
    private UUID productId;
    private UUID productCatalogId;
    private String accountType;
    private String currency;
    private UUID branchId;
    private String email;
    private String phone;
}
