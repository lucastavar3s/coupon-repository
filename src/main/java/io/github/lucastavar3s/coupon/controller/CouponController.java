package io.github.lucastavar3s.coupon.controller;

import java.time.Clock;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.lucastavar3s.coupon.domain.Coupon;
import io.github.lucastavar3s.coupon.dto.CouponResponse;
import io.github.lucastavar3s.coupon.dto.CreateCouponRequest;
import io.github.lucastavar3s.coupon.mapper.CouponMapper;
import io.github.lucastavar3s.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
@Tag(name = "coupon")
public class CouponController {

	private final CouponService couponService;
	private final Clock clock;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um cupom")
	@ApiResponse(responseCode = "201", description = "Cupom criado")
	@ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "422", description = "Regra de negócio violada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	public CouponResponse create(@Valid @RequestBody CreateCouponRequest request) {
		Coupon coupon = couponService.create(request.code(), request.description(), request.discountValue(),
				request.expirationDate(), Boolean.TRUE.equals(request.published()));
		return CouponMapper.toResponse(coupon, clock.instant());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um cupom pelo identificador")
	@ApiResponse(responseCode = "200", description = "Cupom encontrado")
	@ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Cupom não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	public CouponResponse findById(@PathVariable UUID id) {
		return CouponMapper.toResponse(couponService.findById(id), clock.instant());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui um cupom de forma lógica")
	@ApiResponse(responseCode = "204", description = "Cupom excluído")
	@ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Cupom não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "409", description = "Cupom já excluído", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	public void delete(@PathVariable UUID id) {
		couponService.delete(id);
	}

}
