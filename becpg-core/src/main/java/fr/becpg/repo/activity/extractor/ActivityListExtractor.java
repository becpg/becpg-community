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
package fr.becpg.repo.activity.extractor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>ActivityListExtractor class.</p>
 *
 * @author matthieu Extract activity Fields
 * @version $Id: $Id
 */
public class ActivityListExtractor extends SimpleExtractor {

	private static Log logger = LogFactory.getLog(ActivityListExtractor.class);

	private EntityActivityService entityActivityService;

	private EntityActivityExtractorService entityActivityExtractorService;
	
	private static final String PROP_BECPG_ALDATA = "prop_bcpg_alData";
	
	/**
	 * <p>Setter for the field <code>entityActivityService</code>.</p>
	 *
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object.
	 */
	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
	}
	
	public void setEntityActivityExtractorService(EntityActivityExtractorService entityActivityExtractorService) {
		this.entityActivityExtractorService = entityActivityExtractorService;
	}
	
	/** {@inheritDoc} */
	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		Map<String, Boolean> sortMap = new LinkedHashMap<>();

		sortMap.put("@cm:created", false);

		dataListFilter.setSortMap(sortMap);

		return super.getListNodeRef(dataListFilter, pagination);
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, FormatMode mode,
			Map<QName, Serializable> properties, Map<String, Object> props, Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.doExtract(nodeRef, itemType, metadataFields, mode, properties, props, cache);
		if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(itemType)) {
			postLookupActivity(nodeRef, ret, properties, mode);
		}
		return ret;

	}

	/**
	 * <p>postLookupActivity.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param ret a {@link java.util.Map} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 */
	protected void postLookupActivity(NodeRef nodeRef, Map<String, Object> ret, Map<QName, Serializable> properties, FormatMode mode) {

		String activityType = (String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_TYPE);
		if (activityType != null) {

			ret.put("prop_bcpg_alUserId", extractPerson((String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_USERID)));
			JSONObject postLookup = entityActivityService.postActivityLookUp(
					ActivityType.valueOf((String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_TYPE)),
					(String) properties.get(BeCPGModel.PROP_ACTIVITYLIST_DATA));
			if (postLookup != null) {
				if (FormatMode.JSON.equals(mode) || FormatMode.XLSX.equals(mode)) {
					try {
						entityActivityExtractorService.formatPostLookup(postLookup);
					} catch (JSONException e) {
						logger.error(e, e);
					}
					ret.put(PROP_BECPG_ALDATA, postLookup);
				} else {
					try {
						if (postLookup.has("content")) {
							ret.put(PROP_BECPG_ALDATA, postLookup.get("content"));
						} else {
							ret.put(PROP_BECPG_ALDATA, "");
						}
					} catch (JSONException e) {
						logger.error(e, e);
					}
				}
			} else {
				logger.warn("No activity type for node :" + nodeRef);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
//		return (dataListFilter.getDataType() != null) && dataListFilter.getDataType().equals(BeCPGModel.TYPE_ACTIVITY_LIST);
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
