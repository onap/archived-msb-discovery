package org.onap.msb.sdclient.core.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnprocessableEntityException extends ClientErrorException{
private static final long serialVersionUID = -8266622745725405656L;
private static final Logger LOGGER = LoggerFactory.getLogger(UnprocessableEntityException.class);  

  public UnprocessableEntityException(final String message) {
    super(Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(message).type(MediaType.TEXT_PLAIN).build());
    LOGGER.warn(message);
  }
}
