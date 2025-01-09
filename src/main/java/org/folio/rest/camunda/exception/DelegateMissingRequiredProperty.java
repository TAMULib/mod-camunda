package org.folio.rest.camunda.exception;

public class DelegateMissingRequiredProperty extends Exception {

  private static final long serialVersionUID = 336593046617507766L;

  private static final String MESSAGE = "The %s (%s) is missing the required property '%s'.";

  /**
   * Error on delegate missing a required property.
   *
   * @param type The delegate type.
   * @param id The identifier of the delegate.
   * @param property The name of the invalid property.
   */
  public DelegateMissingRequiredProperty(String type, String id, String property) {
    super(String.format(MESSAGE, type, id, property));
  }

  /**
   * Error on delegate missing a required property.
   *
   * @param type The delegate type.
   * @param id The identifier of the delegate.
   * @param property The name of the invalid property.
   * @param e The exception thrown.
   */
  public DelegateMissingRequiredProperty(String type, String id, String property, Exception e) {
    super(String.format(MESSAGE, type, id, property), e);
  }

}