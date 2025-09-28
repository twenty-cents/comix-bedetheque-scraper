package com.comix.scrapers.bedetheque.exception;

import com.comix.scrapers.bedetheque.rest.v1.dto.ApiErrorDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.FieldErrorDto;
import com.comix.scrapers.bedetheque.util.ApiLanguageHelper;
import com.comix.scrapers.bedetheque.util.ResourceBundleHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class FreebdsApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreebdsApiExceptionHandler.class); //NOSONAR

    private final ApiLanguageHelper apiLanguageHelper;

    public FreebdsApiExceptionHandler(ApiLanguageHelper apiLanguageHelper) {
        this.apiLanguageHelper = apiLanguageHelper;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Get the API language
        String lang = request.getParameter("lang");
        if(lang == null) {
            lang = "";
        }
        lang = apiLanguageHelper.validateAndApplyApiLanguage(lang);
        // Set the API response
        String message = ResourceBundleHelper.getLocalizedMessage("ERR-CTL-ARG", lang);
        HttpStatus httpStatus;
        if(HttpStatus.resolve(status.value()) == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else {
            httpStatus = HttpStatus.valueOf(status.value());
        }

        var apiError = buildApiErrorDto("ERR-CTL-ARG", httpStatus,  "Method Argument validation exception", message);
        // List all errors
        List<FieldErrorDto> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            var f = new FieldErrorDto();
            f.setField(error.getField());
            f.setMessage(error.getDefaultMessage());
            errors.add(f);
            LOGGER.error("{} {} : {} - {}", apiError.getType(), apiError.getCode(), f.getField(), f.getMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            var f = new FieldErrorDto();
            f.setField(error.getObjectName());
            f.setMessage(error.getDefaultMessage());
            errors.add(f);
            LOGGER.error("{} {} : {} - {}", apiError.getType(), apiError.getCode(), f.getField(), f.getMessage());
        }
        apiError.setDetail(errors);
        logError(apiError);
        return new ResponseEntity<>(apiError, httpStatus);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleFreeBdsApiScraperBusinessException(ConstraintViolationException ex) {
        String msg = ResourceBundleHelper.getLocalizedMessage("CONSTRAINT_VIOLATION", new Object[]{});
        var apiError = buildApiErrorDto("CONSTRAINT_VIOLATION", HttpStatus.BAD_REQUEST,  "ConstraintViolationException", msg);
        // List all errors
        List<FieldErrorDto> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            var f = new FieldErrorDto();
            f.setField(violation.getPropertyPath().toString());
            f.setMessage(violation.getMessage());
            errors.add(f);
        }
        apiError.setDetail(errors);
        logError(apiError);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<Object> handleFreeBdsApiScraperTechnicalException(TechnicalException ex) {
        String msg = ResourceBundleHelper.getLocalizedMessage(ex.getCodeMessage(), ex.getArgs());
        var apiError = buildApiErrorDto(ex.getCodeMessage(), ex.getHttpStatus(), "Technical exception", msg);
        logError(apiError);
        return new ResponseEntity<>(apiError, ex.getHttpStatus());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleFreeBdsApiScraperBusinessException(BusinessException ex) {
        String msg = ResourceBundleHelper.getLocalizedMessage(ex.getCodeMessage(), ex.getArgs());
        var apiError = buildApiErrorDto(ex.getCodeMessage(), ex.getHttpStatus(), "Business exception", msg);
        logError(apiError);
        return new ResponseEntity<>(apiError, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest req) {
        var apiError = buildApiErrorDto( "ERR-JVM-001", HttpStatus.INTERNAL_SERVER_ERROR, "Exception", ex.getLocalizedMessage());
        logError(apiError);
        LOGGER.error(ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    private ApiErrorDto buildApiErrorDto(String code, HttpStatus status, String type, String message) {
        var apiError = new ApiErrorDto();
        apiError.setCode(code);
        apiError.setType(type);
        apiError.setStatus(status.value());
        apiError.setMessage(message);
        return apiError;
    }

    private void logError(ApiErrorDto apiErrorDto) {
        StringBuilder errorMessage = new StringBuilder(String.format("API error response : [%s] - %s - %s", apiErrorDto.getStatus(), apiErrorDto.getCode(), apiErrorDto.getMessage()));
        if(!CollectionUtils.isEmpty(apiErrorDto.getDetail())) {
            errorMessage.append(" - Details : ");
            for(FieldErrorDto fieldErrorDto : apiErrorDto.getDetail()) {
                errorMessage.append(String.format(" - %s : %s", fieldErrorDto.getField(), fieldErrorDto.getMessage()));
            }
        }
        LOGGER.error("{}", errorMessage);
    }

}
