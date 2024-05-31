package org.folio.rest.camunda.delegate;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.folio.rest.workflow.model.ProcessorTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
@Scope("prototype")
public class ProcessorDelegate extends AbstractWorkflowIODelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression processor;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    getLogger().info("{} started", delegateName);

    EmbeddedProcessor processorValue = objectMapper.readValue(this.processor.getValue(execution).toString(), EmbeddedProcessor.class);

    String scriptName = processorValue.getFunctionName();

    String scriptTypeExtension = processorValue.getScriptType().getExtension();

    Map<String, Object> inputs = getInputs(execution);

    JsonNode input = objectMapper.valueToTree(inputs);
    
    System.out.print("\n\n\nDEBUG: running script " + scriptTypeExtension + ", " + scriptName + "\n\n\n");

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

    setOutput(execution, output);

    long endTime = System.nanoTime();
    getLogger().info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setProcessor(Expression processor) {
    this.processor = processor;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}
