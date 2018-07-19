package com.gillsoft;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.RestError;

@ControllerAdvice
@RestController
public class RestControllerAdvice {
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public RestError allExceptions(Exception e) {
		e.printStackTrace();
		return new RestError(e.getClass().getName(), e.getMessage());
	}

}
