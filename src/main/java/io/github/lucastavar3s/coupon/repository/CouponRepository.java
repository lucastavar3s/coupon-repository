package io.github.lucastavar3s.coupon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.lucastavar3s.coupon.entity.CouponEntity;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
}
