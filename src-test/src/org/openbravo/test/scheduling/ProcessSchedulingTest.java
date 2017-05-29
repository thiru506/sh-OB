/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2015-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.scheduling;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.junit.Test;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.base.weld.test.testinfrastructure.ApplicationScopedBean;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.ProcessRun;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Test cases for process schedule and process runner
 * 
 * @author alostale
 *
 */
public class ProcessSchedulingTest extends WeldBaseTest {
  private static final String anyProcessID = "800170";
  private static final String USER_ID = "100";
  private static final String ROLE_ID = "0";
  private static final String CLIENT_ID = "0";
  private static final String ORG_ID = "0";
  private static final String SOME_VALUE = "CDI is working";

  @Inject
  private ApplicationScopedBean theBean;

  /**
   * Test case to cover issue #29902. Before its fix it failed throwing an exception due to
   * ProcessRunner attempting to set process run status on an already closed connection.
   */
  @Test
  public void processRunnerNotClosingConnection() throws Exception {
    ProcessRun pr = executeBackgroundProcess(MyProcess.class);

    assertThat("Process Run is saved", pr, notNullValue());
    assertThat("Process status", pr.getStatus(), equalTo(Process.SUCCESS));
  }

  @Test
  public void processesShouldInjectBeans() throws ServletException {
    ProcessRun pr = executeBackgroundProcess(MyCDIProcess.class);

    assertThat("Process status", pr.getStatus(), equalTo(Process.SUCCESS));
    assertThat("Value should be set in injected bean", theBean.getValue(), equalTo(SOME_VALUE));
  }

  private <P extends DalBaseProcess> ProcessRun executeBackgroundProcess(Class<P> processClass)
      throws ServletException {
    DalConnectionProvider conn = new DalConnectionProvider();
    VariablesSecureApp vsa = new VariablesSecureApp(USER_ID, CLIENT_ID, ORG_ID, ROLE_ID);
    ProcessBundle bundle = new ProcessBundle(anyProcessID, vsa);

    bundle.setProcessClass(processClass);
    bundle.setParams(new HashMap<String, Object>());
    bundle.setConnection(conn);
    bundle.setLog(new ProcessLogger(conn));

    // do not close the connection after executing the process
    bundle.setCloseConnection(false);

    // invoke the process through ProcessRunner
    String executionId = new ProcessRunner(bundle).execute(new DalConnectionProvider());

    return OBDal.getInstance().get(ProcessRun.class, executionId);
  }

  /** Fake process */
  public static class MyProcess extends DalBaseProcess {
    @Override
    protected void doExecute(ProcessBundle bundle) throws Exception {
      // do nothing
    }
  }

  /** Fake process with CDI */
  public static class MyCDIProcess extends DalBaseProcess {
    @Inject
    private ApplicationScopedBean appScopedBean;

    @Override
    protected void doExecute(ProcessBundle bundle) throws Exception {
      appScopedBean.setValue(SOME_VALUE);
    }
  }
}
