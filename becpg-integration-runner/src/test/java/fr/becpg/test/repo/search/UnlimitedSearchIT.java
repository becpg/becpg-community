/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.search;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.RepoBaseTestCase;

/**
 *
 * @author matthieu
 */
public class UnlimitedSearchIT extends RepoBaseTestCase {

	protected static final Log logger = LogFactory.getLog(UnlimitedSearchIT.class);

	@Test
	public void testUnlimitedSearch() throws Exception {

		Date date = Calendar.getInstance().getTime();

		final int searchSize = 1015;

		// in DB test
		inWriteTx(() -> {

			for (int i = 0; i < searchSize; i++) {

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, "UnlimitedSearchTest" + i);
				properties.put(ContentModel.PROP_TITLE, "UnlimitedSearchTest-" + date.getTime());
				nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
								(String) properties.get(ContentModel.PROP_NAME)),
						BeCPGModel.TYPE_LIST_VALUE, properties);

			}
			return true;
		});

		// in DB test
		inWriteTx(() -> {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LIST_VALUE).inDB()
					.ftsLanguage().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
					.andPropEquals(ContentModel.PROP_TITLE, "UnlimitedSearchTest-" + date.getTime());

			assertEquals(query.list().size(), searchSize);
			assertEquals(query.andBetween(ContentModel.PROP_MODIFIED, ISO8601DateFormat.format(date),
					ISO8601DateFormat.format(Calendar.getInstance().getTime())).list().size(), searchSize);

			return true;
		});

		waitForSolr();
		// solr test
		inWriteTx(() -> {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LIST_VALUE)
					.andPropQuery(ContentModel.PROP_NAME, "UnlimitedSearchTest")
					.andPropEquals(ContentModel.PROP_TITLE, "UnlimitedSearchTest-" + date.getTime())
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);

			assertEquals(query.list().size(), searchSize);
			assertEquals(query.andBetween(ContentModel.PROP_MODIFIED, ISO8601DateFormat.format(date),
					ISO8601DateFormat.format(Calendar.getInstance().getTime())).list().size(), searchSize);

			return true;
		});
	}

}
