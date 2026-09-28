package com.auth_service.domain.exception;

public class EmailNotFoundException extends  RuntimeException{
    public EmailNotFoundException(String message){
        super(message+" Not Found");
    }
}
