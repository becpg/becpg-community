/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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

public interface GS1Model {

	String GS1_URI = "http://www.bcpg.fr/model/gs1/1.0";

	String BECPG_PREFIX = "gs1";

	/** The Constant MODEL. */
	QName MODEL = QName.createQName(GS1_URI, "gs1Model");

	QName TYPE_DELIVERY_CHANNEL = QName.createQName(GS1_URI, "DeliveryChannel");

	QName ASPECT_MEASURES_ASPECT = QName.createQName(GS1_URI, "measuresAspect");

	QName PROP_WEIGHT = QName.createQName(GS1_URI, "weight");
	QName PROP_SECONDARY_WEIGHT = QName.createQName(GS1_URI, "secondaryWeight");
	QName PROP_TERTIARY_WEIGHT = QName.createQName(GS1_URI, "tertiaryWeight");
	QName PROP_SECONDARY_NET_WEIGHT = QName.createQName(GS1_URI, "secondaryNetWeight");
	QName PROP_TERTIARY_NET_WEIGHT = QName.createQName(GS1_URI, "tertiaryNetWeight");

}
