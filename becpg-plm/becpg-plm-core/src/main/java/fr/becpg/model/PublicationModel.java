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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface PublicationModel {

	String PUBLICATION_URI = "http://www.bcpg.fr/model/publication/1.0";

	String PUBLICATION_PREFIX = "bp";

	/** 
	 * The Constant MODEL. 
	 */
	QName MODEL = QName.createQName(PUBLICATION_URI, "publicationModel");

	/**
	 * Mailing List
	 */

	QName TYPE_DELIVERY_CHANNEL = QName.createQName(PUBLICATION_URI, "MailingListChannel");

	QName PROP_MAILLING_MEMBERS = QName.createQName(PUBLICATION_URI, "mailingMembers");

	/**
	 * Product catalog
	 */
	QName TYPE_PRODUCT_CATALOG = QName.createQName(PUBLICATION_URI, "productCatalog");

	QName PROP_PRODUCT_CATALOG_ID = QName.createQName(PUBLICATION_URI, "productCatalogId");

	QName ASSOC_PRODUCT_CATALOGS = QName.createQName(PUBLICATION_URI, "productCatalogs");

}
