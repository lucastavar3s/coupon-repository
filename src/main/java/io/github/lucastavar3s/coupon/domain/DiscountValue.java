package io.github.lucastavar3s.coupon.domain;

import java.math.BigDecimal;
import java.util.Objects;

import io.github.lucastavar3s.coupon.domain.exception.InvalidCouponException;

public final class DiscountValue {

	public static final BigDecimal MINIMUM = new BigDecimal("0.5");

	private final BigDecimal amount;

	private DiscountValue(BigDecimal amount) {
		this.amount = amount;
	}

	public static DiscountValue of(BigDecimal value) {
		if (value == null || value.compareTo(MINIMUM) < 0) {
			throw new InvalidCouponException("O valor de desconto deve ser maior ou igual a 0,5");
		}
		return new DiscountValue(value);
	}

	public BigDecimal amount() {
		return amount;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DiscountValue discountValue)) {
			return false;
		}
		return amount.compareTo(discountValue.amount) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount.stripTrailingZeros());
	}

}
