package io.github.lucastavar3s.coupon.domain.exception;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

	public CouponNotFoundException(UUID id) {
		super("Cupom não encontrado: " + id);
	}

}
