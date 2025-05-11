package org.janggo.pseveryday.util.exception;

import org.janggo.pseveryday.util.exception.custom.SubscriberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriberNotFoundException.class)
    public String handleSubscriberNotFound(SubscriberNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/subscriber-not-found";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/illegal-argument";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandlerFound(NoHandlerFoundException ex) {
        return "error/404"; // templates/error/404.html 파일을 보여줍니다.
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNoHandlerFound(Exception ex) {
        return "error/500"; // templates/error/404.html 파일을 보여줍니다.
    }
}

