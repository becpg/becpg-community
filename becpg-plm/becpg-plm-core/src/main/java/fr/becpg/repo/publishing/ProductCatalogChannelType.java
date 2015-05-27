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
package fr.becpg.repo.publishing;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PublicationModel;

/**
 * 
 * @author matthieu
 * 
 */
public class ProductCatalogChannelType extends AbstractChannelType {

	public final static String ID = "ProductCatalog";
	
	private static Log logger = LogFactory.getLog(ProductCatalogChannelType.class);

	@Override
	public String getTitle() {
		return ID;
	}

	@Override
	public boolean canPublish() {
		return true;
	}

	@Override
	public boolean canPublishStatusUpdates() {
		return false;
	}

	@Override
	public boolean canUnpublish() {
		return false;
	}

	@Override
	public QName getChannelNodeType() {
		return PublicationModel.TYPE_PRODUCT_CATALOG;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Resource getIcon(String sizeSuffix) {
		String className = "beCPG/publishing/" + this.getClass().getSimpleName();
		StringBuilder iconPath = new StringBuilder(className);
		iconPath.append(sizeSuffix).append('.').append(getIconFileExtension());
		Resource resource = new ClassPathResource(iconPath.toString());
		return resource.exists() ? resource : null;
	}

	public String getIconFileExtension() {
		return "png";
	}

	@Override
	public Set<QName> getSupportedContentTypes() {
		Set<QName> types = new HashSet<QName>();
		types.add(PLMModel.TYPE_FINISHEDPRODUCT);
		return types;
	}

	
	@Override
	public void publish(NodeRef nodeToPublish, Map<QName, Serializable> channelProperties) {
		if(logger.isDebugEnabled()){
			logger.debug("Publish to product Catalog:"+channelProperties.get(ContentModel.PROP_NAME));
			logger.debug("With identifier:"+channelProperties.get(PublicationModel.PROP_PRODUCT_CATALOG_ID));
		}
				
	}

	
	

}
