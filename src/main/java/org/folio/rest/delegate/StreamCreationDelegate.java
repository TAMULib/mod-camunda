package org.folio.rest.delegate;

import java.time.Instant;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class StreamCreationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  private Expression isReporting;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Stream<String> primaryStream = Stream.empty();
    
    Boolean isReportingValue = (isReporting) != null ? Boolean.parseBoolean(isReporting.getValue(execution).toString())
        : false;
    
    String primaryStreamId = streamService.createStream(primaryStream);

    execution.setVariable("primaryStreamId", primaryStreamId);
    execution.setVariable("isReporting", isReportingValue);

    if (isReportingValue) {
      log.info("Reporting enabled");
      streamService.appendToReport(primaryStreamId, String.format("Beginning Streaming Report at %s", Instant.now()));
    } else {
      log.info("Reporting disabled");
    }
    log.info("CREATED PRIMARY STREAM");
  }

  public void setIsReporting(Expression isReporting) {
    this.isReporting = isReporting;
  }

}