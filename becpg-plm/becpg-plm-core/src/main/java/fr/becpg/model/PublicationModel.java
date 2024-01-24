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
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>PublicationModel interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PublicationModel {

	private PublicationModel() {
		//Constants only
	}

	/** Constant <code>PUBLICATION_URI="http://www.bcpg.fr/model/publication/1."{trunked}</code> */
	public static final String PUBLICATION_URI = "http://www.bcpg.fr/model/publication/1.0";

	/** Constant <code>PUBLICATION_PREFIX="bp"</code> */
	public static  final String PUBLICATION_PREFIX = "bp";
	
	/** 
	 * The Constant MODEL. 
	 */
	public static final QName MODEL = QName.createQName(PUBLICATION_URI, "publicationModel");

	public static final QName TYPE_PUBLICATION_CHANNEL = QName.createQName(PUBLICATION_URI, "pubChannel");
	public static final QName TYPE_PUBLICATION_CHANNEL_LIST = QName.createQName(PUBLICATION_URI, "pubChannelList");

	public static final QName ASSOC_PUBCHANNELLIST_CHANNEL = QName.createQName(PUBLICATION_URI, "pubChannelListChannel");
	public static final QName PROP_PUBCHANNEL_CATALOG_ID = QName.createQName(PUBLICATION_URI, "pubChannelCatalogId");
	public static final QName PROP_PUBCHANNELLIST_MODIFIED_DATE = QName.createQName(PUBLICATION_URI, "pubChannelListModifiedDate");
	public static final QName PROP_PUBCHANNEL_ID = QName.createQName(PUBLICATION_URI, "pubChannelId");
	public static final QName PROP_PUBCHANNEL_ACTION = QName.createQName(PUBLICATION_URI, "pubChannelAction");
	public static final QName PROP_PUBCHANNEL_LASTDATE = QName.createQName(PUBLICATION_URI, "pubChannelLastDate");

	public static final QName PROP_PUBCHANNELLIST_BATCHID = QName.createQName(PUBLICATION_URI, "pubChannelListBatchId");
	public static final QName PROP_PUBCHANNELLIST_PUBLISHEDDATE = QName.createQName(PUBLICATION_URI, "pubChannelListPublishedDate");
	public static final QName PROP_PUBCHANNELLIST_STATUS = QName.createQName(PUBLICATION_URI, "pubChannelListStatus");
	public static final QName PROP_PUBCHANNELLIST_ERROR = QName.createQName(PUBLICATION_URI, "pubChannelListError");
	public static final QName PROP_PUBCHANNELLIST_ACTION = QName.createQName(PUBLICATION_URI, "pubChannelListAction");
	public static final QName PROP_PUBCHANNEL_CONFIG = QName.createQName(PUBLICATION_URI, "pubChannelConfig");
	public static final QName ASSOC_PUBCHANNEL_CONFIGFILE = QName.createQName(PUBLICATION_URI, "pubChannelConfigFile");

	public static final QName PROP_PUBCHANNEL_BATCHSTARTTIME = QName.createQName(PUBLICATION_URI, "pubChannelBatchStartTime");
	public static final QName PROP_PUBCHANNEL_BATCHENDTIME = QName.createQName(PUBLICATION_URI, "pubChannelBatchEndTime");
	public static final QName PROP_PUBCHANNEL_BATCHDURATION = QName.createQName(PUBLICATION_URI, "pubChannelBatchDuration");
	public static final QName PROP_PUBCHANNEL_BATCHID = QName.createQName(PUBLICATION_URI, "pubChannelBatchId");
	public static final QName PROP_PUBCHANNEL_FAILCOUNT = QName.createQName(PUBLICATION_URI, "pubChannelFailCount");
	public static final QName PROP_PUBCHANNEL_READCOUNT = QName.createQName(PUBLICATION_URI, "pubChannelReadCount");
	public static final QName PROP_PUBCHANNEL_ERROR = QName.createQName(PUBLICATION_URI, "pubChannelError");
	public static final QName PROP_PUBCHANNEL_LASTSUCCESSBATCHID = QName.createQName(PUBLICATION_URI, "pubChannelLastSuccessBatchId");
	public static final QName PROP_PUBCHANNEL_STATUS = QName.createQName(PUBLICATION_URI, "pubChannelStatus");

}
