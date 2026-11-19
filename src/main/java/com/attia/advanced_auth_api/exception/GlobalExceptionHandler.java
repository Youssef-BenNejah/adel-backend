package com.attia.advanced_auth_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
		String detail = ex.getReason() == null ? ex.getMessage() : ex.getReason();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), detail);
		problem.setTitle(ex.getStatusCode().toString());
		problem.setType(URI.create("https://yourapp.com/errors/" + ex.getStatusCode().value()));
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.status(ex.getStatusCode()).body(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining(", "));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
		problem.setTitle("Validation failed");
		problem.setType(URI.create("https://yourapp.com/errors/validation"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.badRequest().body(problem);
	}

	/** Unmapped route — no controller matches this path at all. */
	@ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
	public ResponseEntity<ProblemDetail> handleNotFound(Exception ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
				"No endpoint " + request.getMethod() + " " + request.getRequestURI() + ".");
		problem.setTitle("Not Found");
		problem.setType(URI.create("https://yourapp.com/errors/404"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	/** Route exists, but not for this HTTP method. */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
		problem.setTitle("Method Not Allowed");
		problem.setType(URI.create("https://yourapp.com/errors/405"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
		problem.setTitle("Internal Server Error");
		problem.setType(URI.create("https://yourapp.com/errors/internal"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
