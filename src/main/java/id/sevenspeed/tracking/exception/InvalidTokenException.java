// exception/InvalidTokenException.java
package id.sevenspeed.tracking.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}