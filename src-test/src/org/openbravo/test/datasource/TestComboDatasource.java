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
 * All portions are Copyright (C) 2014-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.datasource;

/**
 * Test cases for ComboTableDatasourceService
 * 
 * @author Shankar Balachandran 
 */

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.json.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestComboDatasource extends BaseDataSourceTestDal {

  private static final Logger log = LoggerFactory.getLogger(TestComboDatasource.class);

  /**
   * Test to fetch values from ComboTableDatasoureService using set parameters. Based on field
   * information and current context, the field values are returned as jsonObject. The test case
   * asserts the case where startRow and endRow parameters are not present. In this case OBException
   * is raised
   * 
   * @throws Exception
   */
  @Test
  public void testFetchWithoutLimitParameters() throws Exception {
    setOBContext("100");
    // Using values of window dropdown in preference window
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "876");
    params.put("_operationType", "fetch");

    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertFalse(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
  }

  /**
   * Test to fetch values from ComboTableDatasoureService using set parameters. Based on field
   * information and current context, the field values are returned as jsonObject. The test case
   * asserts the case where more than 500 records are set to be fetched. In this case OBException is
   * raised.
   * 
   * @throws Exception
   */
  @Test
  public void testFetchWithLargeData() throws Exception {
    // setContext("100");
    // Using values of window dropdown in preference window
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "876");
    params.put("_operationType", "fetch");
    params.put("_startRow", "1");
    params.put("_endRow", "1000");

    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertFalse(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
  }

  /**
   * Test to fetch paginated values from ComboTableDatasoureService
   * 
   * @throws Exception
   */
  @Test
  public void testPaginatedFetch() throws Exception {
    setOBContext("100");
    // Using values of window dropdown in preference window
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "876");
    params.put("_operationType", "fetch");
    params.put("_startRow", "76");
    params.put("_endRow", "150");

    JSONObject jsonResponse = requestCombo(params);
    JSONArray data = getData(jsonResponse);
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
    assertEquals("paginated combo number of records", 75, data.length());
  }

  /**
   * Checks selected value not in 1st page
   * 
   * see issue #27233
   */
  public void testDefaultNotInFirstPage() throws Exception {
    String US_ID = "100";

    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "1005500085"); // BP > Account > Country
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "75");
    params.put("_currentValue", US_ID); // US

    JSONObject jsonResponse = requestCombo(params);
    JSONArray data = getData(jsonResponse);
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));

    int totalRows = jsonResponse.getJSONObject("response").getInt("totalRows"); // 78
    int endRow = jsonResponse.getJSONObject("response").getInt("endRow"); // 76
    assertTrue("more than one page shoudl be detected", totalRows > endRow + 1);
    String lastRowId = data.getJSONObject(data.length() - 1).getString("id");
    assertFalse(
        "selected record should not be added at the end of 1st page, because it is in a page after it",
        lastRowId.equals(US_ID));
  }

  /**
   * Test to check filtering of the record using passed parameter
   * 
   * @throws Exception
   */
  @Test
  public void testFilter() throws Exception {
    setOBContext("100");
    // Set F&B International Admin role
    changeProfile("42D0EEB1C66F497A90DD526DC597E6F0", "192", "E443A31992CB4635AFCAEABE7183CE85",
        "B2D40D8A5D644DD89E329DC297309055");
    // Using values of visible at user in preference
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "927D156048246E92E040A8C0CF071D3D");
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "10");
    // try to filter by string 'Jo'
    params.put("criteria",
        "{\"fieldName\":\"_identifier\",\"operator\":\"iStartsWith\",\"value\":\"Jo\"}");

    JSONObject jsonResponse = requestCombo(params);
    JSONArray data = getData(jsonResponse);
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
    assertEquals("number of filtered records", 3, data.length());
  }

  /**
   * Test to check filtering of the record using passed parameter and return paginated results
   * 
   * @throws Exception
   */
  @Test
  public void testFilterWithPagination() throws Exception {
    setOBContext("100");
    // Set F&B International Admin role
    changeProfile("42D0EEB1C66F497A90DD526DC597E6F0", "192", "E443A31992CB4635AFCAEABE7183CE85",
        "B2D40D8A5D644DD89E329DC297309055");
    // Using values of visible at user in preference
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "927D156048246E92E040A8C0CF071D3D");
    params.put("_operationType", "fetch");
    // try to filter by string 'Jo'
    params.put("criteria",
        "{\"fieldName\":\"_identifier\",\"operator\":\"iStartsWith\",\"value\":\"Jo\"}");
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    JSONObject jsonResponse = requestCombo(params);
    JSONArray data = getData(jsonResponse);
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
    assertEquals("number of filtered records", 2, data.length());
  }

  /**
   * Test to check whether data is accessible to unauthorized user.
   * 
   * @throws Exception
   */
  @Test
  public void testAccess() throws Exception {
    setOBContext("100");
    // Using values of window dropdown in menu
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "206");
    params.put("columnValue", "233");
    params.put("_operationType", "fetch");
    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    // error should be raised
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR)));
  }

  /**
   * Test to check whether filter data is accessible to unauthorized user.
   * 
   * @throws Exception
   */
  @Test
  public void testAccessForFilter() throws Exception {
    setOBContext("100");
    // Using values of window dropdown in menu
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "206");
    params.put("columnValue", "233");
    params.put("_operationType", "fetch");
    // try to filter by string 'Me'
    params.put("_identifier", "Me");
    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    // error should be raised
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR)));
  }

  /**
   * Test to check when empty value is rendered in UIDefinition, proper data is returned. Refer
   * issue https://issues.openbravo.com/view.php?id=27612
   * 
   * @throws Exception
   */
  @Test
  public void testForIssue27612() throws Exception {
    String ficRequest = "/org.openbravo.client.kernel?_action=org.openbravo.client.application.window.FormInitializationComponent" //
        + "&MODE=CHANGE" //
        + "&PARENT_ID=null" //
        + "&TAB_ID=186" // Sales Order
        + "&ROW_ID=null" //
        + "&CHANGED_COLUMN=inpadOrgId";

    // Executes a FIC in mode change for an empty ad_org, before the fix it returned default value,
    // now it should return empty

    String response = doRequest(ficRequest, new HashMap<String, String>(), 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertTrue(jsonResponse.toString() != null);
    assertThat(jsonResponse.getJSONObject("columnValues").getJSONObject("AD_Org_ID")
        .getString("id"), isEmptyOrNullString());
  }

  /**
   * Tests a request to a combo with a role that has no access to field entity. This case was
   * failing (see issue #27057)
   */
  @Test
  public void testRequestWithoutFieldAccess() throws Exception {
    // Set employee role
    changeProfile("D615084948E046E3A439915008F464A6", "192", "E443A31992CB4635AFCAEABE7183CE85",
        "B2D40D8A5D644DD89E329DC297309055");

    // Fetching Price List combo in Requisition window
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "803817");
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "75");
    JSONObject jsonResponse = requestCombo(params);
    assertTrue("Combo should have data", getData(jsonResponse).length() > 0);
  }

  private JSONObject requestCombo(Map<String, String> params) throws Exception {
    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertTrue(jsonResponse.toString() != null);

    if (log.isDebugEnabled() || true) {
      String paramStr = "";
      for (String paramKey : params.keySet()) {
        paramStr += paramStr.isEmpty() ? "" : ", ";
        paramStr += "{" + paramKey + ":" + params.get(paramKey) + "}";
      }
      paramStr = "[" + paramStr + "]";
      log.debug("Combo request:\n  *params:{}\n  *response:{}", paramStr, jsonResponse);
    }
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
    assertNotNull("Combo response shoulnd't be null", jsonResponse.toString());
    return jsonResponse;
  }

  /**
   * 
   * @param jsonResponse
   * @return data of the json response
   * @throws JSONException
   */
  private JSONArray getData(JSONObject jsonResponse) throws JSONException {
    JSONArray data = jsonResponse.getJSONObject("response").getJSONArray("data");
    return data;
  }

  /**
   * 
   * @param jsonResponse
   * @return status of the json response
   * @throws JSONException
   */
  private String getStatus(JSONObject jsonResponse) throws JSONException {
    return jsonResponse.getJSONObject("response").get("status").toString();
  }

  private void setOBContext(String user) {
    OBContext.setOBContext("100");
  }

}