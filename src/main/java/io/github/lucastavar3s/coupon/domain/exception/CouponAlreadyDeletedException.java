package io.github.lucastavar3s.coupon.domain.exception;

public class CouponAlreadyDeletedException extends RuntimeException {

	public CouponAlreadyDeletedException() {
		super("Não é possível excluir um cupom que já foi excluído");
	}

}
