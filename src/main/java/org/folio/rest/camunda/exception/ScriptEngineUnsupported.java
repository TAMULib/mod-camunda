package org.folio.rest.camunda.exception;

public class ScriptEngineUnsupported extends Exception {

  private static final long serialVersionUID = 2176459311584173842L;

  private static final String MESSAGE = "The Scripting Engine, %s, is not supported.";

  public ScriptEngineUnsupported(String extension) {
    super(String.format(MESSAGE, extension));
  }

  public ScriptEngineUnsupported(String extension, Exception e) {
    super(String.format(MESSAGE, extension), e);
  }

}
