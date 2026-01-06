package com.abidev.controllers.exception;

import com.abidev.annotations.ControllerAdvice;
import com.abidev.annotations.ExceptionHandler;
import com.abidev.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleGeneric(Exception ex) {
//        System.out.println("Unhandled exception: " + ex.getMessage());
//        return ResponseEntity
//                .status(500)
//                .body("Unexpected error");
//    }

//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Object> handleBadRequest(Exception ex) {
//        return ResponseEntity
//                .status(400)
//                .body(ex.getMessage());
//    }

}
