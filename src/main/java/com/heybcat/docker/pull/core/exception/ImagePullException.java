package com.heybcat.docker.pull.core.exception;

/**
 * @author Fetters
 */
public class ImagePullException extends RuntimeException {
  public ImagePullException(String message) {
    super(message);
  }
}
