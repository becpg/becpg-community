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

	/** Constant <code>TYPE_PUBLICATION_CHANNEL</code> */
	public static final QName TYPE_PUBLICATION_CHANNEL = QName.createQName(PUBLICATION_URI, "pubChannel");
	/** Constant <code>TYPE_PUBLICATION_CHANNEL_LIST</code> */
	public static final QName TYPE_PUBLICATION_CHANNEL_LIST = QName.createQName(PUBLICATION_URI, "pubChannelList");

	/** Constant <code>ASSOC_PUBCHANNELLIST_CHANNEL</code> */
	public static final QName ASSOC_PUBCHANNELLIST_CHANNEL = QName.createQName(PUBLICATION_URI, "pubChannelListChannel");
	/** Constant <code>PROP_PUBCHANNEL_CATALOG_ID</code> */
	public static final QName PROP_PUBCHANNEL_CATALOG_ID = QName.createQName(PUBLICATION_URI, "pubChannelCatalogId");
	/** Constant <code>PROP_PUBCHANNELLIST_MODIFIED_DATE</code> */
	public static final QName PROP_PUBCHANNELLIST_MODIFIED_DATE = QName.createQName(PUBLICATION_URI, "pubChannelListModifiedDate");
	/** Constant <code>PROP_PUBCHANNEL_ID</code> */
	public static final QName PROP_PUBCHANNEL_ID = QName.createQName(PUBLICATION_URI, "pubChannelId");
	/** Constant <code>PROP_PUBCHANNEL_ACTION</code> */
	public static final QName PROP_PUBCHANNEL_ACTION = QName.createQName(PUBLICATION_URI, "pubChannelAction");
	/** Constant <code>PROP_PUBCHANNEL_LASTDATE</code> */
	public static final QName PROP_PUBCHANNEL_LASTDATE = QName.createQName(PUBLICATION_URI, "pubChannelLastDate");

	/** Constant <code>PROP_PUBCHANNELLIST_BATCHID</code> */
	public static final QName PROP_PUBCHANNELLIST_BATCHID = QName.createQName(PUBLICATION_URI, "pubChannelListBatchId");
	/** Constant <code>PROP_PUBCHANNELLIST_PUBLISHEDDATE</code> */
	public static final QName PROP_PUBCHANNELLIST_PUBLISHEDDATE = QName.createQName(PUBLICATION_URI, "pubChannelListPublishedDate");
	/** Constant <code>PROP_PUBCHANNELLIST_STATUS</code> */
	public static final QName PROP_PUBCHANNELLIST_STATUS = QName.createQName(PUBLICATION_URI, "pubChannelListStatus");
	/** Constant <code>PROP_PUBCHANNELLIST_ERROR</code> */
	public static final QName PROP_PUBCHANNELLIST_ERROR = QName.createQName(PUBLICATION_URI, "pubChannelListError");
	/** Constant <code>PROP_PUBCHANNELLIST_ACTION</code> */
	public static final QName PROP_PUBCHANNELLIST_ACTION = QName.createQName(PUBLICATION_URI, "pubChannelListAction");
	/** Constant <code>PROP_PUBCHANNEL_CONFIG</code> */
	public static final QName PROP_PUBCHANNEL_CONFIG = QName.createQName(PUBLICATION_URI, "pubChannelConfig");
	/** Constant <code>ASSOC_PUBCHANNEL_CONFIGFILE</code> */
	public static final QName ASSOC_PUBCHANNEL_CONFIGFILE = QName.createQName(PUBLICATION_URI, "pubChannelConfigFile");

	/** Constant <code>PROP_PUBCHANNEL_BATCHSTARTTIME</code> */
	public static final QName PROP_PUBCHANNEL_BATCHSTARTTIME = QName.createQName(PUBLICATION_URI, "pubChannelBatchStartTime");
	/** Constant <code>PROP_PUBCHANNEL_BATCHENDTIME</code> */
	public static final QName PROP_PUBCHANNEL_BATCHENDTIME = QName.createQName(PUBLICATION_URI, "pubChannelBatchEndTime");
	/** Constant <code>PROP_PUBCHANNEL_BATCHDURATION</code> */
	public static final QName PROP_PUBCHANNEL_BATCHDURATION = QName.createQName(PUBLICATION_URI, "pubChannelBatchDuration");
	/** Constant <code>PROP_PUBCHANNEL_BATCHID</code> */
	public static final QName PROP_PUBCHANNEL_BATCHID = QName.createQName(PUBLICATION_URI, "pubChannelBatchId");
	/** Constant <code>PROP_PUBCHANNEL_FAILCOUNT</code> */
	public static final QName PROP_PUBCHANNEL_FAILCOUNT = QName.createQName(PUBLICATION_URI, "pubChannelFailCount");
	/** Constant <code>PROP_PUBCHANNEL_READCOUNT</code> */
	public static final QName PROP_PUBCHANNEL_READCOUNT = QName.createQName(PUBLICATION_URI, "pubChannelReadCount");
	/** Constant <code>PROP_PUBCHANNEL_ERROR</code> */
	public static final QName PROP_PUBCHANNEL_ERROR = QName.createQName(PUBLICATION_URI, "pubChannelError");
	/** Constant <code>PROP_PUBCHANNEL_LASTSUCCESSBATCHID</code> */
	public static final QName PROP_PUBCHANNEL_LASTSUCCESSBATCHID = QName.createQName(PUBLICATION_URI, "pubChannelLastSuccessBatchId");
	/** Constant <code>PROP_PUBCHANNEL_STATUS</code> */
	public static final QName PROP_PUBCHANNEL_STATUS = QName.createQName(PUBLICATION_URI, "pubChannelStatus");

	/** Constant <code>PROP_FAILED_CHANNELIDS</code> */
	public static final QName PROP_FAILED_CHANNELIDS = QName.createQName(PUBLICATION_URI, "failedPubChannelIds");
	/** Constant <code>PROP_PUBLISHED_CHANNELIDS</code> */
	public static final QName PROP_PUBLISHED_CHANNELIDS = QName.createQName(PUBLICATION_URI, "publishedPubChannelIds");
	/** Constant <code>PROP_CHANNELIDS</code> */
	public static final QName PROP_CHANNELIDS = QName.createQName(PUBLICATION_URI, "pubChannelIds");

}
