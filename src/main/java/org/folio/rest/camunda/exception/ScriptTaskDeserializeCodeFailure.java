package org.folio.rest.camunda.exception;

public class ScriptTaskDeserializeCodeFailure extends Exception {

  private static final long serialVersionUID = -6270663785866339965L;

  private static final String MESSAGE = "Failed to De-serialize code for ScriptTask %s, code JSON is '%s'.";

  public ScriptTaskDeserializeCodeFailure(String scriptTaskUuid, String code) {
    super(String.format(MESSAGE, scriptTaskUuid, code));
  }

  public ScriptTaskDeserializeCodeFailure(String scriptTaskUuid, String code, Exception e) {
    super(String.format(MESSAGE, scriptTaskUuid, code), e);
  }

}
