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
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.PropertySupport;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSRange;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * <p>DBFTSRange class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DBFTSRange extends FTSRange implements DBQueryBuilderComponent {

	PropertySupport greaterThanSupport = null;
	PropertySupport lessThanSupport = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#
	 * isSupported()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#
	 * prepare(org.alfresco.service.namespace.NamespaceService,
	 * org.alfresco.service.cmr.dictionary.DictionaryService,
	 * org.alfresco.repo.domain.qname.QNameDAO,
	 * org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map,
	 * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
	 */
	/** {@inheritDoc} */
	@Override
	public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO,
			TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext,
			boolean supportBooleanFloatAndDouble) {

		// Greater

		PropertyArgument propertyArgument = (PropertyArgument) functionArgs.get(ARG_PROPERTY);

		if ((propertyArgument == null) || (propertyArgument.getPropertyName() == null)) {
			throw new QueryModelException("Default field not supported");
		}

		String from = (String) functionArgs.get(ARG_FROM).getValue(functionContext);
		String to = (String) functionArgs.get(ARG_TO).getValue(functionContext);

        QName propertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(propertyArgument.getPropertyName()), namespaceService));

		if (!"MIN".equalsIgnoreCase(from)  && !"MAX".equalsIgnoreCase(from)) {
			greaterThanSupport = new PropertySupport();
			

			greaterThanSupport.setValue(from);
			greaterThanSupport.setPropertyQName(propertyQName);
			greaterThanSupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, propertyQName));
			greaterThanSupport.setPair(qnameDAO.getQName(propertyQName));
			greaterThanSupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
			greaterThanSupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
			greaterThanSupport
					.setCommandType((Boolean) functionArgs.get(ARG_FROM_INC).getValue(functionContext) ? DBQueryBuilderPredicatePartCommandType.GTE
							: DBQueryBuilderPredicatePartCommandType.GT);
		}

		if (!"MAX".equalsIgnoreCase(to) && !"MIN".equalsIgnoreCase(to)) {

			lessThanSupport = new PropertySupport();

			lessThanSupport.setValue(to);

			lessThanSupport.setPropertyQName(propertyQName);
			lessThanSupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, propertyQName));
			lessThanSupport.setPair(qnameDAO.getQName(propertyQName));
			lessThanSupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
			lessThanSupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
			lessThanSupport
					.setCommandType((Boolean) functionArgs.get(ARG_TO_INC).getValue(functionContext) ? DBQueryBuilderPredicatePartCommandType.LTE
							: DBQueryBuilderPredicatePartCommandType.LT);
		}
		
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#
	 * buildJoins(java.util.Map, java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins) {
		if (greaterThanSupport != null) {
			greaterThanSupport.buildJoins(singleJoins, multiJoins);
		}
		if (lessThanSupport != null) {
			lessThanSupport.buildJoins(singleJoins, multiJoins);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#
	 * buildPredicateCommands(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands) {

		if ((greaterThanSupport != null) && (lessThanSupport != null)) {

			DBQueryBuilderPredicatePartCommand open = new DBQueryBuilderPredicatePartCommand();
			open.setType(DBQueryBuilderPredicatePartCommandType.OPEN);
			predicatePartCommands.add(open);

			greaterThanSupport.buildPredicateCommands(predicatePartCommands);

			DBQueryBuilderPredicatePartCommand and = new DBQueryBuilderPredicatePartCommand();
			and.setType(DBQueryBuilderPredicatePartCommandType.AND);
			predicatePartCommands.add(and);

			lessThanSupport.buildPredicateCommands(predicatePartCommands);

			DBQueryBuilderPredicatePartCommand close = new DBQueryBuilderPredicatePartCommand();
			close.setType(DBQueryBuilderPredicatePartCommandType.CLOSE);
			predicatePartCommands.add(close);
		} else {

			if (greaterThanSupport != null) {
				greaterThanSupport.buildPredicateCommands(predicatePartCommands);
			} else if (lessThanSupport != null) {
				lessThanSupport.buildPredicateCommands(predicatePartCommands);
			} else {
				DBQueryBuilderPredicatePartCommand and = new DBQueryBuilderPredicatePartCommand();
				and.setType(DBQueryBuilderPredicatePartCommandType.NP_MATCHES);
				predicatePartCommands.add(and);
			}
		}
	}

}
