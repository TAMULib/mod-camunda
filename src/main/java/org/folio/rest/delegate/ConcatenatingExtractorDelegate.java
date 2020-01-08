package org.folio.rest.delegate;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/*
 *  This delegate concatenates a new stream of data to the end of the primary stream
 */
@Service
@Scope("prototype")
public class ConcatenatingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private StreamService streamService;

  public ConcatenatingExtractorDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Stream<String> newStream = this.getStream(execution);
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");
    streamService.concatenateStream(primaryStreamId, newStream);
  }

}
