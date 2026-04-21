package com.firefly.experience.accounts.interfaces.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to open a new account")
public class OpenAccountRequest {

    @NotNull
    @Schema(description = "Party (customer) identifier")
    private UUID partyId;

    @NotNull
    @Schema(description = "Product identifier")
    private UUID productId;

    @Schema(description = "Product catalog identifier")
    private UUID productCatalogId;

    @Schema(description = "Role in contract identifier")
    private UUID roleInContractId;

    @NotBlank
    @Schema(description = "Account type code")
    private String accountType;

    @NotBlank
    @Schema(description = "Currency code (ISO 4217)")
    private String currency;

    @Schema(description = "Branch identifier")
    private UUID branchId;
}
