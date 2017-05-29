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
 * All portions are Copyright (C) 2001-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * @author Fernando Iriazabal
 * 
 *         This class builds the queries for populating the different kind of combos in the
 *         application.
 */
public class ComboTableData {
  private static Logger log4j = Logger.getLogger(ComboTableData.class);
  private final String internalPrefix = "@@";
  private static final String FIELD_CONCAT = " || ' - ' || ";
  private static final String INACTIVE_DATA = "**";
  private VariablesSecureApp vars;
  private Hashtable<String, String> parameters = new Hashtable<String, String>();
  private Vector<QueryParameterStructure> paramSelect = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramFrom = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramWhere = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramOrderBy = new Vector<QueryParameterStructure>();
  private Vector<QueryFieldStructure> select = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> from = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> where = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> orderBy = new Vector<QueryFieldStructure>();
  private boolean canBeCached;
  public int index = 0;

  /**
   * Constructor
   */
  public ComboTableData() {
  }

  /**
   * Constructor
   * 
   * @param _conn
   *          Object with the database connection methods.
   * @param _referenceType
   *          String with the type of reference.
   * @param _name
   *          String with the Object name.
   * @param _objectReference
   *          String with id to the reference value.
   * @param _validation
   *          String with the id to the validation.
   * @param _orgList
   *          String with the list of granted organizations.
   * @param _clientList
   *          String with the list of granted clients.
   * @param _index
   *          String with the id of the default value for the combo.
   * @throws Exception
   */
  public ComboTableData(ConnectionProvider _conn, String _referenceType, String _name,
      String _objectReference, String _validation, String _orgList, String _clientList, int _index)
      throws Exception {
    this(null, _conn, _referenceType, _name, _objectReference, _validation, _orgList, _clientList,
        _index);
  }

  /**
   * Constructor
   * 
   * @param _vars
   *          Object with the session methods.
   * @param _conn
   *          Object with the database connection methods.
   * @param _referenceType
   *          String with the type of reference.
   * @param _name
   *          String with the Object name.
   * @param _objectReference
   *          String with id to the reference value.
   * @param _validation
   *          String with the id to the validation.
   * @param _orgList
   *          String with the list of granted organizations.
   * @param _clientList
   *          String with the list of granted clients.
   * @param _index
   *          String with the id of the default value for the combo.
   * @throws Exception
   */
  public ComboTableData(VariablesSecureApp _vars, ConnectionProvider _conn, String _referenceType,
      String _name, String _objectReference, String _validation, String _orgList,
      String _clientList, int _index) throws Exception {
    if (_vars != null)
      setVars(_vars);
    setReferenceType(_referenceType);
    setObjectName(_name);
    setObjectReference(_objectReference);
    setValidation(_validation);
    setOrgList(_orgList);
    setClientList(_clientList);
    setIndex(_index);
    generateSQL();
    parseNames();
  }

  /**
   * Setter for the session object.
   * 
   * @param _vars
   *          New session object.
   * @throws Exception
   */
  private void setVars(VariablesSecureApp _vars) throws Exception {
    if (_vars == null)
      throw new Exception("The session vars is null");
    this.vars = _vars;
  }

  /**
   * Getter for the session object.
   * 
   * @return Session object.
   */
  public VariablesSecureApp getVars() {
    return this.vars;
  }

  /**
   * Getter for the database handler object.
   * 
   * @return Database handler object.
   */
  public ConnectionProvider getPool() {
    return new DalConnectionProvider(false);
  }

  /**
   * Setter for the reference type id.
   * 
   * @param _reference
   *          String with the new reference
   * @throws Exception
   */
  private void setReferenceType(String _reference) throws Exception {
    String localReference = _reference;
    if (localReference != null && !localReference.equals("")) {
      try {
        Integer.valueOf(localReference).intValue();
      } catch (Exception ignore) {
        if (!Utility.isUUIDString(localReference))
          localReference = ComboTableQueryData.getBaseReferenceID(getPool(), localReference);
      }
    }
    setParameter(internalPrefix + "reference", localReference);
  }

  /**
   * Getter for the reference type id.
   * 
   * @return String with the reference type id.
   */
  private String getReferenceType() {
    return getParameter(internalPrefix + "reference");
  }

  /**
   * Setter for the object name.
   * 
   * @param _name
   *          String with the new object name.
   * @throws Exception
   */
  private void setObjectName(String _name) throws Exception {
    setParameter(internalPrefix + "name", _name);
  }

  /**
   * Getter for the object name.
   * 
   * @return String with the object name.
   */
  public String getObjectName() {
    return getParameter(internalPrefix + "name");
  }

  /**
   * Setter for the object reference id.
   * 
   * @param _reference
   *          String with the new object reference id.
   * @throws Exception
   */
  private void setObjectReference(String _reference) throws Exception {
    String localReference = _reference;
    if (localReference != null && !localReference.equals("")) {
      try {
        Integer.valueOf(localReference).intValue();
      } catch (Exception ignore) {
        if (!Utility.isUUIDString(localReference)) {
          // Looking reference by name! This shouldn't be used, name is prone to change. It only
          // looks in core names
          localReference = ComboTableQueryData.getReferenceID(getPool(), localReference,
              getReferenceType());
          if (localReference == null || localReference.equals("")) {
            throw new OBException(Utility.messageBD(getPool(), "ReferenceNotFound",
                vars.getLanguage())
                + " " + localReference);
          }
        }
      }
    }
    setParameter(internalPrefix + "objectReference", localReference);
  }

  /**
   * Getter for the object reference id.
   * 
   * @return String with the object reference id.
   */
  public String getObjectReference() {
    return getParameter(internalPrefix + "objectReference");
  }

  /**
   * Setter for the validation id.
   * 
   * @param _reference
   *          String for the new validation id.
   * @throws Exception
   */
  private void setValidation(String _reference) throws Exception {
    String localReference = _reference;
    if (localReference != null && !localReference.equals("")) {
      try {
        Integer.valueOf(localReference).intValue();
      } catch (Exception ignore) {
        if (!Utility.isUUIDString(localReference))
          localReference = ComboTableQueryData.getValidationID(getPool(), localReference);
      }
    }
    setParameter(internalPrefix + "validation", localReference);
  }

  /**
   * Getter for the validation id.
   * 
   * @return String with the validation id.
   */
  private String getValidation() {
    return getParameter(internalPrefix + "validation");
  }

  /**
   * Setter for the granted organizations list.
   * 
   * @param _orgList
   *          String with the new granted organizations list.
   * @throws Exception
   */
  private void setOrgList(String _orgList) throws Exception {
    setParameter(internalPrefix + "orgList", _orgList);
  }

  /**
   * Getter for the granted organizations list.
   * 
   * @return String with the granted organizations list.
   */
  public String getOrgList() {
    return getParameter(internalPrefix + "orgList");
  }

  /**
   * Setter for the granted clients list.
   * 
   * @param _clientList
   *          String with the new granted clients list.
   * @throws Exception
   */
  private void setClientList(String _clientList) throws Exception {
    setParameter(internalPrefix + "clientList", _clientList);
  }

  /**
   * Getter for the granted clients list.
   * 
   * @return String with the granted clients list.
   */
  public String getClientList() {
    return getParameter(internalPrefix + "clientList");
  }

  /**
   * Adds new field to the select section of the query.
   * 
   * @param _field
   *          String with the field.
   * @param _alias
   *          String with the alias for this field.
   */
  public void addSelectField(String _field, String _alias) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " AS ", _alias, "SELECT");
    if (this.select == null)
      this.select = new Vector<QueryFieldStructure>();
    select.addElement(p);
  }

  /**
   * Gets the defined fields for the select section of the query.
   * 
   * @return Vector with the select's fields.
   */
  private Vector<QueryFieldStructure> getSelectFields() {
    return this.select;
  }

  /**
   * Adds new field to the from section of the query.
   * 
   * @param _field
   *          String with the field.
   * @param _alias
   *          String with the alias for the field.
   */
  public void addFromField(String _field, String _alias) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " ", _alias, "FROM");
    if (this.from == null)
      this.from = new Vector<QueryFieldStructure>();
    from.addElement(p);
  }

  /**
   * Gets the defined fields for the from section of the query.
   * 
   * @return Vector with the from's fields.
   */
  private Vector<QueryFieldStructure> getFromFields() {
    return this.from;
  }

  /**
   * Adds new field to the where section of the query.
   * 
   * @param _field
   *          String with the field.
   * @param _type
   *          String for group fields.
   */
  public void addWhereField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.where == null)
      this.where = new Vector<QueryFieldStructure>();
    where.addElement(p);
  }

  /**
   * Gets the defined fields for the where section of the query.
   * 
   * @return Vector with the where's fields.
   */
  private Vector<QueryFieldStructure> getWhereFields() {
    return this.where;
  }

  /**
   * Adds new field to the order by section of the query.
   * 
   * @param _field
   *          String with the field.
   */
  public void addOrderByField(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.orderBy == null)
      this.orderBy = new Vector<QueryFieldStructure>();
    orderBy.addElement(p);
  }

  /**
   * Gets the defined fields for the order by section of the query.
   * 
   * @return Vector with the order by's fields.
   */
  private Vector<QueryFieldStructure> getOrderByFields() {
    return this.orderBy;
  }

  /**
   * Gets all the defined parameters for the select section.
   * 
   * @return Vector with the parameters.
   */
  private Vector<QueryParameterStructure> getSelectParameters() {
    return this.paramSelect;
  }

  /**
   * Adds a new parameter to the from section of the query.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the name od the field.
   */
  public void addFromParameter(String _parameter, String _fieldName) {
    if (this.paramFrom == null)
      this.paramFrom = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "FROM");
    paramFrom.addElement(aux);
  }

  /**
   * Gets the defined parameters for the from section.
   * 
   * @return Vector with the parameters.
   */
  private Vector<QueryParameterStructure> getFromParameters() {
    return this.paramFrom;
  }

  /**
   * Adds a new parameter to the where section of the query.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the name of the field.
   * @param _type
   *          String with a group name.
   */
  public void addWhereParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramWhere == null)
      this.paramWhere = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    paramWhere.addElement(aux);
  }

  /**
   * Gets the parameters defined for the where section.
   * 
   * @return Vector with the parameters.
   */
  private Vector<QueryParameterStructure> getWhereParameters() {
    return this.paramWhere;
  }

  /**
   * Adds a new parameter to the order by section of the query.
   * 
   * @param _parameter
   *          String with the parameter.
   * @param _fieldName
   *          String with the name of the field.
   */
  private void addOrderByParameter(String _parameter, String _fieldName) {
    if (this.paramOrderBy == null)
      this.paramOrderBy = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "ORDERBY");
    paramOrderBy.addElement(aux);
  }

  /**
   * Gets the parameters for the order by section.
   * 
   * @return Vector with the parameters.
   */
  private Vector<QueryParameterStructure> getOrderByParameters() {
    return this.paramOrderBy;
  }

  /**
   * Setter for the parameters value.
   * 
   * @param name
   *          The name of the field defined for the parameter.
   * @param value
   *          The value for this parameter.
   * @throws Exception
   */
  public void setParameter(String name, String value) throws Exception {
    if (name == null || name.equals(""))
      throw new Exception("Invalid parameter name");
    if (this.parameters == null)
      this.parameters = new Hashtable<String, String>();
    if (value == null || value.equals(""))
      this.parameters.remove(name.toUpperCase());
    else
      this.parameters.put(name.toUpperCase(), value);
  }

  /**
   * Getter for the parameters value.
   * 
   * @param name
   *          The name of the field defined for the parameter.
   * @return String with the value.
   */
  private String getParameter(String name) {
    if (name == null || name.equals(""))
      return "";
    else if (this.parameters == null)
      return "";
    else
      return this.parameters.get(name.toUpperCase());
  }

  /**
   * Gets the values for all of the defined parameters in the query.
   * 
   * @return Vector with the values.
   */
  Vector<String> getParameters() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled())
      log4j.debug("Obtaining parameters");
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Select parameters obtained");
    vAux = getFromParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("From parameters obtained");
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Where parameters obtained");
    vAux = getOrderByParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux == null || strAux.equals(""))
          result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Order by parameters obtained");
    result.addElement("#AD_LANGUAGE");
    return result;
  }

  /**
   * Setter for the table alias index.
   * 
   * @param _index
   *          Integer with the new index.
   */
  private void setIndex(int _index) {
    this.index = _index;
  }

  /**
   * Main method to build the query.
   * 
   * @throws Exception
   */
  private void generateSQL() throws Exception {
    if (getPool() == null)
      throw new Exception("No pool defined for database connection");
    else if (getReferenceType().equals(""))
      throw new Exception("No reference type defined");

    identifier("", null);
  }

  /**
   * Method to fix the names of the fields. Searchs all the fields in the where clause and order by
   * clause to change the names with correct aliases. This intends to fix the problem of the names
   * in the whereclauses, filterclauses and orderbyclauses fields of the tab's table, where the user
   * doesn´t know the alias of the referenced field.
   */
  private void parseNames() {
    Vector<QueryFieldStructure> tables = getFromFields();
    if (tables == null || tables.size() == 0)
      return;
    if (where != null && where.size() > 0) {
      for (int i = 0; i < where.size(); i++) {
        QueryFieldStructure auxStructure = where.elementAt(i);
        if (auxStructure.getType().equalsIgnoreCase("FILTER")) {
          String strAux = auxStructure.getField();
          for (int j = 0; j < tables.size(); j++) {
            QueryFieldStructure auxTable = tables.elementAt(j);
            String strTable = auxTable.getField();
            int p = strTable.indexOf(" ");
            if (p != -1)
              strTable = strTable.substring(0, p).trim();
            strAux = replaceIgnoreCase(strAux, strTable + ".", auxTable.getAlias() + ".");
          }
          if (!strAux.equalsIgnoreCase(auxStructure.getField())) {
            auxStructure.setField(strAux);
            if (log4j.isDebugEnabled())
              log4j.debug("Field replaced: " + strAux);
            where.set(i, auxStructure);
          }
        }
      }
    }
    if (orderBy != null && orderBy.size() > 0) {
      for (int i = 0; i < orderBy.size(); i++) {
        QueryFieldStructure auxStructure = orderBy.elementAt(i);
        String strAux = auxStructure.getField();
        for (int j = 0; j < tables.size(); j++) {
          QueryFieldStructure auxTable = tables.elementAt(j);
          String strTable = auxTable.getField();
          int p = strTable.indexOf(" ");
          if (p != -1)
            strTable = strTable.substring(0, p).trim();
          strAux = replaceIgnoreCase(strAux, strTable + ".", auxTable.getAlias() + ".");
        }
        if (!strAux.equalsIgnoreCase(auxStructure.getField())) {
          auxStructure.setField(strAux);
          if (log4j.isDebugEnabled())
            log4j.debug("Field replaced: " + strAux);
          orderBy.set(i, auxStructure);
        }
      }
    }
  }

  /**
   * Auxiliar method to make a replace ignoring the case.
   * 
   * @param data
   *          String with the text.
   * @param replaceWhat
   *          The string to search.
   * @param replaceWith
   *          The new string to replace with.
   * @return String with the text replaced.
   */
  private String replaceIgnoreCase(String data, String replaceWhat, String replaceWith) {
    String localData = data;
    if (localData == null || localData.equals(""))
      return "";
    if (log4j.isDebugEnabled())
      log4j.debug("parsing data: " + localData + " - replace: " + replaceWhat + " - with: "
          + replaceWith);
    StringBuffer text = new StringBuffer();
    int i = localData.toUpperCase().indexOf(replaceWhat.toUpperCase());
    while (i != -1) {
      text.append(localData.substring(0, i)).append(replaceWith);
      localData = localData.substring(i + replaceWhat.length());
      i = localData.toUpperCase().indexOf(replaceWhat.toUpperCase());
    }
    text.append(localData);
    return text.toString();
  }

  /**
   * Parse the validation string searching the @ elements and replacing them with the correct
   * values, adding the needed parameters.
   * 
   * @throws Exception
   */
  public void parseValidation() throws Exception {
    if (getValidation() == null || getValidation().equals(""))
      return;
    if (log4j.isDebugEnabled())
      log4j.debug("Validation id: " + getValidation());
    String val = ComboTableQueryData.getValidation(getPool(), getValidation());
    if (log4j.isDebugEnabled())
      log4j.debug("Validation text: " + val);
    if (val.indexOf("@") != -1)
      val = parseContext(val, "WHERE");
    if (!val.equals(""))
      addWhereField("(" + val + ")", "FILTER");
    if (log4j.isDebugEnabled())
      log4j.debug("Validation parsed: " + val);
  }

  /**
   * Auxiliar method to replace the variable sections of the clauses.
   * 
   * @param context
   *          String with the variable.
   * @param type
   *          String with the type of the clause (WHERE, ORDER...)
   * @return String with the text replaced.
   */
  public String parseContext(String context, String type) {
    if (context == null || context.equals(""))
      return "";
    StringBuffer strOut = new StringBuffer();
    String value = new String(context);
    String token, defStr;
    int i = value.indexOf("@");
    while (i != -1) {
      strOut.append(value.substring(0, i));
      value = value.substring(i + 1);
      int j = value.indexOf("@");
      if (j == -1) {
        strOut.append(value);
        return strOut.toString();
      }
      token = value.substring(0, j);
      if (token.equalsIgnoreCase("#User_Client"))
        defStr = getClientList();
      else if (token.equalsIgnoreCase("#User_Org"))
        defStr = getOrgList();
      else
        defStr = "?";

      if (defStr.equals("?")) {
        if (type.equalsIgnoreCase("WHERE"))
          addWhereParameter(token, "FILTER", "FILTER");
        else if (type.equalsIgnoreCase("ORDERBY"))
          addOrderByParameter(token, "FILTER");
      }
      strOut.append(defStr);
      value = value.substring(j + 1);
      i = value.indexOf("@");
    }
    strOut.append(value);
    return strOut.toString().replace("'?'", "?");
  }

  /**
   * Support method for the generateSQL method, to build the query.
   * 
   * @param tableName
   *          String with the name of the table.
   * @param field
   *          String with the name of the field.
   * @throws Exception
   */
  public void identifier(String tableName, FieldProvider field) throws Exception {
    UIReference uiref;
    if (field == null) {
      if (getObjectReference() != null && getObjectReference().length() > 0) {
        uiref = Reference.getUIReference(getReferenceType(), getObjectReference());
      } else {
        uiref = Reference.getUIReference(getReferenceType(), null);
      }
    } else {
      uiref = Reference.getUIReference(field.getField("reference"),
          field.getField("referenceValue"));
    }
    uiref.setComboTableDataIdentifier(this, tableName, field);
    canBeCached = uiref.canBeCached();
  }

  /**
   * Returns the generated query.
   * 
   * @param onlyId
   *          Boolean to indicate if the select clause must have only the key field.
   * @param discard
   *          Array of field groups to remove from the query.
   * @param recordId
   *          recordId to be filtered.
   * @param startRow
   *          starting index of the records.
   * @param endRow
   *          end index of the records.
   * @param conn
   *          Connection provider
   * @return String with the query.
   */
  private String getQuery(boolean onlyId, String[] discard, String recordId, Integer startRow,
      Integer endRow, ConnectionProvider conn, boolean applyFilter) {
    StringBuffer text = new StringBuffer();
    Vector<QueryFieldStructure> aux = getSelectFields();
    String idName = "", nameToCompare = null;
    boolean hasWhere = false;
    boolean applyLimits = (startRow != null && startRow != -1) && (endRow != null && endRow != -1)
        && StringUtils.isEmpty(recordId);
    String rdbms = conn == null ? "" : conn.getRDBMS();
    if (aux != null) {
      StringBuffer name = new StringBuffer();
      String description = "";
      String id = "";
      text.append("SELECT ");
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!isInArray(discard, auxStructure.getType())) {
          if (auxStructure.getData("alias").equalsIgnoreCase("ID")) {
            if (id.equals("")) {
              id = auxStructure.toString(true);
              idName = auxStructure.toString();
            }
          } else if (auxStructure.getData("alias").equalsIgnoreCase("DESCRIPTION")) {
            if (description.equals(""))
              description = auxStructure.toString(true);
          } else {
            if (name.toString().equals(""))
              name.append("(");
            else
              name.append(FIELD_CONCAT);
            name.append("COALESCE(TO_CHAR(").append(auxStructure.toString()).append("),'')");
          }
        }
      }
      text.append(id);
      if (!name.toString().equals("")) {
        nameToCompare = name.toString() + ")";
        name.append(") AS NAME");
      } else {
        name.append("'>>No Record Identifier<<' AS NAME");
        log4j.error("Foreign table referenced by '" + idName
            + "' does not have 'Record Identifier' defined");
      }
      text.append(", ").append(name.toString());
      if (description != null && !description.equals(""))
        text.append(", ").append(description);
      else
        text.append(", '' AS DESCRIPTION");
      text.append(" \n");
    }

    aux = getFromFields();
    if (aux != null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("FROM ");
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!isInArray(discard, auxStructure.getType())) {
          if (!txtAux.toString().equals(""))
            txtAux.append("left join ");
          txtAux.append(auxStructure.toString()).append(" \n");
        }
      }
      text.append(txtAux.toString());
    }

    aux = getWhereFields();
    if (aux != null) {
      StringBuffer txtAux = new StringBuffer();
      for (int i = 0; i < aux.size(); i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!isInArray(discard, auxStructure.getType())) {
          hasWhere = true;
          if (!txtAux.toString().equals(""))
            txtAux.append("AND ");
          txtAux.append(auxStructure.toString()).append(" \n");
        }
      }
      if (hasWhere) {
        if (recordId != null) {
          txtAux.append(" AND " + idName + "=(?) ");
        }

        text.append("WHERE ").append(txtAux.toString());
      }
      if (applyFilter && !StringUtils.isEmpty(nameToCompare)) {
        // filtering by value
        text.append(" AND UPPER(" + nameToCompare + ") like UPPER(?)\n");
      }
    }

    if (!onlyId) {
      aux = getOrderByFields();
      if (aux != null) {
        StringBuffer txtAux = new StringBuffer();
        text.append("ORDER BY ");
        for (int i = 0; i < aux.size(); i++) {
          QueryFieldStructure auxStructure = aux.elementAt(i);
          if (!isInArray(discard, auxStructure.getType())) {
            if (!txtAux.toString().equals(""))
              txtAux.append(", ");
            txtAux.append(auxStructure.toString());
          }
        }
        text.append(txtAux.toString());
      }
    } else {
      if (!hasWhere)
        text.append("WHERE ");
      else
        text.append("AND ");
      text.append(idName).append(" = ? ");
    }

    if (applyLimits && rdbms.equalsIgnoreCase("POSTGRE")) {
      int numberOfRows = endRow - startRow + 1;
      text.append(" LIMIT " + numberOfRows + " OFFSET " + startRow);
    }
    if (applyLimits && rdbms.equalsIgnoreCase("ORACLE")) {
      // in oracle rows are defined from 1, so incrementing startRow and endRow by 1
      String oraQuery = "select * from ( select a.*, ROWNUM rnum from ( " + text.toString()
          + ") a where rownum <= " + (endRow + 1) + " ) where rnum >= " + (startRow + 1) + "";
      return oraQuery;
    }
    return text.toString();
  }

  /**
   * Auxiliar method to search a value in an array.
   * 
   * @param data
   *          Array with the data.
   * @param element
   *          String to search in the array.
   * @return Boolean to indicate if the element was found in the array.
   */
  private boolean isInArray(String[] data, String element) {
    if (data == null || data.length == 0 || element == null || element.equals(""))
      return false;
    for (int i = 0; i < data.length; i++) {
      if (data[i].equalsIgnoreCase(element))
        return true;
    }
    return false;
  }

  private int setSQLParameters(PreparedStatement st, Map<String, String> lparameters,
      int iParameter, String[] discard) {
    return setSQLParameters(st, lparameters, iParameter, discard, null);
  }

  private int setSQLParameters(PreparedStatement st, Map<String, String> lparameters,
      int iParameter, String[] discard, String recordId) {
    return setSQLParameters(st, lparameters, iParameter, discard, recordId, null);
  }

  /**
   * Fills the query parameter's values.
   * 
   * @param st
   *          PreparedStatement object.
   * @param iParameter
   *          Index of the parameter.
   * @param discard
   *          Array with the groups to discard.
   * @return Integer with the next parameter's index.
   */
  private int setSQLParameters(PreparedStatement st, Map<String, String> lparameters,
      int iParameter, String[] discard, String recordId, String filter) {
    int localIParameter = iParameter;
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        if (!isInArray(discard, aux.getType())) {
          String strAux = lparameters != null ? (aux.getName() == null ? null : lparameters.get(aux
              .getName().toUpperCase())) : getParameter(aux.getName());
          if (log4j.isDebugEnabled())
            log4j.debug("Parameter - " + localIParameter + " - " + aux.getName() + ": " + strAux);
          UtilSql.setValue(st, ++localIParameter, 12, null, strAux);
        }
      }
    }
    vAux = getFromParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        if (!isInArray(discard, aux.getType())) {
          String strAux = lparameters != null ? (aux.getName() == null ? null : lparameters.get(aux
              .getName().toUpperCase())) : getParameter(aux.getName());
          if (log4j.isDebugEnabled())
            log4j.debug("Parameter - " + localIParameter + " - " + aux.getName() + ": " + strAux);
          UtilSql.setValue(st, ++localIParameter, 12, null, strAux);
        }
      }
    }
    vAux = getWhereParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        if (!isInArray(discard, aux.getType())) {
          String strAux = lparameters != null ? (aux.getName() == null ? null : lparameters.get(aux
              .getName().toUpperCase())) : getParameter(aux.getName());
          if (log4j.isDebugEnabled())
            log4j.debug("Parameter - " + localIParameter + " - " + aux.getName() + ": " + strAux);
          UtilSql.setValue(st, ++localIParameter, 12, null, strAux);
        }
      }
    }
    if (recordId != null) {
      UtilSql.setValue(st, ++localIParameter, 12, null, recordId);
    }
    if (!StringUtils.isEmpty(filter)) {
      // filtering by value
      UtilSql.setValue(st, ++localIParameter, 12, null, "%" + filter + "%");
    }
    vAux = getOrderByParameters();
    if (vAux != null) {
      for (int i = 0; i < vAux.size(); i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        if (!isInArray(discard, aux.getType())) {
          String strAux = lparameters != null ? (aux.getName() == null ? null : lparameters.get(aux
              .getName().toUpperCase())) : getParameter(aux.getName());
          if (log4j.isDebugEnabled())
            log4j.debug("Parameter - " + localIParameter + " - " + aux.getName() + ": " + strAux);
          UtilSql.setValue(st, ++localIParameter, 12, null, strAux);
        }
      }
    }
    return localIParameter;
  }

  /**
   * Executes the query in the database and returns the data.
   * 
   * @param includeActual
   *          Boolean that indicates if the actual selected value must be included in the result,
   *          even if it doesn´t exists in the new query.
   * @return Array of FieldProvider with the data.
   * @throws Exception
   */
  public FieldProvider[] select(boolean includeActual) throws Exception {
    return select(getPool(), null, includeActual);
  }

  public FieldProvider[] select(ConnectionProvider conn, Map<String, String> lparameters,
      boolean includeActual) throws Exception {
    return select(conn, lparameters, includeActual, null, null);
  }

  public FieldProvider[] select(ConnectionProvider conn, Map<String, String> lparameters,
      boolean includeActual, Integer startRow, Integer endRow) throws Exception {
    String actual = lparameters != null ? lparameters.get("@ACTUAL_VALUE@")
        : getParameter("@ACTUAL_VALUE@");
    String filterValue = lparameters != null ? lparameters.get("FILTER_VALUE")
        : getParameter("FILTER_VALUE");
    if (lparameters != null && lparameters.containsKey("@ONLY_ONE_RECORD@")
        && !lparameters.get("@ONLY_ONE_RECORD@").isEmpty()) {
      String strSqlSingleRecord = getQuery(false, null, lparameters.get("@ONLY_ONE_RECORD@"), null,
          null, null, false);
      log4j.debug("Query for single record: " + strSqlSingleRecord);
      PreparedStatement stSingleRecord = conn.getPreparedStatement(strSqlSingleRecord);
      try {
        ResultSet result;
        int iParameter = 0;
        iParameter = setSQLParameters(stSingleRecord, lparameters, iParameter, null,
            lparameters.get("@ONLY_ONE_RECORD@"));
        result = stSingleRecord.executeQuery();
        if (result.next()) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", UtilSql.getValue(result, "ID"));
          sqlReturnObject.setData("NAME", UtilSql.getValue(result, "NAME"));
          sqlReturnObject.setData("DESCRIPTION", UtilSql.getValue(result, "DESCRIPTION"));
          Vector<Object> vector = new Vector<Object>(0);
          vector.add(sqlReturnObject);
          FieldProvider objectListData[] = new FieldProvider[vector.size()];
          vector.copyInto(objectListData);
          return (objectListData);
        }

        if (includeActual && actual != null && !actual.equals("")) {

          String[] discard = { "filter", "orderBy", "CLIENT_LIST", "ORG_LIST" };
          String strSqlDisc = getQuery(true, discard, null, null, null, null, false);
          PreparedStatement stInactive = conn.getPreparedStatement(strSqlDisc);
          iParameter = setSQLParameters(stInactive, lparameters, 0, discard);
          UtilSql.setValue(stInactive, ++iParameter, 12, null, actual);
          ResultSet resultIn = stInactive.executeQuery();
          while (resultIn.next()) {
            SQLReturnObject sqlReturnObject = new SQLReturnObject();
            sqlReturnObject.setData("ID", UtilSql.getValue(resultIn, "ID"));
            String strName = UtilSql.getValue(resultIn, "NAME");
            if (!strName.startsWith(INACTIVE_DATA))
              strName = INACTIVE_DATA + strName;
            sqlReturnObject.setData("NAME", strName);
            Vector<Object> vector = new Vector<Object>(0);
            vector.add(sqlReturnObject);
            FieldProvider objectListData[] = new FieldProvider[vector.size()];
            vector.copyInto(objectListData);
            return (objectListData);
          }

        }
      } finally {
        conn.releasePreparedStatement(stSingleRecord);
      }

    }
    String strSql = getQuery(false, null, null, startRow, endRow, conn,
        !StringUtils.isEmpty(filterValue));
    if (log4j.isDebugEnabled())
      log4j.debug("SQL: " + strSql);
    PreparedStatement st = conn.getPreparedStatement(strSql);
    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);

    try {
      int iParameter = 0;
      iParameter = setSQLParameters(st, lparameters, iParameter, null, null, filterValue);
      boolean idFound = false;
      result = st.executeQuery();
      while (result.next()) {
        SQLReturnObject sqlReturnObject = new SQLReturnObject();
        sqlReturnObject.setData("ID", UtilSql.getValue(result, "ID"));
        sqlReturnObject.setData("NAME", UtilSql.getValue(result, "NAME"));
        sqlReturnObject.setData("DESCRIPTION", UtilSql.getValue(result, "DESCRIPTION"));
        if (includeActual && actual != null && !actual.equals("")) {
          if (actual.equals(sqlReturnObject.getData("ID"))) {
            if (!idFound) {
              vector.addElement(sqlReturnObject);
              idFound = true;
            }
          } else {
            vector.addElement(sqlReturnObject);
          }
        } else
          vector.addElement(sqlReturnObject);
        if (lparameters != null && lparameters.containsKey("#ONLY_ONE_RECORD#")) {
          FieldProvider objectListData[] = new FieldProvider[vector.size()];
          vector.copyInto(objectListData);
          return (objectListData);
        }
      }
      result.close();

      if (includeActual && actual != null && !actual.equals("") && !idFound) {
        boolean allDataInSinglePage;
        if (startRow != null && endRow != null) {
          allDataInSinglePage = startRow == 0 && vector.size() < endRow - startRow;
        } else {
          // This method is invoked with startRow = endRow = null for lists. Lists always have load
          // all data in a single page
          allDataInSinglePage = true;
        }
        if (!allDataInSinglePage) {
          // retrieved a partial set of data, checking if current id is in a page different that the
          // served applying the same criteria, if so, do not add it again to the list (it will
          // appear in its own page)
          conn.releasePreparedStatement(st);
          strSql = getQuery(true, null, null, 0, 1, conn, !StringUtils.isEmpty(filterValue));
          log4j.debug("SQL to check if actual ID is in another page: " + strSql);
          st = conn.getPreparedStatement(strSql);
          setSQLParameters(st, lparameters, 0, null, actual, filterValue);
          result = st.executeQuery();
          idFound = result.next();
          result.close();
        }
        if (!idFound) {
          conn.releasePreparedStatement(st);
          String[] discard = { "filter", "orderBy", "CLIENT_LIST", "ORG_LIST" };
          strSql = getQuery(true, discard, null, null, null, null, false);
          if (log4j.isDebugEnabled())
            log4j.debug("SQL Actual ID: " + strSql);
          st = conn.getPreparedStatement(strSql);
          iParameter = setSQLParameters(st, lparameters, 0, discard);
          UtilSql.setValue(st, ++iParameter, 12, null, actual);
          result = st.executeQuery();
          if (result.next()) {
            SQLReturnObject sqlReturnObject = new SQLReturnObject();
            sqlReturnObject.setData("ID", UtilSql.getValue(result, "ID"));
            String strName = UtilSql.getValue(result, "NAME");
            if (!strName.startsWith(INACTIVE_DATA))
              strName = INACTIVE_DATA + strName;
            sqlReturnObject.setData("NAME", strName);
            vector.addElement(sqlReturnObject);
            idFound = true;
          }
        }
        result.close();
        if (!idFound) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", actual);
          sqlReturnObject.setData(
              "NAME",
              INACTIVE_DATA
                  + Utility.messageBD(conn, "NotFound",
                      lparameters != null ? lparameters.get("#AD_LANGUAGE")
                          : getParameter("#AD_LANGUAGE")));

          vector.addElement(sqlReturnObject);
        }
      }
    } catch (SQLException e) {
      log4j.error("Error of SQL in query: " + strSql + "Exception:" + e);
      throw new Exception("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } finally {
      conn.releasePreparedStatement(st);
    }
    FieldProvider objectListData[] = new FieldProvider[vector.size()];
    vector.copyInto(objectListData);
    return (objectListData);
  }

  /**
   * Special fill parameters function to be used from the search popup (servlet).
   * 
   * It flags the combo to be used from a search popup changing the logic to get the needed
   * parameters for a possible where clause: it uses the pattern inpParam<columnName> to get the
   * values from the request and does not use the preferences for fields to preset a search filter
   */
  public void fillParametersFromSearch(String tab, String window) throws ServletException {
    fillSQLParameters(getPool(), vars, null, tab, window, "", true);
  }

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values. Used in the
   * combo fields.
   * 
   * @param data
   *          optional FieldProvider which can be used to get the needed parameter values from. If
   *          the FieldProvider has a filed named after a parameter, then its value will be used if
   *          the value could not be already obtained from the request parameters.
   * @param window
   *          Window id.
   * @param actual_value
   *          actual value for the combo.
   * @throws ServletException
   */
  public void fillParameters(FieldProvider data, String window, String actual_value)
      throws ServletException {
    fillSQLParameters(getPool(), vars, data, "", window, actual_value, false);
  }

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values. Used in the
   * combo fields.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param variables
   *          Handler for the session info.
   * @param data
   *          FieldProvider with the columns values.
   * @param window
   *          Window id.
   * @param actual_value
   *          actual value for the combo.
   * @throws ServletException
   */
  void fillSQLParameters(ConnectionProvider conn, VariablesSecureApp variables, FieldProvider data,
      String tab, String window, String actual_value, boolean fromSearch) throws ServletException {
    final Vector<String> vAux = getParameters();
    if (vAux != null && vAux.size() > 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("Combo Parameters: " + vAux.size());
      for (int i = 0; i < vAux.size(); i++) {
        final String strAux = vAux.elementAt(i);
        try {
          final String value = Utility.parseParameterValue(conn, variables, data, strAux, tab,
              window, actual_value, fromSearch);
          if (log4j.isDebugEnabled())
            log4j.debug("Combo Parameter: " + strAux + " - Value: " + value);
          setParameter(strAux, value);
        } catch (final Exception ex) {
          throw new ServletException(ex);
        }
      }
    }
  }

  public Map<String, String> fillSQLParametersIntoMap(ConnectionProvider conn,
      VariablesSecureApp variables, FieldProvider data, String window, String actual_value)
      throws ServletException {
    final Vector<String> vAux = getParameters();
    Hashtable<String, String> lparameters = new Hashtable<String, String>();
    // We first add all current parameters in the combo
    for (String key : parameters.keySet()) {
      lparameters.put(key, parameters.get(key));
    }
    if (vAux != null && vAux.size() > 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("Combo Parameters: " + vAux.size());
      for (int i = 0; i < vAux.size(); i++) {
        final String strAux = vAux.elementAt(i);
        try {
          final String value = Utility.parseParameterValue(conn, variables, data, strAux, "",
              window, actual_value, false);
          if (log4j.isDebugEnabled())
            log4j.debug("Combo Parameter: " + strAux + " - Value: " + value);
          if (value == null || value.equals("") || "null".equals(value))
            lparameters.remove(strAux.toUpperCase());
          else
            lparameters.put(strAux.toUpperCase(), value);
        } catch (final Exception ex) {
          throw new ServletException(ex);
        }
      }
    }
    return lparameters;

  }

  public boolean canBeCached() {
    return canBeCached;
  }
}
