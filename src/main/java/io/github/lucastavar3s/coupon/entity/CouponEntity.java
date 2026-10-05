package io.github.lucastavar3s.coupon.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
public class CouponEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 6)
	private String code;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private BigDecimal discountValue;

	@Column(nullable = false)
	private Instant expirationDate;

	@Column(nullable = false)
	private boolean published;

	@Column(nullable = false)
	private boolean redeemed;

	private Instant deletedAt;

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CouponEntity entity)) {
			return false;
		}
		return id != null && id.equals(entity.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
