package io.github.lucastavar3s.coupon.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.lucastavar3s.coupon.domain.exception.CouponAlreadyDeletedException;
import io.github.lucastavar3s.coupon.domain.exception.InvalidCouponException;

@Tag("unit")
class CouponTest {

	private final Instant now = Instant.parse("2026-10-05T18:00:00Z");

	@Test
	void createsCouponRemovingSpecialCharacters() {
		Coupon coupon = Coupon.create("ABC-123", "  Boas-vindas  ", new BigDecimal("0.8"),
				now.plus(1, ChronoUnit.DAYS), true, now);

		assertThat(coupon.getCode().value()).isEqualTo("ABC123");
		assertThat(coupon.getDescription()).isEqualTo("Boas-vindas");
		assertThat(coupon.getDiscountValue().amount()).isEqualByComparingTo("0.8");
		assertThat(coupon.isPublished()).isTrue();
		assertThat(coupon.isRedeemed()).isFalse();
		assertThat(coupon.getDeletedAt()).isNull();
		assertThat(coupon.statusAt(now)).isEqualTo(CouponStatus.ACTIVE);
	}

	@Test
	void createsUnpublishedCouponAsActive() {
		Coupon coupon = Coupon.create("abc 123", "Desconto", new BigDecimal("0.50"), now.plusSeconds(1), false, now);

		assertThat(coupon.getCode().value()).isEqualTo("abc123");
		assertThat(coupon.isPublished()).isFalse();
		assertThat(coupon.statusAt(now)).isEqualTo(CouponStatus.ACTIVE);
	}

	@Test
	void acceptsMinimumDiscount() {
		Coupon coupon = Coupon.create("XYZ789", "Minimo", new BigDecimal("0.5"), now.plusSeconds(30), true, now);

		assertThat(coupon.getDiscountValue().amount()).isEqualByComparingTo("0.5");
	}

	@Test
	void rejectsDiscountBelowMinimum() {
		assertThatThrownBy(() -> Coupon.create("XYZ789", "Baixo", new BigDecimal("0.49"), now.plusSeconds(30), true, now))
				.isInstanceOf(InvalidCouponException.class)
				.hasMessage("O valor de desconto deve ser maior ou igual a 0,5");
	}

	@Test
	void rejectsExpirationInThePast() {
		assertThatThrownBy(() -> Coupon.create("XYZ789", "Vencido", new BigDecimal("1"), now.minusSeconds(1), true, now))
				.isInstanceOf(InvalidCouponException.class)
				.hasMessage("A data de expiração não pode estar no passado");
	}

	@Test
	void acceptsExpirationEqualToNow() {
		Coupon coupon = Coupon.create("XYZ789", "Agora", new BigDecimal("1"), now, true, now);

		assertThat(coupon.getExpirationDate()).isEqualTo(now);
		assertThat(coupon.statusAt(now)).isEqualTo(CouponStatus.ACTIVE);
	}

	@Test
	void rejectsCodeThatDoesNotNormalizeToSixCharacters() {
		assertThatThrownBy(() -> Coupon.create("AB-12", "Curto", new BigDecimal("1"), now.plusSeconds(10), false, now))
				.isInstanceOf(InvalidCouponException.class);
		assertThatThrownBy(() -> Coupon.create("ABC-1234", "Longo", new BigDecimal("1"), now.plusSeconds(10), false, now))
				.isInstanceOf(InvalidCouponException.class);
	}

	@Test
	void rejectsDescriptionLongerThanFourThousandCharacters() {
		String description = "a".repeat(Coupon.DESCRIPTION_MAX_LENGTH + 1);

		assertThatThrownBy(() -> Coupon.create("ABC123", description, new BigDecimal("1"), now.plusSeconds(10), false, now))
				.isInstanceOf(InvalidCouponException.class)
				.hasMessage("A descrição do cupom deve ter no máximo 4000 caracteres");
	}

	@Test
	void rejectsBlankDescription() {
		assertThatThrownBy(() -> Coupon.create("ABC123", "   ", new BigDecimal("1"), now.plusSeconds(10), false, now))
				.isInstanceOf(InvalidCouponException.class)
				.hasMessage("A descrição do cupom é obrigatória");
	}

	@Test
	void softDeletesOnlyOnce() {
		Coupon coupon = Coupon.create("ABC123", "Unico", new BigDecimal("2"), now.plusSeconds(10), true, now);

		coupon.delete(now.plusSeconds(5));

		assertThat(coupon.getDeletedAt()).isEqualTo(now.plusSeconds(5));
		assertThat(coupon.statusAt(now.plusSeconds(5))).isEqualTo(CouponStatus.DELETED);
		assertThatThrownBy(() -> coupon.delete(now.plusSeconds(6)))
				.isInstanceOf(CouponAlreadyDeletedException.class);
	}

	@Test
	void restoredExpiredCouponIsInactiveAndDeletedCouponStaysDeleted() {
		Coupon expired = Coupon.restore(couponId(), "ABC123", "Expirado", new BigDecimal("1"), now.minusSeconds(1),
				true, false, null);
		Coupon deleted = Coupon.restore(couponId(), "ABC123", "Removido", new BigDecimal("1"), now.plusSeconds(10),
				true, false, now);

		assertThat(expired.statusAt(now)).isEqualTo(CouponStatus.INACTIVE);
		assertThat(deleted.statusAt(now)).isEqualTo(CouponStatus.DELETED);
	}

	private java.util.UUID couponId() {
		return java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
	}

}
