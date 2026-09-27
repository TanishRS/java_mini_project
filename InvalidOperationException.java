// Our own custom exception.
// We throw this whenever the user does something invalid
// (bad song ID, duplicate playlist, empty queue, etc.)
public class InvalidOperationException extends Exception {

    public InvalidOperationException(String message) {
        super(message);   // pass the message to the parent Exception class
    }
}
