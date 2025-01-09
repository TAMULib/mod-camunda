package org.folio.rest.camunda.exception;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowHasInvalidNodeTest {

  private static final String NODE_UUID = UUID + "Node";

  @Mock
  private Workflow workflow;

  @Mock
  private Node node;

  @BeforeEach
  void beforeEach() {
    when(workflow.getId()).thenReturn(UUID);
    when(workflow.getName()).thenReturn(KEY);

    lenient().when(node.getIdentifier()).thenReturn(NODE_UUID);
    lenient().when(node.getName()).thenReturn(VALUE);
  }

  @Test
  void WorkflowHasInvalidNodeWorksTest() throws IOException {
    WorkflowHasInvalidNode exception = Assertions.assertThrows(WorkflowHasInvalidNode.class, () -> {
      throw new WorkflowHasInvalidNode(workflow, node);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(NODE_UUID));
  }

  @Test
  void WorkflowHasInvalidNodeWorksWithNullNodeTest() throws IOException {
    WorkflowHasInvalidNode exception = Assertions.assertThrows(WorkflowHasInvalidNode.class, () -> {
      throw new WorkflowHasInvalidNode(workflow, null);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertFalse(exception.getMessage().contains(NODE_UUID));
    assertTrue(exception.getMessage().contains("is null"));
  }

  @Test
  void WorkflowHasInvalidNodeWorksWithParameterTest() throws IOException {
    WorkflowHasInvalidNode exception = Assertions.assertThrows(WorkflowHasInvalidNode.class, () -> {
      throw new WorkflowHasInvalidNode(workflow, node, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertTrue(exception.getMessage().contains(NODE_UUID));
  }

  @Test
  void WorkflowHasInvalidNodeWorksWithParameterWithNullNodeTest() throws IOException {
    WorkflowHasInvalidNode exception = Assertions.assertThrows(WorkflowHasInvalidNode.class, () -> {
      throw new WorkflowHasInvalidNode(workflow, null, new RuntimeException());
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains(UUID));
    assertFalse(exception.getMessage().contains(NODE_UUID));
    assertTrue(exception.getMessage().contains("is null"));
  }

}
