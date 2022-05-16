package ru.minikhanov.cloud_storage.exceptions;

import io.jsonwebtoken.MalformedJwtException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import ru.minikhanov.cloud_storage.models.MessageResponse;

import java.io.IOException;

@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handlerIllegalArgumentException(Exception e) {
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler({IOException.class, NullPointerException.class, ConstraintViolationException.class, StorageException.class, SizeLimitExceededException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handlerIOException(Exception e) {
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler({AuthorizationServiceException.class, AuthenticationException.class, HttpClientErrorException.Unauthorized.class, MalformedJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handlerUnauthorized(Exception e) {
        return new MessageResponse(e.getMessage());
    }
}
