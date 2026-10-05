package io.github.lucastavar3s.coupon.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.lucastavar3s.coupon.domain.Coupon;
import io.github.lucastavar3s.coupon.domain.exception.CouponNotFoundException;
import io.github.lucastavar3s.coupon.mapper.CouponMapper;
import io.github.lucastavar3s.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

	private final CouponRepository couponRepository;
	private final Clock clock;

	public Coupon create(String code, String description, BigDecimal discountValue, Instant expirationDate,
			boolean published) {
		Coupon coupon = Coupon.create(code, description, discountValue, expirationDate, published, clock.instant());
		return CouponMapper.toDomain(couponRepository.save(CouponMapper.toEntity(coupon)));
	}

	@Transactional(readOnly = true)
	public Coupon findById(UUID id) {
		return couponRepository.findById(id)
				.map(CouponMapper::toDomain)
				.orElseThrow(() -> new CouponNotFoundException(id));
	}

	public void delete(UUID id) {
		Coupon coupon = findById(id);
		coupon.delete(clock.instant());
		couponRepository.save(CouponMapper.toEntity(coupon));
	}

}
