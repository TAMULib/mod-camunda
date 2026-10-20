package org.folio.rest.camunda.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.exception.BpmnModelFailure;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.listener.LogListener;
import org.folio.rest.camunda.listener.ScriptListener;
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
import org.folio.rest.workflow.model.components.DelegateTask;
import org.folio.rest.workflow.model.components.Event;
import org.folio.rest.workflow.model.components.Navigation;
import org.folio.rest.workflow.model.components.Task;
import org.folio.rest.workflow.model.components.Wait;
import org.operaton.bpm.engine.delegate.Expression;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.GatewayDirection;
import org.operaton.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.operaton.bpm.model.bpmn.builder.MultiInstanceLoopCharacteristicsBuilder;
import org.operaton.bpm.model.bpmn.builder.ProcessBuilder;
import org.operaton.bpm.model.bpmn.builder.ScriptTaskBuilder;
import org.operaton.bpm.model.bpmn.builder.StartEventBuilder;
import org.operaton.bpm.model.bpmn.builder.SubProcessBuilder;
import org.operaton.bpm.model.bpmn.instance.ExtensionElements;
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonField;
import org.operaton.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class BpmnModelFactory {

  private static final Logger logger = LoggerFactory.getLogger(BpmnModelFactory.class);

  private static final String SETUP_TASK_ID = "setup_task_98832611_3d33_476b_adcc_fcb6c4e8718b";

  private static final String START_EVENT = "start";

  private static final Class<?>[] SERIALIZABLE_TYPES = new Class<?>[] {
    String.class,
    Number.class,
    Boolean.class,
    Enum.class
  };

  private final JsonMapper mapper;

  private final List<AbstractWorkflowDelegate> workflowDelegates;

  /**
   * Constructor.
   *
   * @param mapper            The mapper.
   * @param workflowDelegates The delegates.
   */
  public BpmnModelFactory(JsonMapper mapper, List<AbstractWorkflowDelegate> workflowDelegates) {

    this.mapper = mapper;
    this.workflowDelegates = workflowDelegates;
  }

  public BpmnModelInstance fromWorkflow(Workflow workflow) throws ScriptTaskDeserializeCodeFailure {

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess()
      .name(workflow.getName())
      .operatonHistoryTimeToLive(workflow.getHistoryTimeToLive())
      .operatonVersionTag(workflow.getVersionTag());

    BpmnModelInstance model = build(processBuilder, workflow);

    final Iterator<Node> iter = workflow.getNodes().iterator();

    while (iter.hasNext()) {
      final Node node = iter.next();

      if (node instanceof EventSubprocess) {
        eventSubprocess(processBuilder, node);
      }
    }

    setup(model, workflow);
    expressions(model, workflow, workflow.getNodes());
    return model;
  }

  private BpmnModelInstance build(ProcessBuilder processBuilder, Workflow workflow) throws ScriptTaskDeserializeCodeFailure {
    List<Node> nodes = workflow.getNodes();

    if (nodes.isEmpty()) {
      return processBuilder.done();
    }

    AbstractFlowNodeBuilder<?, ?> builder = processBuilder.startEvent(START_EVENT);

    if (!(nodes.get(0) instanceof StartEvent)) {
      throw new BpmnModelFailure("Workflow must start with a start event!");
    }

    builder = build(builder, nodes, Setup.from(workflow.getSetup()));

    return builder.done();
  }

  private void eventSubprocess(ProcessBuilder processBuilder, Node node) throws ScriptTaskDeserializeCodeFailure {
    String identifier = node.getIdentifier();
    String name = node.getName();
    AbstractFlowNodeBuilder<?, ?> builder = processBuilder.eventSubProcess(identifier).name(name).startEvent();

    build(builder, ((EventSubprocess) node).getNodes(), Setup.NONE);
  }

  /**
   * Build the Workflow.
   *
   * @param builder The builder.
   * @param nodes   All of the nodes.
   * @param setup   The set up data.
   *
   * @return A fully constructed builder representing the Workflow.
   *
   * @throws ScriptTaskDeserializeCodeFailure On error.
   */
  private AbstractFlowNodeBuilder<?, ?> build(AbstractFlowNodeBuilder<?, ?> builder, List<Node> nodes, Setup setup)
    throws ScriptTaskDeserializeCodeFailure {

    for (Node node : nodes) {
      builder = switch (node) {
      case Event event -> buildEvent(builder, event, setup);

      case DelegateTask delegateTask -> buildDelegateTask(builder, delegateTask);

      case Branch branch -> {
        builder = switch (branch) {
        case ExclusiveGateway exclusiveGateway -> buildBranchExclusiveGateway(builder, exclusiveGateway);

        case InclusiveGateway inclusiveGateway -> buildBranchInclusiveGateway(builder, inclusiveGateway);

        case MoveToLastGateway moveToLastGateway -> buildBranchMoveToLastGateway(builder, moveToLastGateway);

        case MoveToNode moveToNode -> buildBranchMoveToNode(builder, moveToNode);

        case ParallelGateway parallelGateway -> buildBranchParallelGateway(builder, parallelGateway);

        case Subprocess subprocess -> buildBranchSubprocess(builder, subprocess);

        default -> builder;
        };

        if (!(branch instanceof Subprocess)) {
          builder = build(builder, branch.getNodes(), Setup.NONE);
        }

        yield builder;
      }

      case Condition condition -> builder.condition(condition.getAnswer(), condition.getExpression());

      case Navigation navigation -> {
        if (navigation instanceof ConnectTo connectTo) {
          builder = builder.connectTo(connectTo.getNodeId());
        } else {
          logger.warn("Navigation named {} is of an unknown type.", node.getName());
        }

        yield builder;
      }

      case Task task -> {
        if (task instanceof Wait wait) {
          if (wait instanceof ReceiveTask receiveTask) {
            builder = builder.receiveTask(receiveTask.getIdentifier()).name(receiveTask.getName())
              .message(receiveTask.getMessage());
          } else {
            logger.warn("Wait Task named {} is of an unknown type.", node.getName());
          }
        } else if (task instanceof ScriptTask scriptTask) {
          final String code;

          try {
            code = mapper.readValue(scriptTask.getCode(), String.class);
          } catch (JacksonException e) {
            throw new ScriptTaskDeserializeCodeFailure(scriptTask.getId(), scriptTask.getCode(), e);
          }

          builder = builder.scriptTask(scriptTask.getIdentifier())
            .name(scriptTask.getName())
            .scriptFormat(scriptTask.getScriptFormat())
            .scriptText(code)
            .operatonExecutionListenerClass(START_EVENT, ScriptListener.class);

          if (scriptTask.hasResultVariable()) {
            builder = ((ScriptTaskBuilder) builder).operatonResultVariable(scriptTask.getResultVariable());
          }
        } else if (task instanceof InputTask inputTask) {
          builder = builder
            .userTask(inputTask.getIdentifier())
            .name(inputTask.getName());
        } else {
          logger.warn("Script Task named {} is of an unknown type.", node.getName());
        }

        if (Boolean.TRUE.equals(task.getAsyncBefore())) {
          builder = builder.operatonAsyncBefore();
        }

        if (Boolean.TRUE.equals(task.getAsyncAfter())) {
          builder = builder.operatonAsyncAfter();
        }

        yield builder;
      }

      default -> builder;
      };
    }

    return builder;
  }

  /**
   * Builder for a ExclusiveGateway Node types.
   *
   * @param builder The builder.
   * @param eg      The ExclusiveGateway Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchExclusiveGateway(AbstractFlowNodeBuilder<?, ?> builder, ExclusiveGateway eg) {

    return builder.exclusiveGateway(eg.getIdentifier())
      .name(eg.getName())
      .gatewayDirection(GatewayDirection.valueOf(eg.getDirection().getValue()));
  }

  /**
   * Builder for a InclusiveGateway Node types.
   *
   * @param builder The builder.
   * @param ig      The InclusiveGateway Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchInclusiveGateway(AbstractFlowNodeBuilder<?, ?> builder, InclusiveGateway ig) {

    return builder.inclusiveGateway(ig.getIdentifier())
      .name(ig.getName())
      .gatewayDirection(GatewayDirection.valueOf(ig.getDirection().getValue()));
  }

  /**
   * Builder for a MoveToLastGateway Node types.
   *
   * @param builder The builder.
   * @param mlg     The MoveToLastGateway Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchMoveToLastGateway(AbstractFlowNodeBuilder<?, ?> builder, MoveToLastGateway mlg) {

    return builder.moveToLastGateway()
      .gatewayDirection(GatewayDirection.valueOf(mlg.getDirection().getValue()));
  }

  /**
   * Builder for a MoveToNode Node types.
   *
   * @param builder The builder.
   * @param mn      The MoveToNode Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchMoveToNode(AbstractFlowNodeBuilder<?, ?> builder, MoveToNode mn) {

    return builder.moveToNode(mn.getGatewayId());
  }

  /**
   * Builder for a ParallelGateway Node types.
   *
   * @param builder The builder.
   * @param pg      The ParallelGateway Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchParallelGateway(AbstractFlowNodeBuilder<?, ?> builder, ParallelGateway pg) {

    return builder.parallelGateway(pg.getIdentifier())
      .name(pg.getName())
      .gatewayDirection(GatewayDirection.valueOf(pg.getDirection().getValue()));
  }

  /**
   * Builder for a Subprocess Node types.
   *
   * @param builder The builder.
   * @param s       The Subprocess Node.
   *
   * @return The builder.
   *
   * @throws ScriptTaskDeserializeCodeFailure On error.
   */
  private AbstractFlowNodeBuilder<?, ?> buildBranchSubprocess(AbstractFlowNodeBuilder<?, ?> builder, Subprocess s)
    throws ScriptTaskDeserializeCodeFailure {

    SubProcessBuilder subProcessBuilder = builder.subProcess(s.getIdentifier()).name(s.getName());

    if (Boolean.TRUE.equals(s.getAsyncBefore())) {
      subProcessBuilder = subProcessBuilder.operatonAsyncBefore();
    }

    if (Boolean.TRUE.equals(s.getAsyncAfter())) {
      subProcessBuilder = subProcessBuilder.operatonAsyncAfter();
    }

    if (s.isMultiInstance()) {
      MultiInstanceLoopCharacteristicsBuilder multiInstanceBuilder = subProcessBuilder.multiInstance();

      EmbeddedLoopReference loopRef = s.getLoopRef();

      if (loopRef.hasCardinalityExpression()) {
        multiInstanceBuilder = multiInstanceBuilder.cardinality(loopRef.getCardinalityExpression());
      } else if (loopRef.hasDataInput()) {
        multiInstanceBuilder = multiInstanceBuilder.operatonCollection(loopRef.getDataInputRefExpression())
          .operatonElementVariable(loopRef.getInputDataName());
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

    switch (s.getType()) {
    case EMBEDDED:
      builder = subProcessBuilder.embeddedSubProcess().startEvent();
      builder = build(builder, s.getNodes(), Setup.NONE);
      builder = builder.subProcessDone();
      break;

    case TRANSACTION:
      throw new BpmnModelFailure("Transaction subprocess not yet supported!");

    default:
      logger.warn("Subprocess named {} is of an unknown type.", s.getName());
      break;
    }

    return builder;
  }

  /**
   * Builder for an DelegateTask Node type.
   *
   * @param builder The builder.
   * @param event   The Event Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildDelegateTask(AbstractFlowNodeBuilder<?, ?> builder, DelegateTask delegateTask) {

    final Iterator<AbstractWorkflowDelegate> iter = workflowDelegates.iterator();
    AbstractWorkflowDelegate delegate = null;

    while (iter.hasNext()) {
      final AbstractWorkflowDelegate d = iter.next();

      if (d != null && d.fromTask().equals(delegateTask.getClass())) {
        delegate = d;
        break;
      }
    }

    if (delegate != null) {
      builder = builder
        .serviceTask(((Node) delegateTask).getIdentifier())
        .name(((Node) delegateTask).getName())
        .operatonDelegateExpression(delegate.getExpression());
    } else {
      throw new BpmnModelFailure("Task must have delegate representation!");
    }

    if (Boolean.TRUE.equals(delegateTask.getAsyncBefore())) {
      builder = builder.operatonAsyncBefore();
    }

    if (Boolean.TRUE.equals(delegateTask.getAsyncAfter())) {
      builder = builder.operatonAsyncAfter();
    }

    return builder;
  }

  /**
   * Builder for an Event Node type.
   *
   * @param builder The builder.
   * @param event   The Event Node.
   * @param setup   The set up data.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildEvent(AbstractFlowNodeBuilder<?, ?> builder, Event event, Setup setup) {

    return switch (event) {
      case StartEvent startEvent -> buildEventStart(builder, startEvent, setup);

      case EndEvent endEvent -> buildEventEnd(builder, endEvent);

      default -> {
        logger.warn("Event named {} is of an unknown type.", ((Node) event).getName());

        yield builder;
      }
    };
  }

  /**
   * Builder for a EndEvent Node type.
   *
   * @param builder  The builder.
   * @param endEvent The end event Node.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildEventEnd(AbstractFlowNodeBuilder<?, ?> builder, EndEvent endEvent) {

    return builder
      .endEvent(endEvent.getIdentifier())
      .name(endEvent.getName())
      .operatonExecutionListenerClass(START_EVENT, LogListener.class);
  }

  /**
   * Builder for a StartEvent Node type.
   *
   * @param builder    The builder.
   * @param startEvent The start event Node.
   * @param setup      The set up data.
   *
   * @return The builder.
   */
  private AbstractFlowNodeBuilder<?, ?> buildEventStart(AbstractFlowNodeBuilder<?, ?> builder, StartEvent startEvent, Setup setup) {

    StartEventBuilder seBuilder = (StartEventBuilder) builder;

    if (Boolean.TRUE.equals(startEvent.getAsyncBefore())) {
      seBuilder = seBuilder.operatonAsyncBefore();
    }

    final boolean interrupting = Boolean.TRUE.equals(startEvent.getInterrupting());
    final StartEventType type = startEvent.getType();
    final String expression = startEvent.getExpression();

    if (type != StartEventType.NONE && expression == null) {
      throw new BpmnModelFailure(String.format("%s start event requires an expression", type));
    }

    seBuilder = seBuilder
      .id(startEvent.getIdentifier())
      .name(startEvent.getName())
      .operatonExecutionListenerClass(START_EVENT, LogListener.class);

    switch (type) {
      case MESSAGE_CORRELATION:
        seBuilder = seBuilder.message(expression).interrupting(interrupting);
        break;

      case SCHEDULED:
        seBuilder = seBuilder.timerWithCycle(expression).interrupting(interrupting);
        break;

      case SIGNAL:
        seBuilder = seBuilder.signal(expression).interrupting(interrupting);
        break;

      case NONE:
        seBuilder = seBuilder.interrupting(interrupting);
        break;

      default:
        logger.warn("Start Event named {} has an unknown event type of {}.", startEvent.getName(), type);
        break;
    }

    builder = seBuilder;

    if (!setup.equals(Setup.NONE)) {
      builder = builder.serviceTask(SETUP_TASK_ID).name("Setup").operatonDelegateExpression("${setupDelegate}");

      switch (setup) {
      case ASYNC_AFTER:
        builder = builder.operatonAsyncAfter();
        break;

      case ASYNC_BEFORE:
        builder = builder.operatonAsyncBefore();
        break;

      case ASYNC_BEFORE_AFTER:
        builder = builder.operatonAsyncBefore().operatonAsyncAfter();
        break;

      case NONE, SIMPLE:
        break;

      default:
        logger.warn("Start Event named {} has an unknown setup type of {}.", startEvent.getName(), setup);
        break;
      }
    }

    return builder;
  }

  private void setup(BpmnModelInstance model, Workflow workflow) {
    ExtensionElements extensions = model.newInstance(ExtensionElements.class);

    Map<String, JsonNode> initialContext = workflow.getInitialContext();
    OperatonField icField = model.newInstance(OperatonField.class);
    icField.setOperatonName("initialContext");
    try {
      icField.setOperatonStringValue(mapper.writeValueAsString(initialContext));
    } catch (JacksonException e) {
      logger.warn("Failed to serialize initial context");
    }
    extensions.addChildElement(icField);

    List<EmbeddedProcessor> processors = getProcessorScripts(workflow, workflow.getNodes());
    OperatonField psField = model.newInstance(OperatonField.class);
    psField.setOperatonName("processors");
    try {
      psField.setOperatonStringValue(mapper.writeValueAsString(processors));
    } catch (JacksonException e) {
      logger.warn("Failed to serialize processor scripts");
    }
    extensions.addChildElement(psField);

    ModelElementInstance element = model.getModelElementById(SETUP_TASK_ID);
    element.addChildElement(extensions);
  }

  private void expressions(BpmnModelInstance model, Workflow workflow, List<Node> nodes) {

    final Iterator<Node> iter = nodes.iterator();

    while (iter.hasNext()) {
      final Node node = iter.next();

      final Optional<AbstractWorkflowDelegate> delegate = workflowDelegates.stream()
        .filter(d -> d.fromTask().equals(node.getClass())).findAny();

      if (delegate.isPresent()) {
        final ExtensionElements extensions = model.newInstance(ExtensionElements.class);

        FieldUtils.getAllFieldsList(delegate.get().getClass()).forEach((Field df) -> {
          if (!Expression.class.isAssignableFrom(df.getType())) return;

          final Field f = FieldUtils.getField(node.getClass(), df.getName(), true);

          if (f != null) {
            final Object value;

            try {
              value = f.get(node);
            } catch (IllegalArgumentException | IllegalAccessException e) {
              throw new BpmnModelFailure(
                String.format("Workflow '%s' (%s) error: %s", workflow.getName(), workflow.getId(), e.getMessage()),
                e
              );
            }

            if (value != null) {
              final OperatonField field = model.newInstance(OperatonField.class);
              field.setOperatonName(f.getName());
              field.setOperatonStringValue(serialize(value));
              extensions.addChildElement(field);
            }
          }
        });

        model.getModelElementById(node.getIdentifier()).addChildElement(extensions);
      } else {
        if (node instanceof Branch branch) {
          expressions(model, workflow, branch.getNodes());
        } else if (node instanceof Subprocess subprocess) {
          expressions(model, workflow, subprocess.getNodes());
        } else if (node instanceof DelegateTask) {
          throw new BpmnModelFailure(
            String.format("Task must have delegate representation for Workflow '%s' (%s)!", workflow.getName(), workflow.getId()));
        }
      }
    }
  }

  private List<EmbeddedProcessor> getProcessorScripts(Workflow workflow, List<Node> nodes) {

    final List<EmbeddedProcessor> scripts = new ArrayList<>();

    nodes.forEach(node -> {
      if (node instanceof ProcessorTask processorTask) {
        scripts.add(processorTask.getProcessor());
      } else if (node instanceof Branch branch) {
        scripts.addAll(getProcessorScripts(workflow, branch.getNodes()));
      } else if (node instanceof Subprocess subprocess) {
        scripts.addAll(getProcessorScripts(workflow, subprocess.getNodes()));
      } else if (node instanceof Task) {
        logger.debug(
          "A Process Script named {} for Workflow '{}' ({}) is a non-processor task ({}).",
          node.getName(), workflow.getName(), workflow.getId(), node.getClass().getSimpleName()
        );
      } else if (node instanceof Event) {
        logger.debug(
          "A Process Script named {} for Workflow '{}' ({}) is a non-processor event ({}).",
          node.getName(), workflow.getName(), workflow.getId(), node.getClass().getSimpleName()
        );
      } else if (node == null) {
        throw new BpmnModelFailure(String.format(
          "A Process Script Node for Workflow '%s' (%s) is NULL.",
          workflow.getName(), workflow.getId())
        );
      } else {
        logger.warn(
          "A Process Script named {} for Workflow '{}' ({}) is of an unknown type ({}).",
          node.getName(),workflow.getName(), workflow.getId(), node.getClass().getSimpleName()
        );
      }
    });

    return scripts;
  }

  private String serialize(Object value) throws JacksonException {
    if (isSerializableType(value.getClass())) {
      return value.toString();
    } else {
      return mapper.writeValueAsString(value);
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
