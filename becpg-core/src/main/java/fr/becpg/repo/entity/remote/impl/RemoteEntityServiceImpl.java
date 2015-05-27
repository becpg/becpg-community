/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.extractor.ExcelXmlEntityVisitor;
import fr.becpg.repo.entity.remote.extractor.ImportEntityXmlVisitor;
import fr.becpg.repo.entity.remote.extractor.XmlEntityVisitor;

/**
 * 
 * @author matthieu
 * 
 */
@Service("remoteEntityService")
public class RemoteEntityServiceImpl implements RemoteEntityService {

	@Autowired
	private ServiceRegistry serviceRegistry;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	private static Log logger = LogFactory.getLog(RemoteEntityServiceImpl.class);

	@Override
	public void getEntity(NodeRef entityNodeRef, OutputStream out, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml) || format.equals(RemoteEntityFormat.xml_all)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService, contentService);
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
			throw new BeCPGException("Unknow format " + format.toString());
		}
	}

	@Override
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format,
			EntityProviderCallBack entityProviderCallBack) throws BeCPGException {
		return createOrUpdateEntity(entityNodeRef, null, null, in, format, entityProviderCallBack);
	}

	@Override
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties, InputStream in,
			RemoteEntityFormat format, EntityProviderCallBack entityProviderCallBack) throws BeCPGException {

		if (format.equals(RemoteEntityFormat.xml)) {
			ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(serviceRegistry, entityDictionaryService);
			xmlEntityVisitor.setEntityProviderCallBack(entityProviderCallBack);
			NodeRef ret = null;
			try {
				ret = xmlEntityVisitor.visit(entityNodeRef,destNodeRef, properties , in);

			} catch (IOException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (SAXException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (ParserConfigurationException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} finally {
				if (ret == null) {
					logger.error("Cannot create or update entity :" + entityNodeRef + " at format " + format);
				}
			}
			return ret;
		}
		throw new BeCPGException("Unknow format " + format.toString());
	}

	@Override
	public void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService, contentService);
			try {
				xmlEntityVisitor.visit(entities, result);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot list entities at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknow format " + format.toString());
		}

	}

	@Override
	public void getEntityData(NodeRef entityNodeRef, OutputStream result, RemoteEntityFormat format) throws BeCPGException {
		if (RemoteEntityFormat.xml.equals(format)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService, contentService);
			try {
				xmlEntityVisitor.visitData(entityNodeRef, result);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot get entity data at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknow format " + format.toString());
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
			} catch (IOException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (SAXException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (ParserConfigurationException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknow format " + format.toString());
		}

	}

}
