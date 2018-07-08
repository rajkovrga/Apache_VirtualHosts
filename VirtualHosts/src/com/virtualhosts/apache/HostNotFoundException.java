package com.virtualhosts.apache;

public class HostNotFoundException extends Throwable {
    private String message;
    public HostNotFoundException() {
        super();
        this.message = "Wanted host is not found";
    }
    public HostNotFoundException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }
}
