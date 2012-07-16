package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.extractor.ImportEntityXmlVisitor;
import fr.becpg.repo.entity.remote.extractor.XmlEntityVisitor;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author matthieu
 * 
 */
public class RemoteEntityServiceImpl implements RemoteEntityService {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	private BeCPGSearchService beCPGSearchService;

	private EntityService entityService;
	
	private static Log logger = LogFactory.getLog(RemoteEntityServiceImpl.class);

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	@Override
	public void getEntity(NodeRef entityNodeRef, OutputStream out, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService);
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
	public NodeRef createOrUpdateEntity(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format, EntityProviderCallBack entityProviderCallBack) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(nodeService, namespaceService, beCPGSearchService);
			xmlEntityVisitor.setEntityProviderCallBack(entityProviderCallBack);
			try {
				return xmlEntityVisitor.visit(entityNodeRef, in);
			} catch (IOException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (SAXException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (ParserConfigurationException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			}
		}
		throw new BeCPGException("Unknow format " + format.toString());
	}

	@Override
	public void listEntities(List<NodeRef> entities, OutputStream result, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService);
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
		if (format.equals(RemoteEntityFormat.xml)) {
			XmlEntityVisitor xmlEntityVisitor = new XmlEntityVisitor(nodeService, namespaceService, dictionaryService);
			try {
				xmlEntityVisitor.visit(entityNodeRef, extractData(entityNodeRef), result);
			} catch (XMLStreamException e) {
				throw new BeCPGException("Cannot get entity data at format " + format, e);
			}
		} else {
			throw new BeCPGException("Unknow format " + format.toString());
		}

	}

	@Override
	public void addOrUpdateEntityData(NodeRef entityNodeRef, InputStream in, RemoteEntityFormat format) throws BeCPGException {
		if (format.equals(RemoteEntityFormat.xml)) {
			ImportEntityXmlVisitor xmlEntityVisitor = new ImportEntityXmlVisitor(nodeService, namespaceService, beCPGSearchService);
			try {
				Map<String, byte[]> images = xmlEntityVisitor.visitData(in);
				entityService.writeImages(entityNodeRef, images);
			} catch (IOException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (SAXException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (ParserConfigurationException e) {
				throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
			} catch (BeCPGException e) {
				logger.warn(e.getMessage());
			}
		} else {
			throw new BeCPGException("Unknow format " + format.toString());
		}

	}

	private Map<String, byte[]> extractData(NodeRef entityNodeRef) {
		Map<String, byte[]> images = new HashMap<String, byte[]>();
		try {
			for (NodeRef imageNodeRef : entityService.getImages(entityNodeRef)) {
	
				images.put((String) nodeService.getProperty(imageNodeRef, ContentModel.PROP_NAME), entityService.getImage(imageNodeRef));
			}
		} catch (BeCPGException e) {
			logger.warn(e.getMessage());
		}

		return images;
	}

	@Override
	public boolean containsData(NodeRef entityNodeRef)  {
		return entityService.hasImageFolder(entityNodeRef);
	}

}
