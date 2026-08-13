package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.folio.rest.workflow.model.ProcessorTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ProcessorDelegate extends AbstractWorkflowIODelegate {

  private ScriptEngineService scriptEngineService;

  private Expression processor;

  /**
   * Initializer.
   */
  public ProcessorDelegate(ObjectMapper objectMapper, RuntimeService runtimeService, ScriptEngineService scriptEngineService) {

    super(objectMapper, runtimeService);

    this.scriptEngineService = scriptEngineService;
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    EmbeddedProcessor processorValue = objectMapper.readValue(this.processor.getValue(execution).toString(), EmbeddedProcessor.class);

    String scriptName = processorValue.getFunctionName();

    String scriptTypeExtension = processorValue.getScriptType().getExtension();

    Map<String, Object> inputs = getInputs(execution);

    JsonNode input = objectMapper.valueToTree(inputs);

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

    setOutput(execution, output);

    determineEndTime(execution, startTime);
  }

  public void setProcessor(Expression processor) {
    this.processor = processor;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}
