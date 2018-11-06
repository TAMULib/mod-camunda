package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.model.FolioLogin;
import org.springframework.stereotype.Service;

@Service
public class ResetLoginTokenDelegate extends AbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Reset Login Delegate");

    FolioLogin folioLogin = (FolioLogin) execution.getVariable("folioLogin");
    folioLogin.setxOkapiToken("");

    execution.setVariable("folioLogin", folioLogin);
  }
}
