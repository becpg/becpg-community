/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.search.impl.querymodel.impl.db.functions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.adaptor.AnalysisMode;
import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.AspectSupport;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.ParentSupport;
import org.alfresco.repo.search.impl.querymodel.impl.db.PropertySupport;
import org.alfresco.repo.search.impl.querymodel.impl.db.TypeSupport;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 */
public class DBFTSTerm extends FTSTerm implements DBQueryBuilderComponent {
	DBQueryBuilderComponent builderSupport;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
	 */
	@Override
	public boolean isSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace .NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService,
	 * org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map, org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
	 */
	@Override
	public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO,
			TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext,
			boolean supportBooleanFloatAndDouble) {
		
		Argument argument = functionArgs.get(ARG_TERM);
		String term = (String) argument.getValue(functionContext);
		PropertyArgument propArg = (PropertyArgument) functionArgs.get(ARG_PROPERTY);

		if ((propArg == null) || (propArg.getPropertyName() == null)) {
			throw new QueryModelException("Default field not supported");
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_PARENT)) {
			ParentSupport parentSupport = new ParentSupport();
			String id = term;
			parentSupport.setDbid(DBQuery.getDbid(id, nodeDAO, tenantService));
			parentSupport.setCommandType(DBQueryBuilderPredicatePartCommandType.EQUALS);
			builderSupport = parentSupport;
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_TYPE)) {
			TypeSupport typeSupport = new TypeSupport();
			typeSupport.setQnameIds(DBQuery.findTypeIds(term, namespaceService, dictionaryService, qnameDAO, false));
			typeSupport.setCommandType(DBQueryBuilderPredicatePartCommandType.IN);
			builderSupport = typeSupport;
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_ASPECT)) {
			AspectSupport aspectSupport = new AspectSupport();
			aspectSupport.setQnameIds(DBQuery.findAspectIds(term, namespaceService, dictionaryService, qnameDAO, false));
			builderSupport = aspectSupport;
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_EXACTTYPE)) {
			TypeSupport typeSupport = new TypeSupport();
			typeSupport.setQnameIds(DBQuery.findTypeIds(term, namespaceService, dictionaryService, qnameDAO, true));
			typeSupport.setCommandType(DBQueryBuilderPredicatePartCommandType.IN);
			builderSupport = typeSupport;
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_EXACTASPECT)) {
			AspectSupport aspectSupport = new AspectSupport();
			aspectSupport.setQnameIds(DBQuery.findAspectIds(term, namespaceService, dictionaryService, qnameDAO, true));
			builderSupport = aspectSupport;
		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_ISNOTNULL)
				|| propArg.getPropertyName().equals(QueryConstants.FIELD_ISNULL)) {

			QName propertyQName = QName
					.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(term), namespaceService));
			PropertySupport propertySupport = new PropertySupport();

			propertySupport.setPropertyQName(propertyQName);
			propertySupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, propertyQName));
			propertySupport.setPair(qnameDAO.getQName(propertyQName));
			propertySupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
			propertySupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
			if (propArg.getPropertyName().equals(QueryConstants.FIELD_ISNULL)) {
				propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.NOTEXISTS);
				propertySupport.setLeftOuter(true);
			} else {
				propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.EXISTS);
			}

			builderSupport = propertySupport;

		} else if (propArg.getPropertyName().equals(QueryConstants.FIELD_ISUNSET)) {
			throw new QueryModelException("Unset not supported");
		}

		else {
			argument = functionArgs.get(ARG_TOKENISATION_MODE);
			AnalysisMode mode = (AnalysisMode) argument.getValue(functionContext);

			QName propertyQName = QName
					.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(propArg.getPropertyName()), namespaceService));

			PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
			if (propDef == null) {
				throw new QueryModelException("No prop def for " + propertyQName);
			}

			DataTypeDefinition dataType = propDef.getDataType();

			//beCPG Assume notIndexed field is LIKE and non text is EQUALS
			boolean isLike = !propDef.isIndexed()
					&& (dataType.getName().equals(DataTypeDefinition.TEXT) || dataType.getName().equals(DataTypeDefinition.MLTEXT));
			if (!isLike && (dataType.getName().equals(DataTypeDefinition.TEXT) || dataType.getName().equals(DataTypeDefinition.MLTEXT))
					&& mode != AnalysisMode.IDENTIFIER) {
				throw new QueryModelException("beCPG: Analysis mode not supported for DB " + propArg.getPropertyName());
			}

			PropertySupport propertySupport = new PropertySupport();
			propertySupport.setValue(term);

			propertySupport.setPropertyQName(propertyQName);
			propertySupport.setPropertyDataType(dataType);
			propertySupport.setPair(qnameDAO.getQName(propertyQName));
			propertySupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
			propertySupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));

			if (isLike) {
				propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.LIKE);
			} else {
				propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.EQUALS);
			}

			builderSupport = propertySupport;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List)
	 */
	@Override
	public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins) {
		builderSupport.buildJoins(singleJoins, multiJoins);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
	 */
	@Override
	public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands) {
		builderSupport.buildPredicateCommands(predicatePartCommands);
	}

}
