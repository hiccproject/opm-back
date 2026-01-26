package opm.example.opm.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import opm.example.opm.common.error.ErrorCode;
import opm.example.opm.common.response.ApiResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** GlobalException 처리 */
    @ExceptionHandler(GlobalException.class)
    protected ApiResponse<Void> handleGlobalException(
            GlobalException e, HttpServletResponse response) {
        log.error("GlobalException: {}", e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        response.setStatus(errorCode.getStatus().value());
        return ApiResponse.error(errorCode.getCode(), e.getMessage());
    }

    /**
     * @Valid 검증 실패 시 발생 (RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletResponse response) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(), e);

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        response.setStatus(ErrorCode.INVALID_INPUT_VALUE.getStatus().value());
        return ApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors);
    }

    /** 파라미터 타입 불일치 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletResponse response) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);
        response.setStatus(ErrorCode.INVALID_TYPE_VALUE.getStatus().value());
        return ApiResponse.error(
                ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
    }

    /** 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiResponse<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletResponse response) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage(), e);
        response.setStatus(ErrorCode.MISSING_REQUEST_PARAMETER.getStatus().value());
        return ApiResponse.error(
                ErrorCode.MISSING_REQUEST_PARAMETER.getCode(),
                ErrorCode.MISSING_REQUEST_PARAMETER.getMessage());
    }

    /** 지원하지 않는 HTTP 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        response.setStatus(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value());
        return ApiResponse.error(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    protected ApiResponse<Void> handleNoResourceFoundException(
            NoResourceFoundException e, HttpServletRequest request, HttpServletResponse response) {

        // 스웨거 관련 리소스 요청(/v3/api-docs 등)이라면 핸들러가 개입하지 않고 시스템에 맡깁니다.
        String uri = request.getRequestURI();
        if (uri.contains("v3/api-docs") || uri.contains("swagger-ui")) {
            // 여기서 다시 throw를 하지 않고 404 응답을 직접 주면 "처리되지 않은 예외" 로그가 사라집니다.
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null; // 혹은 빈 응답
        }

        log.warn("일반 리소스 미발견: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return ApiResponse.error("C007", "요청하신 리소스를 찾을 수 없습니다.");
    }

    /**
     * JPA/Hibernate Bean Validation 제약 조건 위반 시 발생
     * 엔티티 필드에 선언된 @Email, @NotBlank 등의 검증 실패 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<Void> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletResponse response) {
        log.error("ConstraintViolationException: {}", e.getMessage(), e);

        // 1. 에러 메시지 추출 ("올바른 형식의 이메일 주소여야 합니다")
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        // 2. HTTP 상태 코드 설정 (400 Bad Request)
        response.setStatus(ErrorCode.INVALID_INPUT_VALUE.getStatus().value());

        // 3. 에러 코드 C002와 함께 구체적인 메시지 반환
        return ApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                errorMessage);
    }

    /**
     * 서비스 로직에서 발생하는 IllegalArgumentException 처리
     * (예: 비밀번호 불일치, 이미 존재하는 이메일 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ApiResponse<Void> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletResponse response) {
        log.error("IllegalArgumentException: {}", e.getMessage());

        // HTTP 상태 코드를 400 Bad Request로 설정
        response.setStatus(ErrorCode.INVALID_INPUT_VALUE.getStatus().value());

        // 에러 코드 C002와 함께 서비스에서 보낸 메시지(e.getMessage())를 반환
        return ApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    protected ApiResponse<Void> handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        // 400 Bad Request 상태 코드를 Swagger에 전달
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        // ApiResponse 양식에 맞춰 에러 메시지를 반환
        return ApiResponse.error("A002", e.getMessage());
    }

    /** 그 외 모든 예외 처리 */
    @ExceptionHandler(Exception.class)
    protected ApiResponse<Void> handleException(Exception e, HttpServletResponse response) {
        log.error("Unhandled Exception: ", e);
        response.setStatus(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value());
        return ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
