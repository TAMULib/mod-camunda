package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class StreamConsumerDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  public StreamConsumerDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    log.info("{} STARTED", delegateName);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    streamService.toJsonNodeFlux(streamService.getFlux(primaryStreamId)).doFinally(r -> {
      log.info("Stream consumption completed");
    }).subscribe();
  }

}