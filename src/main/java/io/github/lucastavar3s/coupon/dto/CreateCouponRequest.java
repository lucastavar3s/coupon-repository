package io.github.lucastavar3s.coupon.dto;

import java.math.BigDecimal;
import java.time.Instant;

import io.github.lucastavar3s.coupon.domain.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um cupom")
public record CreateCouponRequest(
		@NotBlank(message = "O código é obrigatório")
		@Schema(example = "ABC-123")
		String code,

		@NotBlank(message = "A descrição é obrigatória")
		@Size(max = Coupon.DESCRIPTION_MAX_LENGTH, message = "A descrição do cupom deve ter no máximo 4000 caracteres")
		String description,

		@NotNull(message = "O valor de desconto é obrigatório")
		@Schema(example = "0.8")
		BigDecimal discountValue,

		@NotNull(message = "A data de expiração é obrigatória")
		Instant expirationDate,

		@Schema(description = "Quando omitido, o cupom nasce despublicado", defaultValue = "false")
		Boolean published) {
}
