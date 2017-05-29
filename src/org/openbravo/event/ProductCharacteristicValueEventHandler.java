/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.client.kernel.event.TransactionBeginEvent;
import org.openbravo.client.kernel.event.TransactionCompletedEvent;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.service.importprocess.ImportEntryManager;

public class ProductCharacteristicValueEventHandler extends EntityPersistenceEventObserver {
  protected Logger logger = Logger.getLogger(this.getClass());
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      ProductCharacteristicValue.ENTITY_NAME) };
  private static ThreadLocal<Set<String>> prodchvalueUpdated = new ThreadLocal<Set<String>>();
  @Inject
  ImportEntryManager importEntryManager;

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onTransactionBegin(@Observes TransactionBeginEvent event) {
    prodchvalueUpdated.set(new HashSet<String>());
  }

  public void onNew(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductCharacteristicValue pchv = (ProductCharacteristicValue) event.getTargetInstance();
    addProductToList(pchv);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductCharacteristicValue pchv = (ProductCharacteristicValue) event.getTargetInstance();
    addProductToList(pchv);
  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductCharacteristicValue pchv = (ProductCharacteristicValue) event.getTargetInstance();
    addProductToList(pchv);
  }

  public void onTransactionCompleted(@Observes TransactionCompletedEvent event) {
    Set<String> productList = prodchvalueUpdated.get();
    prodchvalueUpdated.set(null);
    prodchvalueUpdated.remove();
    if (productList == null || productList.isEmpty() || event.getTransaction().wasRolledBack()) {
      return;
    }
    JSONObject entryJson = new JSONObject();
    try {
      JSONArray productIds = new JSONArray(productList);
      entryJson.put("productIds", productIds);
    } catch (JSONException ignore) {
    }
    if (!SessionHandler.getInstance().isCurrentTransactionActive()) {
      SessionHandler.getInstance().beginNewTransaction();
    }
    importEntryManager.createImportEntry(SequenceIdData.getUUID(), "VariantChDescUpdate",
        entryJson.toString(), false);
  }

  private void addProductToList(ProductCharacteristicValue pchv) {
    Set<String> productList = prodchvalueUpdated.get();
    if (productList == null) {
      productList = new HashSet<String>();
    }
    productList.add(pchv.getProduct().getId());
    prodchvalueUpdated.set(productList);
  }
}
