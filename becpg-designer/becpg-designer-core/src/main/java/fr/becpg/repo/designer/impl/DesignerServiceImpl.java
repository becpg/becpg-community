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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.designer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.data.DesignerTree;
import fr.becpg.repo.designer.data.FormControl;
import fr.becpg.repo.helper.MLTextHelper;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * <p>
 * DesignerServiceImpl class.
 * </p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class DesignerServiceImpl implements DesignerService {

	private static String BECPG_CONFIG_CUSTOM = "becpg-config-custom.xml";

	private NodeService nodeService;

	private ContentService contentService;

	private DictionaryService dictionaryService;

	private MetaModelVisitor metaModelVisitor;

	private FormModelVisitor formModelVisitor;

	private FormJsonVisitor formJsonVisitor;

	private DesignerTreeVisitor designerTreeVisitor;

	private DesignerInitService designerInitService;

	private MimetypeService mimetypeService;

	private BehaviourFilter policyBehaviourFilter;

	private DictionaryDAO dictionaryDAO;

	// Controls cache
	private List<FormControl> controls = new ArrayList<>();

	private static final Log logger = LogFactory.getLog(DesignerServiceImpl.class);

	/**
	 * <p>
	 * Setter for the field <code>designerInitService</code>.
	 * </p>
	 *
	 * @param designerInitService
	 *            a {@link fr.becpg.repo.designer.DesignerInitService} object.
	 */
	public void setDesignerInitService(DesignerInitService designerInitService) {
		this.designerInitService = designerInitService;
	}

	/**
	 * <p>
	 * Setter for the field <code>mimetypeService</code>.
	 * </p>
	 *
	 * @param mimetypeService
	 *            a {@link org.alfresco.service.cmr.repository.MimetypeService}
	 *            object.
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>policyBehaviourFilter</code>.
	 * </p>
	 *
	 * @param policyBehaviourFilter
	 *            a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>
	 * Setter for the field <code>dictionaryDAO</code>.
	 * </p>
	 *
	 * @param dictionaryDAO
	 *            a {@link org.alfresco.repo.dictionary.DictionaryDAO} object.
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	/**
	 * <p>
	 * Setter for the field <code>dictionaryService</code>.
	 * </p>
	 *
	 * @param dictionaryService
	 *            a
	 *            {@link org.alfresco.service.cmr.dictionary.DictionaryService}
	 *            object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>
	 * Setter for the field <code>contentService</code>.
	 * </p>
	 *
	 * @param contentService
	 *            a {@link org.alfresco.service.cmr.repository.ContentService}
	 *            object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>metaModelVisitor</code>.
	 * </p>
	 *
	 * @param metaModelVisitor
	 *            a {@link fr.becpg.repo.designer.impl.MetaModelVisitor} object.
	 */
	public void setMetaModelVisitor(MetaModelVisitor metaModelVisitor) {
		this.metaModelVisitor = metaModelVisitor;
	}

	/**
	 * <p>
	 * Setter for the field <code>formModelVisitor</code>.
	 * </p>
	 *
	 * @param formModelVisitor
	 *            a {@link fr.becpg.repo.designer.impl.FormModelVisitor} object.
	 */
	public void setFormModelVisitor(FormModelVisitor formModelVisitor) {
		this.formModelVisitor = formModelVisitor;
	}

	/**
	 * <p>
	 * Setter for the field <code>designerTreeVisitor</code>.
	 * </p>
	 *
	 * @param designerTreeVisitor
	 *            a {@link fr.becpg.repo.designer.impl.DesignerTreeVisitor}
	 *            object.
	 */
	public void setDesignerTreeVisitor(DesignerTreeVisitor designerTreeVisitor) {
		this.designerTreeVisitor = designerTreeVisitor;
	}

	/**
	 * <p>
	 * Setter for the field <code>formJsonVisitor</code>.
	 * </p>
	 *
	 * @param formJsonVisitor
	 *            a {@link fr.becpg.repo.designer.impl.FormJsonVisitor} object.
	 */
	public void setFormJsonVisitor(FormJsonVisitor formJsonVisitor) {
		this.formJsonVisitor = formJsonVisitor;
	}

	/**
	 * <p>
	 * init.
	 * </p>
	 */
	public void init() {
		logger.debug("Init DesignerServiceImpl");

		try (InputStream in = getControlsTemplate()) {
			if (in != null) {
				controls = formModelVisitor.visitControls(in);
			}
		} catch (IOException | SAXException | ParserConfigurationException | FactoryConfigurationError e) {
			logger.error(e, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml) {
		logger.debug("call createModelAspectNode");
		M2Model m2Model = null;

		if (modelXml != null) {
			m2Model = M2Model.createModel(modelXml);
		}

		if (m2Model == null) {
			m2Model = M2Model.createModel((String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME));
		}

		NodeRef modelNodeRef = nodeService.createNode(parentNode, DesignerModel.ASSOC_MODEL, DesignerModel.ASSOC_MODEL, DesignerModel.TYPE_M2_MODEL)
				.getChildRef();

		try {
			metaModelVisitor.visitModelNodeRef(modelNodeRef, m2Model);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.error(e, e);
		}

		return modelNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public void writeXml(NodeRef nodeRef) {

		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		writer.setEncoding("UTF-8");

		try (OutputStream out = writer.getContentOutputStream();) {
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_MODEL);

			if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
				logger.debug("Write model XML");
				NodeRef modelNodeRef = findModelNodeRef(nodeRef);

				if (modelNodeRef != null) {
					metaModelVisitor.visitModelXml(modelNodeRef, out);

				}
			} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
				logger.debug("Write config XML");
				NodeRef configNodeRef = findConfigNodeRef(nodeRef);
				if (configNodeRef != null) {
					formModelVisitor.visitConfigXml(configNodeRef, out);
				}

			}
		} catch (ContentIOException | IOException | ParserConfigurationException | FactoryConfigurationError | TransformerException
				| IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.error(e, e);
		} finally {

			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_MODEL);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void publish(NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			logger.debug("Publish model");
			nodeService.setProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE, true);
		} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			nodeService.setProperty(nodeRef, DesignerModel.PROP_PUBLISHED_CONFIG_NAME, name);
			nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, MLTextHelper.getI18NMessage("designer.published"));
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef findModelNodeRef(NodeRef nodeRef) {

		if (logger.isDebugEnabled()) {
			logger.debug("Find model for nodeRef:" + nodeRef.toString());
			if (nodeRef != null) {
				logger.debug("nodeRef type:" + nodeService.getType(nodeRef).toString());
			}
		}

		NodeRef modelNodeRef = null;
		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			for (ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
				if (assoc.getQName().equals(DesignerModel.ASSOC_MODEL)) {
					return assoc.getChildRef();
				}
			}
		} else {
			if (nodeService.getType(nodeRef).equals(DesignerModel.TYPE_M2_MODEL)) {
				return nodeRef;
			}
			for (ChildAssociationRef assoc : nodeService.getParentAssocs(nodeRef)) {
				modelNodeRef = findModelNodeRef(assoc.getParentRef());
				if (modelNodeRef != null) {
					return modelNodeRef;
				}
			}
		}
		return modelNodeRef;
	}

	/**
	 * <p>
	 * findConfigNodeRef.
	 * </p>
	 *
	 * @param nodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef findConfigNodeRef(NodeRef nodeRef) {

		if (!BECPG_CONFIG_CUSTOM.equals(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME))) {

			for (ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
				if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONFIG)) {
					return assoc.getChildRef();
				}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public DesignerTree getDesignerTree(NodeRef nodeRef) {
		NodeRef treeNodeRef = null;

		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			treeNodeRef = findModelNodeRef(nodeRef);
			if (logger.isDebugEnabled() && (treeNodeRef == null)) {
				logger.debug("No assoc model found for this nodeRef");
			}
		} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
			treeNodeRef = findConfigNodeRef(nodeRef);
			if (logger.isDebugEnabled() && (treeNodeRef == null)) {
				logger.debug("No assoc config found for this nodeRef");
			}
		} else if (nodeService.getType(nodeRef).getNamespaceURI().equals(DesignerModel.M2_URI)
				|| nodeService.getType(nodeRef).getNamespaceURI().equals(DesignerModel.DESIGNER_URI)) {
			treeNodeRef = nodeRef;
		} else {
			logger.debug("Node has not mandatory aspect : model aspect. Creating ...");
		}

		if ((treeNodeRef != null) && nodeService.hasAspect(treeNodeRef, ContentModel.ASPECT_TEMPORARY)) {
			nodeService.deleteNode(treeNodeRef);
			treeNodeRef = null;
		}

		if (treeNodeRef == null) {
			if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)
					&& !BECPG_CONFIG_CUSTOM.equals(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME))) {
				treeNodeRef = createConfigAspectNode(nodeRef);
			} else {
				treeNodeRef = createModelAspectNode(nodeRef);
			}
		}

		if (treeNodeRef != null) {
			return designerTreeVisitor.visitModelTreeNodeRef(treeNodeRef);
		}

		return new DesignerTree(null);
	}

	private NodeRef createModelAspectNode(NodeRef dictionaryModelNodeRef) {
		if (ContentModel.TYPE_DICTIONARY_MODEL.equals(nodeService.getType(dictionaryModelNodeRef))) {
			ContentReader reader = contentService.getReader(dictionaryModelNodeRef, ContentModel.PROP_CONTENT);
			try (InputStream in = reader.getContentInputStream()) {
				return createModelAspectNode(dictionaryModelNodeRef, in);
			} catch (ContentIOException | IOException e) {
				logger.error(e, e);
			}
		} else {
			logger.warn("Node is not of type : dictionary model");
		}
		return null;
	}

	private NodeRef createConfigAspectNode(NodeRef parentNodeRef) {
		ContentReader reader = contentService.getReader(parentNodeRef, ContentModel.PROP_CONTENT);
		ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef, DesignerModel.ASSOC_DSG_CONFIG,
				DesignerModel.ASSOC_DSG_CONFIG, DesignerModel.TYPE_DSG_CONFIG);
		NodeRef configNodeRef = childAssociationRef.getChildRef();
		nodeService.setProperty(configNodeRef, DesignerModel.PROP_DSG_ID, nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME));

		try (InputStream in = reader.getContentInputStream()) {
			formModelVisitor.visitConfigNodeRef(configNodeRef, in);
		} catch (ContentIOException | IOException | SAXException | ParserConfigurationException | FactoryConfigurationError e) {
			logger.error(e, e);
		}
		return configNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createModelElement(NodeRef parentNodeRef, QName nodeTypeQname, QName assocQname, Map<QName, Serializable> props,
			String modelTemplate) {

		AssociationDefinition assocDef = dictionaryService.getAssociation(assocQname);
		if (!assocDef.isTargetMany()) {
			logger.debug("Assoc is unique remove existing child");
			List<ChildAssociationRef> assocs = nodeService.getChildAssocs(parentNodeRef);
			for (ChildAssociationRef assoc : assocs) {
				if (assoc.getTypeQName().equals(assocQname)) {
					nodeService.deleteNode(assoc.getChildRef());
				}
			}
		}

		ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef, assocQname, assocQname, nodeTypeQname);

		NodeRef ret = childAssociationRef.getChildRef();

		if (modelTemplate != null) {

			String[] splitted = modelTemplate.split("_");
			try (InputStream in = getModelTemplate(splitted[0])) {

				if (in != null) {
					if (nodeService.getType(parentNodeRef).getNamespaceURI().equals(DesignerModel.DESIGNER_URI)) {
						formModelVisitor.visitModelTemplate(ret, nodeTypeQname, splitted[1], in);
					} else {
						metaModelVisitor.visitModelTemplate(ret, nodeTypeQname, splitted[1], in);
					}
				}

			} catch (IOException | SAXException | ParserConfigurationException | FactoryConfigurationError | IllegalArgumentException
					| IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
				logger.error(e, e);
			}
		}
		if (props != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Set properties on node:" + ret.toString());
			}
			for (Entry<QName, Serializable> entry : props.entrySet()) {
				nodeService.setProperty(ret, entry.getKey(), entry.getValue());
			}

		}

		return ret;
	}

	private InputStream getModelTemplate(String modelTemplate) throws IOException {
		if ((modelTemplate != null) && (modelTemplate.length() > 0)) {
			Resource resource = new ClassPathResource("beCPG/designer/" + modelTemplate + ".xml");
			if (resource.exists()) {
				return resource.getInputStream();
			}
			logger.warn("No model found for :" + modelTemplate);
		}
		return null;
	}

	private InputStream getControlsTemplate() throws IOException {
		Resource resource = new ClassPathResource("beCPG/designer/formControls.xml");
		if (resource.exists()) {
			return resource.getInputStream();
		}
		logger.warn("No controls template for ");
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String prefixName(NodeRef elementRef, String name) {

		NodeRef modelNodeRef = findModelNodeRef(elementRef);
		if (modelNodeRef != null) {
			for (ChildAssociationRef assoc : nodeService.getChildAssocs(modelNodeRef)) {
				if (assoc.getQName().equals(DesignerModel.ASSOC_M2_NAMESPACES)) {
					NodeRef namespaceNodeRef = assoc.getChildRef();
					String prefix = (String) nodeService.getProperty(namespaceNodeRef, DesignerModel.PROP_M2_PREFIX);
					if (logger.isDebugEnabled()) {
						logger.debug("Prefix name : " + prefix + ":" + name);
					}
					return prefix + ":" + name;
				}
			}

		} else {
			logger.warn("Cannot find model nodeRef");
		}

		logger.warn("Could not find any namespace");

		return name;
	}

	/** {@inheritDoc} */
	@Override
	public List<FormControl> getFormControls() {
		// Allow to init designer if not (TODO Move that to get configs
		// webscript)
		if (designerInitService.getConfigsNodeRef() != null) {
			return controls;
		}
		return new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Handle move of properties from type to aspect from aspect to type from
	 * type or aspect to form or set --&gt; create field move of field from set
	 * to form from form to set move of type from model to config --&gt; create
	 * form
	 */
	@Override
	public NodeRef moveElement(NodeRef from, NodeRef to) {

		NodeRef ret = null;
		QName typeFrom = nodeService.getType(from);
		QName typeTo = nodeService.getType(to);
		if (logger.isDebugEnabled()) {
			logger.debug("Try to move node from type :" + typeFrom + " to :" + typeTo);
		}

		if (DesignerModel.TYPE_M2_PROPERTY.equals(typeFrom) || DesignerModel.TYPE_M2_ASSOCIATION.equals(typeFrom)
				|| DesignerModel.TYPE_M2_CHILD_ASSOCIATION.equals(typeFrom) || DesignerModel.TYPE_M2_PROPERTY_OVERRIDE.equals(typeFrom)) {
			logger.debug("Node is a property");
			if (DesignerModel.TYPE_M2_TYPE.equals(typeTo) || DesignerModel.TYPE_M2_ASPECT.equals(typeTo)) {
				logger.debug("Move to type or aspect");

				if (DesignerModel.TYPE_M2_ASSOCIATION.equals(typeFrom) || DesignerModel.TYPE_M2_CHILD_ASSOCIATION.equals(typeFrom)) {
					ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_M2_ASSOCIATIONS,
							DesignerModel.ASSOC_M2_ASSOCIATIONS);
					ret = assocRef.getChildRef();
				} else if (DesignerModel.TYPE_M2_PROPERTY_OVERRIDE.equals(typeFrom)) {
					ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_M2_PROPERTY_OVERRIDES,
							DesignerModel.ASSOC_M2_PROPERTY_OVERRIDES);
					ret = assocRef.getChildRef();
				} else {
					ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_M2_PROPERTIES,
							DesignerModel.ASSOC_M2_PROPERTIES);
					ret = assocRef.getChildRef();
				}
			}
			if (DesignerModel.TYPE_DSG_FORM.equals(typeTo) || DesignerModel.TYPE_DSG_FORMSET.equals(typeTo)) {
				logger.debug("Create field");

				ChildAssociationRef assocRef = nodeService.createNode(to, DesignerModel.ASSOC_DSG_FIELDS, DesignerModel.ASSOC_DSG_FIELDS,
						DesignerModel.TYPE_DSG_FORMFIELD);
				ret = assocRef.getChildRef();

				// Copy prop name to field ID
				nodeService.setProperty(ret, DesignerModel.PROP_DSG_ID, nodeService.getProperty(from, DesignerModel.PROP_M2_NAME));

			}
		}

		else if (DesignerModel.TYPE_DSG_FORMFIELD.equals(typeFrom)) {
			if (DesignerModel.TYPE_DSG_FORM.equals(typeTo) || DesignerModel.TYPE_DSG_FORMSET.equals(typeTo)) {
				logger.debug("Move field");
				ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_DSG_FIELDS, DesignerModel.ASSOC_DSG_FIELDS);
				ret = assocRef.getChildRef();

			}
		} else if (DesignerModel.TYPE_M2_TYPE.equals(typeFrom)) {
			if (DesignerModel.TYPE_DSG_CONFIG.equals(typeTo)) {
				ret = formModelVisitor.visitM2Type(from, to);

			}
		} else if (DesignerModel.TYPE_M2_ASPECT.equals(typeFrom)) {
			if (DesignerModel.TYPE_DSG_FORM.equals(typeTo) || DesignerModel.TYPE_DSG_FORMSET.equals(typeTo)) {
				ret = formModelVisitor.visitM2Properties(to, from);
			} else if (DesignerModel.TYPE_M2_TYPE.equals(typeTo) || DesignerModel.TYPE_M2_ASPECT.equals(typeTo)) {
				@SuppressWarnings("unchecked")
				List<String> aspects = (List<String>) nodeService.getProperty(to, DesignerModel.PROP_M2_MANDATORYASPECTS);
				if (aspects == null) {
					aspects = new ArrayList<>();
				}
				String aspect = (String) nodeService.getProperty(from, DesignerModel.PROP_M2_NAME);
				if (!aspects.contains(aspect)) {
					aspects.add(aspect);
					nodeService.setProperty(to, DesignerModel.PROP_M2_MANDATORYASPECTS, (Serializable) aspects);
				}
				ret = to;
			}

		}
		if (ret == null) {
			logger.warn("unknow type");
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef findOrCreateModel(String modelName, String modelTemplate, Map<String, Object> templateContext) {

		NodeRef modelNodeRef = null;
		try {
			modelNodeRef = findOrCreateModelFile(designerInitService.getModelsNodeRef(), modelName, modelTemplate, templateContext, false);
		} catch (IOException | TemplateException e) {
			logger.error(e, e);
		}

		NodeRef ret = findModelNodeRef(modelNodeRef);
		if (ret == null) {
			return createModelAspectNode(modelNodeRef);
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef findOrCreateConfig(String configName, String modelTemplate, Map<String, Object> templateContext) {

		NodeRef modelNodeRef = null;
		try {
			modelNodeRef = findOrCreateModelFile(designerInitService.getConfigsNodeRef(), configName, modelTemplate, templateContext, true);
		} catch (IOException | TemplateException e) {
			logger.error(e, e);
		}
		NodeRef ret = findConfigNodeRef(modelNodeRef);
		if (ret == null) {
			return createConfigAspectNode(modelNodeRef);
		}
		return ret;
	}

	private NodeRef findOrCreateModelFile(NodeRef parentNodeRef, String modelName, String modelTemplate, Map<String, Object> templateContext,
			boolean isConfig) throws IOException, TemplateException {
		NodeRef modelNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, modelName);

		if (modelNodeRef == null) {
			logger.debug("Model file " + modelName + " not found creating from :" + modelTemplate);

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, modelName);

			modelNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					isConfig ? ContentModel.TYPE_CONTENT : ContentModel.TYPE_DICTIONARY_MODEL, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(modelNodeRef, ContentModel.PROP_CONTENT, true);

			writer.setMimetype(mimetypeService.guessMimetype(modelName));

			Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			TemplateLoader templateLoader = new ClassTemplateLoader(DesignerServiceImpl.class, "/beCPG/designer/");
			cfg.setTemplateLoader(templateLoader);
			Template ftlTemplate = cfg.getTemplate(modelTemplate);

			try (Writer out = new OutputStreamWriter(writer.getContentOutputStream())) {
				ftlTemplate.process(templateContext, out);
				out.flush();
			}

		}
		return modelNodeRef;

	}

	/** {@inheritDoc} */
	@Override
	public void unpublish(NodeRef nodeRef) {
		policyBehaviourFilter.disableBehaviour(nodeRef);
		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			try {
				logger.debug("Publish model");
				nodeService.setProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE, false);
			} finally {
				policyBehaviourFilter.enableBehaviour(nodeRef);
			}
			dictionaryDAO.reset();
		} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
			nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, MLTextHelper.getI18NMessage("designer.not-published"));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createAndPublishConfig(NodeRef nodeRef) {
		policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_CONFIG);
		policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_MODEL);

		logger.debug("Creating and publishing: " + nodeRef);

		NodeRef configNodeRef = null;

		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			configNodeRef = findModelNodeRef(nodeRef);
		} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
			configNodeRef = findConfigNodeRef(nodeRef);
		}

		if (configNodeRef != null) {
			nodeService.addAspect(configNodeRef, ContentModel.ASPECT_TEMPORARY, new HashMap<>());
		}

		policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_CONFIG);
		policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_MODEL);

	}

	/** {@inheritDoc} */
	@Override
	public String export(NodeRef nodeRef) {
		try {
			JSONObject ret = new JSONObject();

			if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
				logger.debug("Write config XML");
				NodeRef configNodeRef = findConfigNodeRef(nodeRef);
				formJsonVisitor.visit(configNodeRef, ret);

			}

			return ret.toString(3);
		} catch (JSONException e) {
			logger.error(e, e);
		}
		return null;
	}

}
