package org.folio.rest.camunda.exception;

import org.folio.rest.camunda.model.FolioEnvDefaultsItem;
import org.folio.rest.camunda.model.enums.FolioEnvDefaultsItemField;

/**
 * Handle exceptions regarding {@link FolioEnvDefaultsItem} fields.
 */
public class FolioEnvDefaultsItemFieldException extends RuntimeException {

  private static final long serialVersionUID = 2889127217533153059L;

  static final String MSG_BASIC = "FOLIO environments defaults item field '%s' error: %s.";

  static final String MSG_NAMED = "FOLIO environments defaults item '%s' field '%s' error: %s.";

  /**
   * Present an error associated with the named field.
   *
   * @param field   The field from FolioEnvDefaultsItem.
   * @param message The description of the problem.
   */
  public FolioEnvDefaultsItemFieldException(FolioEnvDefaultsItemField field, String message) {

    super(String.format(MSG_BASIC, field.getName(), message));
  }

  /**
   * Present an error associated with the named field with an associated exception.
   *
   * @param field   The field from FolioEnvDefaultsItem.
   * @param message The description of the problem.
   * @param e       The associated exception.
   */
  public FolioEnvDefaultsItemFieldException(FolioEnvDefaultsItemField field, String message, Exception e) {

    super(String.format(MSG_BASIC, field.getName(), message), e);
  }

  /**
   * Present an error associated with the named field.
   *
   * @param field   The field from FolioEnvDefaultsItem.
   * @param name    The name of the item.
   * @param message The description of the problem.
   */
  public FolioEnvDefaultsItemFieldException(FolioEnvDefaultsItemField field, String name, String message) {

    super(String.format(MSG_NAMED, name, field.getName(), message));
  }

  /**
   * Present an error associated with the named field with an associated exception.
   *
   * @param field   The field from FolioEnvDefaultsItem.
   * @param name    The name of the item.
   * @param message The description of the problem.
   */
  public FolioEnvDefaultsItemFieldException(FolioEnvDefaultsItemField field, String name, String message, Exception e) {

    super(String.format(MSG_NAMED, name, field.getName(), message), e);
  }

}
