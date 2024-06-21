/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.xml.sax.SAXException;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteSchemaGenerator;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.entity.remote.extractor.ExcelXmlEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.ImportEntityJsonVisitor;
import fr.becpg.repo.entity.remote.extractor.ImportEntityXmlVisitor;
import fr.becpg.repo.entity.remote.extractor.JsonEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.JsonSchemaEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.RemoteEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.XmlEntityVisitor;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>RemoteEntityServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("remoteEntityService")
public class RemoteEntityServiceImpl implements RemoteEntityService {

	private static final Log logger = LogFactory.getLog(RemoteEntityServiceImpl.class);

	private static final String UNKNOW_FORMAT_ERROR = "Unknown format %s";
	private static final String CREATE_ERROR = "Cannot create or update entity: %s at format %s - %s";

	@Autowired
	RemoteServiceRegisty remoteServiceRegisty;
	

	@Autowired
	private RemoteSchemaGenerator remoteSchemaGenerator;
	

	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;
	

	@Autowired
	@Qualifier("ContentService")
	private ContentService contentService;

	
	@Override
	public RemoteServiceRegisty serviceRegistry() {
		return remoteServiceRegisty;
	}

	/** {@inheritDoc} */
	@Override
	public void getEntity(NodeRef entityNodeRef, OutputStream out, RemoteParams params) {
		RemoteEntityFormat format = params.getFormat();
		RemoteEntityVisitor remoteEntityVisitor = null;
		switch (format) {
		case xml, xml_all, xml_light:
			remoteEntityVisitor = new XmlEntityVisitor(remoteServiceRegisty);
			break;
		case xml_excel:
			remoteEntityVisitor = new ExcelXmlEntityVisitor(remoteServiceRegisty);
			break;
		case json, json_all:
			remoteEntityVisitor = new JsonEntityVisitor(remoteServiceRegisty);
			break;
		case json_schema:
			remoteEntityVisitor = new JsonSchemaEntityVisitor(remoteServiceRegisty);
			break;
		case xsd, xsd_excel:

			remoteSchemaGenerator.generateSchema(out);
			break;
		default:
			throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));

		}

		if (remoteEntityVisitor != null) {
			remoteEntityVisitor.setParams(params);

			try {
				remoteEntityVisitor.visit(entityNodeRef, out);
			} catch (Exception e) {
				throw new BeCPGException("Cannot export entity :" + entityNodeRef + " at format: " + format, e);
			}
		}

	}

	@Override
	public void getEntitySchema(QName type, OutputStream out, RemoteParams params) {
		RemoteEntityFormat format = params.getFormat();
		switch (format) {
		case xsd, xsd_excel:
			remoteSchemaGenerator.generateSchema(out);
			break;
		case json_schema, json:
			try {
				JsonSchemaEntityVisitor jsonSchemaVisitor = new JsonSchemaEntityVisitor(remoteServiceRegisty);
				jsonSchemaVisitor.setParams(params);

				jsonSchemaVisitor.visit(type, out);
			} catch (Exception e) {
				throw new BeCPGException("Cannot export schema for type :" + type + " at format " + format, e);
			}
			break;
		default:
			throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));
		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteParams params, EntityProviderCallBack entityProviderCallBack) {
		RemoteEntityFormat format = params.getFormat();
		if (RemoteEntityFormat.xml.equals(format) || RemoteEntityFormat.json.equals(format)) {

			final Set<NodeRef> rets = new HashSet<>();

			L2CacheSupport.doInCacheContext(() -> {

				Map<NodeRef, NodeRef> cache = new HashMap<>();

				rets.add(internalCreateOrUpdateEntity(entityNodeRef, null, in, params, entityProviderCallBack, cache));

			}, false, false);

			if (rets.isEmpty()) {
				throw new BeCPGException(String.format(CREATE_ERROR, entityNodeRef , format, "No entity created"));
			}

			return rets.iterator().next();

		}

		throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef internalCreateOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, InputStream in, RemoteParams params,
			EntityProviderCallBack entityProviderCallBack, Map<NodeRef, NodeRef> cache) {

		RemoteEntityFormat format = params.getFormat();

		StopWatch watch = null;

		try {

			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			try {
				if (RemoteEntityFormat.json.equals(format)) {
					ImportEntityJsonVisitor jsonEntityVisitor = new ImportEntityJsonVisitor(remoteServiceRegisty);

					return jsonEntityVisitor.visit(entityNodeRef, in);
				} else {
					ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(remoteServiceRegisty);
					xmlEntityVisitor.setEntityProviderCallBack(entityProviderCallBack);

					return xmlEntityVisitor.visit(entityNodeRef, destNodeRef, in);

				}
			} catch (IOException | ParserConfigurationException | SAXException | JSONException e) {
				logger.error("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
				
				throw new BeCPGException(String.format(CREATE_ERROR, entityNodeRef , format, e.getMessage()),e);
				
			}

		} finally {
			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("createOrUpdateEntity run in  " + watch.getTotalTimeSeconds() + " seconds ");

			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void listEntities(PagingResults<NodeRef> entities, OutputStream result, RemoteParams params) throws BeCPGException {

		RemoteEntityFormat format = params.getFormat();
		RemoteEntityVisitor remoteEntityVisitor = null;

		switch (format) {
		case xml:

			remoteEntityVisitor = new XmlEntityVisitor(remoteServiceRegisty);

			break;
		case json:

			remoteEntityVisitor = new JsonEntityVisitor(remoteServiceRegisty);
			break;
		default:
			throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));
		}

		remoteEntityVisitor.setParams(params);
		try {
			remoteEntityVisitor.visit(entities, result);
		} catch (Exception e) {
			throw new BeCPGException("Cannot list entities at format " + format, e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void getEntityData(NodeRef entityNodeRef, OutputStream result, RemoteParams params) {

		RemoteEntityFormat format = params.getFormat();
		RemoteEntityVisitor remoteEntityVisitor = null;

		switch (format) {
		case xml:

			remoteEntityVisitor = new XmlEntityVisitor(remoteServiceRegisty);

			break;
		case json:

			remoteEntityVisitor = new JsonEntityVisitor(remoteServiceRegisty);
			break;
		default:
			throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));

		}

		remoteEntityVisitor.setParams(params);
		try {
			remoteEntityVisitor.visitData(entityNodeRef, result);
		} catch (Exception e) {
			throw new BeCPGException("Cannot get entity data at format " + format, e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream in, RemoteParams params) {
		RemoteEntityFormat format = params.getFormat();
		if (RemoteEntityFormat.xml.equals(format)) {
			ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(remoteServiceRegisty);

			String fileName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

			String mimetype = mimetypeService.guessMimetype(fileName);
			ContentWriter writer = contentService.getWriter(entityNodeRef, ContentModel.PROP_CONTENT, true);
			writer.setMimetype(mimetype);
			try (OutputStream out = writer.getContentOutputStream()) {
				xmlEntityVisitor.visitData(in, out);
			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new BeCPGException(String.format(CREATE_ERROR, entityNodeRef , format, e.getMessage()),e);
			}
		} else {
			throw new BeCPGException(String.format(UNKNOW_FORMAT_ERROR, format.toString()));
		}

	}

}
