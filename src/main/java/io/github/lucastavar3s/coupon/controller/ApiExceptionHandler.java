package io.github.lucastavar3s.coupon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.lucastavar3s.coupon.domain.exception.CouponAlreadyDeletedException;
import io.github.lucastavar3s.coupon.domain.exception.CouponNotFoundException;
import io.github.lucastavar3s.coupon.domain.exception.InvalidCouponException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"A requisição contém campos inválidos");
		problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList());
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadable(HttpMessageNotReadableException exception) {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "O corpo da requisição não pôde ser lido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "O identificador informado não é um UUID válido");
	}

	@ExceptionHandler(InvalidCouponException.class)
	public ProblemDetail handleInvalidCoupon(InvalidCouponException exception) {
		return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", exception.getMessage());
	}

	@ExceptionHandler(CouponNotFoundException.class)
	public ProblemDetail handleNotFound(CouponNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "Cupom não encontrado", exception.getMessage());
	}

	@ExceptionHandler(CouponAlreadyDeletedException.class)
	public ProblemDetail handleAlreadyDeleted(CouponAlreadyDeletedException exception) {
		return problem(HttpStatus.CONFLICT, "Cupom já excluído", exception.getMessage());
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}

}
