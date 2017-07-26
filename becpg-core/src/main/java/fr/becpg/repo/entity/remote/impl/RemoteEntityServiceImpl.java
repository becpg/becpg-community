/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.xml.sax.SAXException;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.extractor.ExcelXmlEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.ImportEntityXmlVisitor;
import fr.becpg.repo.entity.remote.extractor.XmlEntityVisitor;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 *
 * @author matthieu
 *
 */
@Service("remoteEntityService")
public class RemoteEntityServiceImpl implements RemoteEntityService {

	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	@Autowired
	private SiteService siteService;

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;
	
	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	private static final Log logger = LogFactory.getLog(RemoteEntityServiceImpl.class);

	@Override
	public void getEntity(NodeRef entityNodeRef, OutputStream out, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml) || format.equals(RemoteEntityFormat.xml_all)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(mlNodeService, nodeService, namespaceService, dictionaryService, contentService, siteService);
			if (format.equals(RemoteEntityFormat.xml_all)) {
				xmlEntityVisitor.setDumpAll(true);
			}
			try {
				xmlEntityVisitor.visit(entityNodeRef, out);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot export entity :" + entityNodeRef + " at format " + format, e);
			}
		} else if (format.equals(RemoteEntityFormat.xml_excel)) {
			ExcelXmlEntityVisitor xmlEntityVisitor = new ExcelXmlEntityVisitor(nodeService, namespaceService, dictionaryService, contentService);
			try {
				xmlEntityVisitor.visit(entityNodeRef, out);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot export entity :" + entityNodeRef + " at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknown format " + format.toString());
		}
	}

	@Override
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format,
			EntityProviderCallBack entityProviderCallBack) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {

			final Set<NodeRef> rets = new HashSet<>();
			L2CacheSupport.doInCacheContext(() -> {
				
				Map<NodeRef, NodeRef> cache = new HashMap<>();
				
				rets.add(createOrUpdateEntity(entityNodeRef, null, null, in, format, entityProviderCallBack, cache));

			}, false, true);

			if (rets.isEmpty()) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format);
			}

			return rets.iterator().next();

		}

		throw new BeCPGException("Unknown format " + format.toString());
	}

	@Override
	@Deprecated
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties, InputStream in,
			RemoteEntityFormat format, EntityProviderCallBack entityProviderCallBack, Map<NodeRef, NodeRef> cache) {

		StopWatch watch = null;

		try {

			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
			return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				// Only for transaction do not reenable it
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);

				ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(serviceRegistry, entityDictionaryService);
				xmlEntityVisitor.setEntityProviderCallBack(entityProviderCallBack);
				try {
					return xmlEntityVisitor.visit(entityNodeRef, destNodeRef, properties, in);
				} catch (IOException | ParserConfigurationException | SAXException e) {
					logger.error("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
				}
				return null;
			}, false, false);

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("createOrUpdateEntity run in  " + watch.getTotalTimeSeconds() + " seconds ");

			}
		}

	}

	@Override
	public void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(mlNodeService, nodeService, namespaceService, dictionaryService, contentService, siteService);
			try {
				xmlEntityVisitor.visit(entities, result);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot list entities at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknown format " + format.toString());
		}

	}

	@Override
	public void getEntityData(NodeRef entityNodeRef, OutputStream result, RemoteEntityFormat format) throws BeCPGException {
		if (RemoteEntityFormat.xml.equals(format)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(mlNodeService, nodeService, namespaceService, dictionaryService, contentService, siteService);
			try {
				xmlEntityVisitor.visitData(entityNodeRef, result);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot get entity data at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknown format " + format.toString());
		}

	}

	@Override
	public void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format) throws BeCPGException {
		if (RemoteEntityFormat.xml.equals(format)) {
			ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(serviceRegistry, entityDictionaryService);

			String fileName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

			String mimetype = mimetypeService.guessMimetype(fileName);
			ContentWriter writer = contentService.getWriter(entityNodeRef, ContentModel.PROP_CONTENT, true);
			writer.setMimetype(mimetype);
			try (OutputStream out = writer.getContentOutputStream()) {
				xmlEntityVisitor.visitData(in, out);
			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknown format " + format.toString());
		}

	}

}
