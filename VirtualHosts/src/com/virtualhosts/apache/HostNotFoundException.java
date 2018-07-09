package com.virtualhosts.apache;


/**
 * Exception when the looking Host is not found
 * @author Dusan Malusev
 * @version 1.0
 */
public class HostNotFoundException extends Throwable {
    /**
     * Private field message
     */
    private String message;

    /**
     * Primary constructor
     */
    public HostNotFoundException() {
        super();
        this.message = "Wanted host is not found";
    }

    /**
     * Constructor overload
     * @param message Message to be displayed
     */
    public HostNotFoundException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
