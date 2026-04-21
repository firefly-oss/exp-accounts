package com.firefly.experience.accounts.core.opening.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureFundingSourceCommand {
    private String fundingMethod;
    private String externalAccountNumber;
    private String externalBankCode;
    private BigDecimal initialDepositAmount;
}
