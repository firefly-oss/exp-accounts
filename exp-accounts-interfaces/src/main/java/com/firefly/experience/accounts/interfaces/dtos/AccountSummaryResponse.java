package com.firefly.experience.accounts.interfaces.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account summary for experience layer")
public class AccountSummaryResponse {

    @Schema(description = "Account unique identifier")
    private UUID accountId;

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Account type")
    private String accountType;

    @Schema(description = "Account status")
    private String status;

    @Schema(description = "Available balance")
    private BigDecimal availableBalance;

    @Schema(description = "Current balance")
    private BigDecimal currentBalance;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Account holder name")
    private String holderName;
}
