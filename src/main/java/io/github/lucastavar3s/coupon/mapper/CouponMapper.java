package io.github.lucastavar3s.coupon.mapper;

import java.time.Instant;

import io.github.lucastavar3s.coupon.domain.Coupon;
import io.github.lucastavar3s.coupon.dto.CouponResponse;
import io.github.lucastavar3s.coupon.entity.CouponEntity;

public final class CouponMapper {

	private CouponMapper() {
	}

	public static CouponEntity toEntity(Coupon coupon) {
		CouponEntity entity = new CouponEntity();
		entity.setId(coupon.getId());
		entity.setCode(coupon.getCode().value());
		entity.setDescription(coupon.getDescription());
		entity.setDiscountValue(coupon.getDiscountValue().amount());
		entity.setExpirationDate(coupon.getExpirationDate());
		entity.setPublished(coupon.isPublished());
		entity.setRedeemed(coupon.isRedeemed());
		entity.setDeletedAt(coupon.getDeletedAt());
		return entity;
	}

	public static Coupon toDomain(CouponEntity entity) {
		return Coupon.restore(entity.getId(), entity.getCode(), entity.getDescription(), entity.getDiscountValue(),
				entity.getExpirationDate(), entity.isPublished(), entity.isRedeemed(), entity.getDeletedAt());
	}

	public static CouponResponse toResponse(Coupon coupon, Instant now) {
		return new CouponResponse(coupon.getId(), coupon.getCode().value(), coupon.getDescription(),
				coupon.getDiscountValue().amount(),
				coupon.getExpirationDate(), coupon.statusAt(now), coupon.isPublished(), coupon.isRedeemed());
	}

}
