package org.folio.rest.camunda.exception;

public class ScriptEngineLoadFailed extends Exception {

  private static final long serialVersionUID = 5176444311634173282L;

  private static final String MESSAGE = "The Scripting Engine, %s, failed to load.";

  public ScriptEngineLoadFailed(String extension) {
    super(String.format(MESSAGE, extension));
  }

  public ScriptEngineLoadFailed(String extension, Exception e) {
    super(String.format(MESSAGE, extension), e);
  }

}
