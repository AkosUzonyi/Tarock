package com.tisza.tarock.spring.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthenticatedException extends RuntimeException
{
}
