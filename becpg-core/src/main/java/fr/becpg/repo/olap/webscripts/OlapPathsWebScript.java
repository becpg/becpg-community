/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.olap.webscripts;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;

/**
 * Publishes the localised folder names beCPG OLAP has to resolve in the repository.
 *
 * <p>beCPG OLAP stores a user's own queries in a folder of their home space. That folder carries the
 * translation of {@code path.olapqueries}, which exists in a dozen languages, so the OLAP cannot
 * guess it. Rather than duplicating the bundle on the analytics side, it asks for the name the
 * repository actually uses.
 *
 * @author matthieu
 */
public class OlapPathsWebScript extends DeclarativeWebScript {

	private static final String PARAM_OLAP_QUERIES = "olapQueriesFolderName";

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();
		model.put(PARAM_OLAP_QUERIES, TranslateHelper.getTranslatedPath(RepoConsts.PATH_OLAP_QUERIES));
		return model;
	}
}
