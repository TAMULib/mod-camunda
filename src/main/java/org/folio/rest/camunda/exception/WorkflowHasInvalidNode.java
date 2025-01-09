package org.folio.rest.camunda.exception;

import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.Workflow;

public class WorkflowHasInvalidNode extends Exception {

  private static final long serialVersionUID = 6453902277765426101L;

  private static final String IS_NULL = "(is null)";

  private static final String MESSAGE = "The workflow %s has an invalid node %s.";
  
  private static final String NAME_ID = "%s (%s)";

  /**
   * Provide error about Workflow having an Invalid Node.
   *
   * @param workflow The Workflow (should not be NULL).
   * @param node The invalid Node (may be NULL).
   */
  public WorkflowHasInvalidNode(Workflow workflow, Node node) {
    super(String.format(MESSAGE, workflowString(workflow), nodeString(node)));
  }

  /**
   * Provide error about Workflow having an Invalid Node.
   *
   * @param workflow The Workflow (should not be NULL).
   * @param node The invalid Node (may be NULL).
   * @param e The exception thrown.
   */
  public WorkflowHasInvalidNode(Workflow workflow, Node node, Exception e) {
    super(String.format(MESSAGE, workflowString(workflow), nodeString(node)), e);
  }

  /**
   * Determine string based on NULL state of node.
   *
   * @param node The node.
   *
   * @return A messages based on the NULL state of the Node.
   */
  private static String nodeString(Node node) {
    if (node == null) {
      return IS_NULL;
    }

    return String.format(NAME_ID, node.getName(), node.getIdentifier());
  }

  /**
   * Determine the Workflow name and identifier.
   *
   * @param workflow The Workflow.
   *
   * @return A messages based on the NULL state of the Workflow.
   */
  private static String workflowString(Workflow workflow) {
    if (workflow == null) {
      return IS_NULL;
    }

    return String.format(NAME_ID, workflow.getName(), workflow.getId());
  }

}