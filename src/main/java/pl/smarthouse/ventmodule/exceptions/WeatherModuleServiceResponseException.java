package pl.smarthouse.ventmodule.exceptions;

import org.springframework.http.HttpStatus;

public class WeatherModuleServiceResponseException extends RuntimeException {
  public WeatherModuleServiceResponseException(final HttpStatus httpStatus, final String response) {
    super(String.format("Http status code: %s, response: %s", httpStatus, response));
  }
}
