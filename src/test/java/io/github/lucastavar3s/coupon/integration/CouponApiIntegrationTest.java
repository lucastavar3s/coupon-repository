package io.github.lucastavar3s.coupon.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import io.github.lucastavar3s.coupon.repository.CouponRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class CouponApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CouponRepository couponRepository;

	@BeforeEach
	void cleanDatabase() {
		couponRepository.deleteAll();
	}

	@Test
	void postReturns201WhenCouponIsCreatedPublished() throws Exception {
		MvcResult result = mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("ABC-123", "0.8", future(), true)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.code").value("ABC123"))
				.andExpect(jsonPath("$.description").value("Cupom de boas-vindas"))
				.andExpect(jsonPath("$.discountValue").value(0.8))
				.andExpect(jsonPath("$.published").value(true))
				.andExpect(jsonPath("$.redeemed").value(false))
				.andReturn();

		String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		assertThat(couponRepository.findById(UUID.fromString(id))).get().satisfies(saved -> {
			assertThat(saved.getCode()).isEqualTo("ABC123");
			assertThat(saved.isPublished()).isTrue();
			assertThat(saved.getDeletedAt()).isNull();
		});
	}

	@Test
	void postReturns201ActiveWhenPublishedIsOmitted() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "ZZZ999",
								  "description": "Rascunho",
								  "discountValue": 0.5,
								  "expirationDate": "%s"
								}
								""".formatted(future())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.published").value(false))
				.andExpect(jsonPath("$.discountValue").value(0.5));

		assertThat(couponRepository.count()).isEqualTo(1);
	}

	@Test
	void postReturns201WhenTheSameCodeIsUsedTwice() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("ABC123", "1", future(), true)))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("ABC123", "1", future(), false)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.code").value("ABC123"));

		assertThat(couponRepository.count()).isEqualTo(2);
	}

	@Test
	void postReturns400WhenDescriptionExceedsFourThousandCharacters() throws Exception {
		String description = "a".repeat(4001);
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "ABC123",
								  "description": "%s",
								  "discountValue": 1,
								  "expirationDate": "%s"
								}
								""".formatted(description, future())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void postReturns400WhenRequiredFieldsAreMissing() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "code": "ABC123" }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"))
				.andExpect(jsonPath("$.errors").isArray());

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void postReturns400WhenBodyCannotBeRead() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code": "ABC123",
								  "description": "Data invalida",
								  "discountValue": 1,
								  "expirationDate": "ontem"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("O corpo da requisição não pôde ser lido"));

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void postReturns422WhenDiscountIsBelowMinimum() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("ABC123", "0.49", future(), true)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.title").value("Regra de negócio violada"))
				.andExpect(jsonPath("$.detail").value("O valor de desconto deve ser maior ou igual a 0,5"));

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void postReturns422WhenExpirationIsInThePast() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("ABC123", "1", Instant.now().minus(1, ChronoUnit.HOURS).toString(), true)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.detail").value("A data de expiração não pode estar no passado"));

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void postReturns422WhenNormalizedCodeDoesNotHaveSixCharacters() throws Exception {
		mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("AB-1", "1", future(), false)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.detail").value(
						"O código do cupom deve conter 6 caracteres alfanuméricos após a remoção dos caracteres especiais"));

		assertThat(couponRepository.count()).isZero();
	}

	@Test
	void getReturns200WhenCouponExists() throws Exception {
		String id = createCoupon("QWE123");

		mockMvc.perform(get("/coupon/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.code").value("QWE123"));
	}

	@Test
	void getReturns200WithDeletedStatusAfterSoftDelete() throws Exception {
		String id = createCoupon("DEL123");
		mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/coupon/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DELETED"))
				.andExpect(jsonPath("$.code").value("DEL123"));
	}

	@Test
	void getReturns404WhenCouponDoesNotExist() throws Exception {
		mockMvc.perform(get("/coupon/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Cupom não encontrado"));
	}

	@Test
	void getReturns400WhenIdentifierIsNotUuid() throws Exception {
		mockMvc.perform(get("/coupon/{id}", "nao-e-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("O identificador informado não é um UUID válido"));
	}

	@Test
	void deleteReturns204AndKeepsTheRow() throws Exception {
		String id = createCoupon("DEL123");

		mockMvc.perform(delete("/coupon/{id}", id))
				.andExpect(status().isNoContent());

		assertThat(couponRepository.findById(UUID.fromString(id))).get().satisfies(saved -> {
			assertThat(saved.getDeletedAt()).isNotNull();
			assertThat(saved.getCode()).isEqualTo("DEL123");
			assertThat(saved.getDescription()).isEqualTo("Cupom de boas-vindas");
		});
		assertThat(couponRepository.count()).isEqualTo(1);
	}

	@Test
	void deleteReturns404WhenCouponDoesNotExist() throws Exception {
		mockMvc.perform(delete("/coupon/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Cupom não encontrado"));
	}

	@Test
	void deleteReturns409WhenCouponWasAlreadyDeleted() throws Exception {
		String id = createCoupon("DEL123");
		mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(delete("/coupon/{id}", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Cupom já excluído"))
				.andExpect(jsonPath("$.detail").value("Não é possível excluir um cupom que já foi excluído"));
	}

	@Test
	void deleteReturns400WhenIdentifierIsNotUuid() throws Exception {
		mockMvc.perform(delete("/coupon/{id}", "nao-e-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("O identificador informado não é um UUID válido"));
	}

	private String createCoupon(String code) throws Exception {
		MvcResult result = mockMvc.perform(post("/coupon")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body(code, "1.5", future(), true)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private String future() {
		return Instant.now().plus(2, ChronoUnit.DAYS).toString();
	}

	private String body(String code, String discount, String expiration, boolean published) {
		return """
				{
				  "code": "%s",
				  "description": "Cupom de boas-vindas",
				  "discountValue": %s,
				  "expirationDate": "%s",
				  "published": %s
				}
				""".formatted(code, discount, expiration, published);
	}

}
