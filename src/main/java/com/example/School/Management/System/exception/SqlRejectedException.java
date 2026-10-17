package com.example.School.Management.System.exception;

public class SqlRejectedException extends RuntimeException {
    public SqlRejectedException(String msg) { super(msg); }
}