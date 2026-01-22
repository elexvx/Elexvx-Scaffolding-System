package com.tencent.tdesign.exception;

public class RepeatSubmitException extends RuntimeException {
  public RepeatSubmitException(String message) {
    super(message);
  }
}
