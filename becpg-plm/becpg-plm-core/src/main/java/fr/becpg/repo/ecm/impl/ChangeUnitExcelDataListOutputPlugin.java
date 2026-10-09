/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.ecm.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.ECMModel;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.StandardExcelDataListOutputPlugin;

/**
 * Exports the change units of a change order with their scores as text instead of the JSON the
 * notifications widget reads.
 */
@Service
public class ChangeUnitExcelDataListOutputPlugin extends StandardExcelDataListOutputPlugin {

	private static final Log logger = LogFactory.getLog(ChangeUnitExcelDataListOutputPlugin.class);

	static final String ENTITY_SCORE_FIELD = "prop_ecm_culEntityScore";

	/** An Excel cell holds at most 32,767 characters. */
	static final int MAX_CELL_LENGTH = 32000;

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return ECMModel.TYPE_CHANGEUNITLIST.equals(dataListFilter.getDataType());
	}

	/** {@inheritDoc} */
	@Override
	public List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException {
		if (items != null) {
			for (Map<String, Object> item : items) {
				Object score = item.get(ENTITY_SCORE_FIELD);
				if (score != null) {
					item.put(ENTITY_SCORE_FIELD, toText(score));
				}
			}
		}
		return items;
	}

	/**
	 * Formats the scores of one change unit, keeping the raw value when it is not the expected JSON.
	 *
	 * @param score the value extracted for {@code ecm:culEntityScore}
	 * @return the text of the cell
	 */
	static Object toText(Object score) {
		try {
			JSONObject json = score instanceof JSONObject jsonObject ? jsonObject : new JSONObject(score.toString());
			String text = ChangeUnitScoreFormatter.format(json, I18NUtil.getLocale(), I18NUtil::getMessage);
			return text.length() > MAX_CELL_LENGTH ? text.substring(0, MAX_CELL_LENGTH) : text;
		} catch (JSONException e) {
			logger.warn("Cannot read the scores of a change unit: " + e.getMessage());
			return score;
		}
	}
}
