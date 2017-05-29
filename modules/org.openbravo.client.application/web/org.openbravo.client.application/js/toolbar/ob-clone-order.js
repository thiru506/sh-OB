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
 * All portions are Copyright (C) 2011-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):   Sreedhar Sirigiri (TDS), Mallikarjun M (TDS)
 ************************************************************************
 */

// Create a button to clone a sales order ('Sales Order' tab)
OB.ToolbarUtils.createCloneButton('org.openbravo.client.application.businesslogic.CloneOrderActionHandler', null, ['186', '294'], OB.I18N.getLabel('OBUIAPP_WantToCloneOrder'));