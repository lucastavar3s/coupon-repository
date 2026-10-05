package io.github.lucastavar3s.coupon.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.github.lucastavar3s.coupon.domain.CouponStatus;

public record CouponResponse(
		UUID id,
		String code,
		String description,
		BigDecimal discountValue,
		Instant expirationDate,
		CouponStatus status,
		boolean published,
		boolean redeemed) {
}
