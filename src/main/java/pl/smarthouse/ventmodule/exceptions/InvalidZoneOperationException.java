package pl.smarthouse.ventmodule.exceptions;

public class InvalidZoneOperationException extends RuntimeException {
  public InvalidZoneOperationException(final String message) {
    super(message);
  }
}
