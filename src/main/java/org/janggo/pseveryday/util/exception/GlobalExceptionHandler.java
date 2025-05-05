package org.janggo.pseveryday.util.exception;

import org.janggo.pseveryday.util.exception.custom.SubscriberNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}

