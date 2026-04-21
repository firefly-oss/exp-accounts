package com.firefly.experience.accounts.interfaces.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after opening an account")
public class OpenAccountResponse {

    @Schema(description = "Newly created account identifier")
    private UUID accountId;

    @Schema(description = "Contract identifier")
    private UUID contractId;

    @Schema(description = "Execution identifier for tracking")
    private String executionId;

    @Schema(description = "Operation status")
    private String status;
}
