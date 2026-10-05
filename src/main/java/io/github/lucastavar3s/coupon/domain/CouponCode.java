package io.github.lucastavar3s.coupon.domain;

import java.util.Objects;
import java.util.regex.Pattern;

import io.github.lucastavar3s.coupon.domain.exception.InvalidCouponException;

public final class CouponCode {

	public static final int LENGTH = 6;

	private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

	private final String value;

	private CouponCode(String value) {
		this.value = value;
	}

	public static CouponCode of(String rawCode) {
		if (rawCode == null || rawCode.isBlank()) {
			throw new InvalidCouponException("O código do cupom é obrigatório");
		}
		String normalized = NON_ALPHANUMERIC.matcher(rawCode).replaceAll("");
		if (normalized.length() != LENGTH) {
			throw new InvalidCouponException(
					"O código do cupom deve conter 6 caracteres alfanuméricos após a remoção dos caracteres especiais");
		}
		return new CouponCode(normalized);
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CouponCode couponCode)) {
			return false;
		}
		return value.equals(couponCode.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

}
