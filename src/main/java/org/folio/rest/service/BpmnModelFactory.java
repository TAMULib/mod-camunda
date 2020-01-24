package org.folio.rest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.SubProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.delegate.AbstractWorkflowDelegate;
import org.folio.rest.workflow.components.Branch;
import org.folio.rest.workflow.components.Conditional;
import org.folio.rest.workflow.components.Event;
import org.folio.rest.workflow.components.Navigation;
import org.folio.rest.workflow.components.Task;
import org.folio.rest.workflow.components.Wait;
import org.folio.rest.workflow.model.ConditionalGateway;
import org.folio.rest.workflow.model.ConnectTo;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.EventSubprocess;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.ReceiveTask;
import org.folio.rest.workflow.model.StartEvent;
import org.folio.rest.workflow.model.StartEventType;
import org.folio.rest.workflow.model.StreamingExtractTransformLoadTask;
import org.folio.rest.workflow.model.Subprocess;
import org.folio.rest.workflow.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BpmnModelFactory {

  private final static Logger logger = LoggerFactory.getLogger(BpmnModelFactory.class);

  private final static String SETUP_TASK_ID = "setup_task_98832611_3d33_476b_adcc_fcb6c4e8718b";

  // @formatter:off
  private final static Class<?>[] SERIALIZABLE_TYPES = new Class<?>[] {
    String.class,
    Number.class,
    Boolean.class,
    Enum.class
  };
  // @formatter:on

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private List<AbstractWorkflowDelegate> workflowDelegates;

  public BpmnModelInstance fromWorkflow(Workflow workflow) {

    // @formatter:off
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess().name(workflow.getName())
        .camundaHistoryTimeToLive(workflow.getHistoryTimeToLive())
        .camundaVersionTag(workflow.getVersionTag());
    // @formatter:on

    BpmnModelInstance model = build(processBuilder, workflow);

    // @formatter:off
    workflow.getNodes().stream()
      .filter(node -> node instanceof EventSubprocess)
      .forEach(subprocess -> eventSubprocess(processBuilder, subprocess));
    // @formatter:on

    setup(model, workflow);
    expressions(model, workflow.getNodes());
    return model;
  }

  private BpmnModelInstance build(ProcessBuilder processBuilder, Workflow workflow) {
    List<Node> nodes = workflow.getNodes();

    if (nodes.isEmpty()) {
      return processBuilder.done();
    }

    AbstractFlowNodeBuilder<?, ?> builder = processBuilder.startEvent();

    if (!(nodes.get(0) instanceof StartEvent)) {
      // TODO: create custom exception and controller advice to handle better
      throw new RuntimeException("Workflow must start with a start event!");
    }

    builder = build(builder, nodes, Setup.from(workflow.getSetup()));

    return builder.done();
  }

  private void eventSubprocess(ProcessBuilder processBuilder, Node node) {
    AbstractFlowNodeBuilder<?, ?> builder = null;
    String identifier = node.getIdentifier();
    String name = node.getName();
    builder = processBuilder.eventSubProcess(identifier).name(name).startEvent();
    builder = build(builder, ((EventSubprocess) node).getNodes(), Setup.NONE);
  }

  private AbstractFlowNodeBuilder<?, ?> build(AbstractFlowNodeBuilder<?, ?> builder, List<Node> nodes, Setup setup) {

    for (Node node : nodes) {

      if (node instanceof Event) {

        if (node instanceof StartEvent) {

          if (((StartEvent) node).isAsyncBefore()) {
            builder = builder.camundaAsyncBefore();
          }

          boolean interrupting = ((StartEvent) node).isInterrupting();

          StartEventType type = ((StartEvent) node).getType();
          String expression = ((StartEvent) node).getExpression();

          if (type != StartEventType.NONE && expression == null) {
            // TODO: implement and ensure validation
            throw new RuntimeException(String.format("%s start event requests an expression", type));
          }

          builder = ((StartEventBuilder) builder).id(node.getIdentifier()).name(node.getName());

          switch (type) {
          case MESSAGE_CORRELATION:
            builder = ((StartEventBuilder) builder).message(expression).interrupting(interrupting);
            break;
          case SCHEDULED:
            builder = ((StartEventBuilder) builder).timerWithCycle(expression).interrupting(interrupting);
            break;
          case SIGNAL:
            builder = ((StartEventBuilder) builder).signal(expression).interrupting(interrupting);
            break;
          case NONE:
            builder = ((StartEventBuilder) builder).interrupting(interrupting);
            break;
          default:
            // unknown start event
            break;
          }

          if (!setup.equals(Setup.NONE)) {
            builder = builder.serviceTask(SETUP_TASK_ID).name("Setup").camundaDelegateExpression("${setupDelegate}");

            switch (setup) {
            case ASYNC_AFTER:
              builder = builder.camundaAsyncAfter();
              break;
            case ASYNC_BEFORE:
              builder = builder.camundaAsyncBefore();
              break;
            case ASYNC_BEFORE_AFTER:
              builder = builder.camundaAsyncBefore().camundaAsyncAfter();
              break;
            case NONE:
            case SIMPLE:
            default:
              break;
            }
          }

        } else if (node instanceof EndEvent) {
          builder = builder.endEvent();
        } else {
          // unknown event
        }

      } else if (node instanceof Task) {

        Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
            .filter(d -> d.fromTask().equals(node.getClass())).findAny();

        if (delegate.isPresent()) {
          builder = builder.serviceTask(node.getIdentifier()).name(node.getName())
              .camundaDelegateExpression(delegate.get().getExpression());
        } else {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

        if (((Task) node).isAsyncBefore()) {
          builder = builder.camundaAsyncBefore();
        }

        if (((Task) node).isAsyncAfter()) {
          builder = builder.camundaAsyncAfter();
        }

      } else if (node instanceof Branch) {

        if (node instanceof ConditionalGateway) {

          switch (((ConditionalGateway) node).getType()) {

          case EXCLUSIVE:
            builder = builder.exclusiveGateway().name(node.getName());
            break;
          case INCLUSIVE:
            // TODO: implement and ensure validation
            throw new RuntimeException("Inclusive gateway not yet supported!");
          case MOVE_TO_LAST:
            builder = builder.moveToLastGateway();
            break;
          default:
            break;

          }
        } else if ((node instanceof Subprocess)) {

          SubProcessBuilder subProcessBuilder = builder.subProcess(node.getIdentifier()).name(node.getName());

          if (((Subprocess) node).isAsyncBefore()) {
            subProcessBuilder = subProcessBuilder.camundaAsyncBefore();
          }

          if (((Subprocess) node).isAsyncAfter()) {
            subProcessBuilder = subProcessBuilder.camundaAsyncAfter();
          }

          switch (((Subprocess) node).getType()) {
          case EMBEDDED:
            builder = subProcessBuilder.embeddedSubProcess().startEvent();
            builder = build(builder, ((Branch) node).getNodes(), Setup.NONE);
            builder = builder.subProcessDone();
            break;
          case TRANSACTION:
            // TODO: create custom exception and controller advice to handle better
            throw new RuntimeException("Transaction subprocess not yet supported!");
          default:
            break;
          }

        }

        if (node instanceof Conditional) {
          builder = builder.condition(((Conditional) node).getAnswer(), ((Conditional) node).getCondition());
        }

        if (!(node instanceof Subprocess)) {
          builder = build(builder, ((Branch) node).getNodes(), Setup.NONE);
        }

      } else if (node instanceof Navigation) {

        if (node instanceof ConnectTo) {

          // TODO: figure out way to get identifier from id
          builder = builder.connectTo(((ConnectTo) node).getNodeId());

        } else {
          // unknown navigation
        }

      } else if (node instanceof Wait) {

        if (node instanceof ReceiveTask) {

          builder = builder.receiveTask(((Node) node).getIdentifier()).name(((Node) node).getName())
              .message(((ReceiveTask) node).getMessage());

        } else {
          // unknown wait
        }

        if (((Wait) node).isAsyncBefore()) {
          builder = builder.camundaAsyncBefore();
        }

        if (((Wait) node).isAsyncAfter()) {
          builder = builder.camundaAsyncAfter();
        }

      }
    }

    // must end in end event or connect to

    return builder;
  }

  private void setup(BpmnModelInstance model, Workflow workflow) {
    ExtensionElements extensions = model.newInstance(ExtensionElements.class);

    Map<String, String> initialContext = workflow.getInitialContext();
    CamundaField icField = model.newInstance(CamundaField.class);
    icField.setCamundaName("initialContext");
    try {
      icField.setCamundaStringValue(objectMapper.writeValueAsString(initialContext));
    } catch (JsonProcessingException e) {
      logger.warn("Failed to serialize initial context");
    }
    extensions.addChildElement(icField);

    List<EmbeddedProcessor> processors = getProcessorScripts(workflow.getNodes());
    CamundaField psField = model.newInstance(CamundaField.class);
    psField.setCamundaName("processors");
    try {
      psField.setCamundaStringValue(objectMapper.writeValueAsString(processors));
    } catch (JsonProcessingException e) {
      logger.warn("Failed to serialize processor scripts");
    }
    extensions.addChildElement(psField);

    ModelElementInstance element = model.getModelElementById(SETUP_TASK_ID);
    element.addChildElement(extensions);
  }

  private void expressions(BpmnModelInstance model, List<Node> nodes) {
    nodes.stream().forEach(node -> {

      Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
          .filter(d -> d.fromTask().equals(node.getClass())).findAny();

      if (delegate.isPresent()) {

        ExtensionElements extensions = model.newInstance(ExtensionElements.class);

        FieldUtils.getAllFieldsList(delegate.get().getClass()).stream()
            .filter(df -> Expression.class.isAssignableFrom(df.getType()))
            .map(df -> FieldUtils.getDeclaredField(node.getClass(), df.getName(), true)).forEach(f -> {
              try {
                CamundaField field = model.newInstance(CamundaField.class);
                field.setCamundaName(f.getName());
                field.setCamundaStringValue(serialize(f.get(node)));
                extensions.addChildElement(field);
              } catch (JsonProcessingException | IllegalArgumentException | IllegalAccessException e) {
                // TODO: create custom exception and controller advice to handle better
                throw new RuntimeException(e);
              }
            });

        ModelElementInstance element = model.getModelElementById(node.getIdentifier());
        element.addChildElement(extensions);

      } else {

        if (node instanceof Branch) {
          expressions(model, ((Branch) node).getNodes());
        } else if (node instanceof Subprocess) {
          expressions(model, ((Subprocess) node).getNodes());
        } else if (node instanceof Task) {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

      }
    });
  }

  private List<EmbeddedProcessor> getProcessorScripts(List<Node> nodes) {
    List<EmbeddedProcessor> scripts = new ArrayList<EmbeddedProcessor>();
    nodes.stream().forEach(node -> {
      if (node instanceof ProcessorTask) {
        scripts.add(((ProcessorTask) node).getProcessor());
      } else if (node instanceof StreamingExtractTransformLoadTask) {
        scripts.addAll(((StreamingExtractTransformLoadTask) node).getProcessors());
      } else if (node instanceof Branch) {
        scripts.addAll(getProcessorScripts(((Branch) node).getNodes()));
      } else if (node instanceof Subprocess) {
        scripts.addAll(getProcessorScripts(((Subprocess) node).getNodes()));
      }
    });
    return scripts;
  }

  private String serialize(Object value) throws JsonProcessingException {
    if (isSerializableType(value.getClass())) {
      return value.toString();
    } else {
      return objectMapper.writeValueAsString(value);
    }
  }

  private boolean isSerializableType(Class<?> type) {
    for (Class<?> c : SERIALIZABLE_TYPES) {
      if (c.isAssignableFrom(type)) {
        return true;
      }
    }
    return false;
  }

  private enum Setup {
    NONE, SIMPLE, ASYNC_BEFORE, ASYNC_AFTER, ASYNC_BEFORE_AFTER;

    public static Setup from(org.folio.rest.workflow.model.Setup setup) {
      if (setup.isAsyncAfter()) {
        if (setup.isAsyncBefore()) {
          return ASYNC_BEFORE_AFTER;
        } else {
          return ASYNC_AFTER;
        }
      } else {
        if (setup.isAsyncBefore()) {
          return ASYNC_BEFORE;
        } else {
          return SIMPLE;
        }
      }
    }
  }

}
