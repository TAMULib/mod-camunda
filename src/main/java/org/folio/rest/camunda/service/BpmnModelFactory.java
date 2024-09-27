package org.folio.rest.camunda.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Convert;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.MultiInstanceLoopCharacteristicsBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.ScriptTaskBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.SubProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.workflow.enums.StartEventType;
import org.folio.rest.workflow.model.Condition;
import org.folio.rest.workflow.model.ConnectTo;
import org.folio.rest.workflow.model.EmbeddedLoopReference;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.EventSubprocess;
import org.folio.rest.workflow.model.ExclusiveGateway;
import org.folio.rest.workflow.model.InclusiveGateway;
import org.folio.rest.workflow.model.InputTask;
import org.folio.rest.workflow.model.MoveToLastGateway;
import org.folio.rest.workflow.model.MoveToNode;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.ParallelGateway;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.ReceiveTask;
import org.folio.rest.workflow.model.ScriptTask;
import org.folio.rest.workflow.model.StartEvent;
import org.folio.rest.workflow.model.Subprocess;
import org.folio.rest.workflow.model.Workflow;
import org.folio.rest.workflow.model.components.Branch;
import org.folio.rest.workflow.model.components.Conditional;
import org.folio.rest.workflow.model.components.DelegateTask;
import org.folio.rest.workflow.model.components.Event;
import org.folio.rest.workflow.model.components.Gateway;
import org.folio.rest.workflow.model.components.Navigation;
import org.folio.rest.workflow.model.components.Task;
import org.folio.rest.workflow.model.components.Wait;
import org.folio.rest.workflow.model.converter.CryptoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  private CryptoConverter cryptoConverter;

  @Autowired
  private List<AbstractWorkflowDelegate> workflowDelegates;

  public BpmnModelInstance fromWorkflow(Workflow workflow) throws ScriptTaskDeserializeCodeFailure {

    // @formatter:off
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess().name(workflow.getName())
        .camundaHistoryTimeToLive(workflow.getHistoryTimeToLive())
        .camundaVersionTag(workflow.getVersionTag());
    // @formatter:on

    BpmnModelInstance model = build(processBuilder, workflow);

    // @formatter:off
    workflow.getNodes().stream()
      .filter(node -> node instanceof EventSubprocess)
      .forEach(subprocess -> {
        try {
            eventSubprocess(processBuilder, subprocess);
        } catch (ScriptTaskDeserializeCodeFailure e) {
            throw new RuntimeException(e);
        }
    });
    // @formatter:on

    setup(model, workflow);
    expressions(model, workflow.getNodes());

    return model;
  }

  private BpmnModelInstance build(ProcessBuilder processBuilder, Workflow workflow) throws ScriptTaskDeserializeCodeFailure {
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

  private void eventSubprocess(ProcessBuilder processBuilder, Node node) throws ScriptTaskDeserializeCodeFailure {
    AbstractFlowNodeBuilder<?, ?> builder = null;
    String identifier = node.getIdentifier();
    String name = node.getName();
    builder = processBuilder.eventSubProcess(identifier).name(name).startEvent();
    builder = build(builder, ((EventSubprocess) node).getNodes(), Setup.NONE);
  }

  private AbstractFlowNodeBuilder<?, ?> build(AbstractFlowNodeBuilder<?, ?> builder, List<Node> nodes, Setup setup) throws ScriptTaskDeserializeCodeFailure {

    for (Node node : nodes) {

      if (node instanceof Event) {

        if (node instanceof StartEvent) {
          if (Boolean.TRUE.equals(((StartEvent) node).getAsyncBefore())) {
            builder = builder.camundaAsyncBefore();
          }

          boolean interrupting = Boolean.TRUE.equals(((StartEvent) node).getInterrupting());

          StartEventType type = ((StartEvent) node).getType();
          String expression = ((StartEvent) node).getExpression();

          if (type != StartEventType.NONE && expression == null) {
            // TODO: create custom exception and controller advice to handle better
            throw new RuntimeException(String.format("%s start event requires an expression", type));
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
            logger.warn("Start Event named {} has an unknown event type of {}.", node.getName(), type);
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
              break;
            default:
              logger.warn("Start Event named {} has an unknown setup type of {}.", node.getName(), setup);
              break;
            }
          }

        } else if (node instanceof EndEvent) {
          builder = builder.endEvent(node.getIdentifier()).name(node.getName());
        } else {
          logger.warn("Event named {} is of an unknown type.", node.getName());
        }

      } else if (node instanceof DelegateTask) {

        Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
            .filter(d -> d.fromTask().equals(node.getClass())).findAny();

        if (delegate.isPresent()) {
          builder = builder.serviceTask(node.getIdentifier()).name(node.getName())
              .camundaDelegateExpression(delegate.get().getExpression());
        } else {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException("Task must have delegate representation!");
        }

        if (Boolean.TRUE.equals(((DelegateTask) node).getAsyncBefore())) {
          builder = builder.camundaAsyncBefore();
        }

        if (Boolean.TRUE.equals(((DelegateTask) node).getAsyncAfter())) {
          builder = builder.camundaAsyncAfter();
        }

      } else if (node instanceof Branch) {

        if (node instanceof ExclusiveGateway) {

          builder = builder.exclusiveGateway(node.getIdentifier())
            .name(node.getName())
            .gatewayDirection(GatewayDirection.valueOf(((Gateway) node).getDirection().getValue()));

        } else if (node instanceof InclusiveGateway) {

          builder = builder.inclusiveGateway(node.getIdentifier())
            .name(node.getName())
            .gatewayDirection(GatewayDirection.valueOf(((Gateway) node).getDirection().getValue()));

        } else if (node instanceof MoveToLastGateway) {

          builder = builder.moveToLastGateway()
            .gatewayDirection(GatewayDirection.valueOf(((Gateway) node).getDirection().getValue()));

        } else if (node instanceof ParallelGateway) {

          builder = builder.parallelGateway(node.getIdentifier())
            .name(node.getName())
            .gatewayDirection(GatewayDirection.valueOf(((Gateway) node).getDirection().getValue()));

        } else if (node instanceof MoveToNode) {

          builder = builder.moveToNode(((MoveToNode) node)
            .getGatewayId());

        } else if ((node instanceof Subprocess)) {

          SubProcessBuilder subProcessBuilder = builder.subProcess(node.getIdentifier()).name(node.getName());

          if (Boolean.TRUE.equals(((Subprocess) node).getAsyncBefore())) {
            subProcessBuilder = subProcessBuilder.camundaAsyncBefore();
          }

          if (Boolean.TRUE.equals(((Subprocess) node).getAsyncAfter())) {
            subProcessBuilder = subProcessBuilder.camundaAsyncAfter();
          }

          if (((Subprocess) node).isMultiInstance()) {
            MultiInstanceLoopCharacteristicsBuilder multiInstanceBuilder = subProcessBuilder.multiInstance();

            EmbeddedLoopReference loopRef = ((Subprocess) node).getLoopRef();

            if (loopRef.hasCardinalityExpression()) {
              multiInstanceBuilder = multiInstanceBuilder.cardinality(loopRef.getCardinalityExpression());
            } else if (loopRef.hasDataInput()) {
              multiInstanceBuilder = multiInstanceBuilder.camundaCollection(loopRef.getDataInputRefExpression())
                  .camundaElementVariable(loopRef.getInputDataName());
            }

            if (Boolean.TRUE.equals(loopRef.getParallel())) {
              multiInstanceBuilder = multiInstanceBuilder.parallel();
            } else {
              multiInstanceBuilder = multiInstanceBuilder.sequential();
            }

            if (loopRef.hasCompleteConditionExpression()) {
              multiInstanceBuilder = multiInstanceBuilder.completionCondition(loopRef.getCompleteConditionExpression());
            }

            subProcessBuilder = multiInstanceBuilder.multiInstanceDone();
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
            logger.warn("Subprocess named {} is of an unknown type.", node.getName());
            break;
          }

        }

        if (!(node instanceof Subprocess)) {
          builder = build(builder, ((Branch) node).getNodes(), Setup.NONE);
        }

      } else if (node instanceof Condition) {
        builder = builder.condition(((Conditional) node).getAnswer(), ((Conditional) node).getExpression());
      } else if (node instanceof Navigation) {

        if (node instanceof ConnectTo) {

          // TODO: figure out way to get identifier from id
          builder = builder.connectTo(((ConnectTo) node).getNodeId());

        } else {
          logger.warn("Navigation named {} is of an unknown type.", node.getName());
        }
      } else if (node instanceof Task) {
        if (node instanceof Wait) {
          if (node instanceof ReceiveTask) {
            builder = builder.receiveTask(node.getIdentifier()).name(node.getName())
              .message(((ReceiveTask) node).getMessage());
          } else {
            logger.warn("Wait Task named {} is of an unknown type.", node.getName());
          }
        } else if (node instanceof ScriptTask) {
          String code;
          try {
            code = objectMapper.readValue(((ScriptTask) node).getCode(), String.class);
          } catch (JsonProcessingException e) {
            throw new ScriptTaskDeserializeCodeFailure(node.getId(), e);
          }

          builder = builder.scriptTask(node.getIdentifier()).name(node.getName())
            .scriptFormat(((ScriptTask) node).getScriptFormat()).scriptText(code);

          if (((ScriptTask) node).hasResultVariable()) {
            builder = ((ScriptTaskBuilder) builder).camundaResultVariable(((ScriptTask) node).getResultVariable());
          }
        } else if (node instanceof InputTask) {
          builder = builder
            .userTask(node.getIdentifier())
            .name(node.getName());
        } else {
          logger.warn("Script Task named {} is of an unknown type.", node.getName());
        }

        if (Boolean.TRUE.equals(((Task) node).getAsyncBefore())) {
          builder = builder.camundaAsyncBefore();
        }

        if (Boolean.TRUE.equals(((Task) node).getAsyncAfter())) {
          builder = builder.camundaAsyncAfter();
        }
      }
    }

    // must end in end event or connect to

    return builder;
  }

  private void setup(BpmnModelInstance model, Workflow workflow) {
    ExtensionElements extensions = model.newInstance(ExtensionElements.class);

    Map<String, JsonNode> initialContext = workflow.getInitialContext();
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
            .map(df -> FieldUtils.getField(node.getClass(), df.getName(), true))
            .filter(f -> Objects.nonNull(f))
            .forEach(f -> {
              try {
                Object value = f.get(node);
                if (Objects.nonNull(value)) {
                  CamundaField field = model.newInstance(CamundaField.class);
                  field.setCamundaName(f.getName());

                  Convert convert = f.getAnnotation(Convert.class);
                  if (convert != null && CryptoConverter.class.isAssignableFrom(convert.converter()) && String.class.isAssignableFrom(value.getClass())) {
                    field.setCamundaExpression(String.format("${cryptoConverter.decrypt(\"%s\")}", cryptoConverter.encrypt(serialize(value))));
                  } else {
                    field.setCamundaStringValue(serialize(value));
                  }

                  extensions.addChildElement(field);
                }
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
        } else if (node instanceof DelegateTask) {
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
      } else if (node instanceof Branch) {
        scripts.addAll(getProcessorScripts(((Branch) node).getNodes()));
      } else if (node instanceof Subprocess) {
        scripts.addAll(getProcessorScripts(((Subprocess) node).getNodes()));
      } else if (node instanceof Task) {
        logger.warn("Processor Script named {} is a non-processor task.", node.getName());
      }
      else {
        logger.warn("Processor Script named {} is of an unknown type.", node.getName());
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
      if (Boolean.TRUE.equals(setup.getAsyncAfter())) {
        if (Boolean.TRUE.equals(setup.getAsyncBefore())) {
          return ASYNC_BEFORE_AFTER;
        }

        return ASYNC_AFTER;
      } else {
        if (Boolean.TRUE.equals(setup.getAsyncBefore())) {
          return ASYNC_BEFORE;
        }

        return SIMPLE;
      }
    }
  }

}
