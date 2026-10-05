package io.github.lucastavar3s.coupon.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.github.lucastavar3s.coupon.domain.exception.CouponAlreadyDeletedException;
import io.github.lucastavar3s.coupon.domain.exception.InvalidCouponException;
import lombok.Getter;

@Getter
public class Coupon {

	public static final int DESCRIPTION_MAX_LENGTH = 4000;

	private final UUID id;
	private final CouponCode code;
	private final String description;
	private final DiscountValue discountValue;
	private final Instant expirationDate;
	private final boolean published;
	private final boolean redeemed;
	private Instant deletedAt;

	private Coupon(UUID id, CouponCode code, String description, DiscountValue discountValue, Instant expirationDate,
			boolean published, boolean redeemed, Instant deletedAt) {
		this.id = id;
		this.code = code;
		this.description = description;
		this.discountValue = discountValue;
		this.expirationDate = expirationDate;
		this.published = published;
		this.redeemed = redeemed;
		this.deletedAt = deletedAt;
	}

	public static Coupon create(String rawCode, String description, BigDecimal discountValue, Instant expirationDate,
			boolean published, Instant now) {
		if (description == null || description.isBlank()) {
			throw new InvalidCouponException("A descrição do cupom é obrigatória");
		}
		String normalizedDescription = description.trim();
		if (normalizedDescription.length() > DESCRIPTION_MAX_LENGTH) {
			throw new InvalidCouponException("A descrição do cupom deve ter no máximo 4000 caracteres");
		}
		if (expirationDate == null || expirationDate.isBefore(now)) {
			throw new InvalidCouponException("A data de expiração não pode estar no passado");
		}
		return new Coupon(UUID.randomUUID(), CouponCode.of(rawCode), normalizedDescription, DiscountValue.of(discountValue),
				expirationDate, published, false, null);
	}

	public static Coupon restore(UUID id, String code, String description, BigDecimal discountValue,
			Instant expirationDate, boolean published, boolean redeemed, Instant deletedAt) {
		return new Coupon(id, CouponCode.of(code), description, DiscountValue.of(discountValue), expirationDate,
				published, redeemed, deletedAt);
	}

	public void delete(Instant now) {
		if (this.deletedAt != null) {
			throw new CouponAlreadyDeletedException();
		}
		this.deletedAt = now;
	}

	public CouponStatus statusAt(Instant now) {
		if (this.deletedAt != null) {
			return CouponStatus.DELETED;
		}
		if (this.expirationDate.isBefore(now)) {
			return CouponStatus.INACTIVE;
		}
		return CouponStatus.ACTIVE;
	}

}
