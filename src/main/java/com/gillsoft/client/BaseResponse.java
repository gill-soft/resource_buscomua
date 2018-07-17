package com.gillsoft.client;

public abstract class BaseResponse {
	
    public abstract ErrorType getError();

    public abstract void setError(ErrorType value);

}
