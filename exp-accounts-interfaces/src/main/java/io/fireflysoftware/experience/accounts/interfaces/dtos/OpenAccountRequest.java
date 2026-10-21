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


package io.fireflysoftware.experience.accounts.interfaces.dtos;

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
