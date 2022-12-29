/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.RepoBaseTestCase;

/**
 *
 * @author matthieu
 */
public class DBSearchIT extends RepoBaseTestCase {

	protected static final Log logger = LogFactory.getLog(DBSearchIT.class);


	@Test
	public void testDBSearch() throws Exception {

		List<NodeRef> nodeRefs = new ArrayList<>();

		Date date = Calendar.getInstance().getTime();

		final int searchSize = 10;

		for (int i = 0; i < searchSize; i++) {
			final int count = i;
			nodeRefs.add(inWriteTx(() -> {

				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Search test-" + count);
				finishedProduct1.setLegalName("Search test-" + date.getTime());
				finishedProduct1.setEndEffectivity(date);
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();

			}));

		}
		inReadTx(() -> {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_FINISHEDPRODUCT).inDB().ftsLanguage()
					.andPropEquals(BeCPGModel.PROP_LEGAL_NAME, "Search test-" + date.getTime());

			assertEquals(query.list().size(), 10);
			assertEquals(query.andBetween(ContentModel.PROP_MODIFIED, ISO8601DateFormat.format(date),
					ISO8601DateFormat.format(Calendar.getInstance().getTime())).list().size(), searchSize);

			assertEquals(
					query.andBetween(BeCPGModel.PROP_END_EFFECTIVITY, ISO8601DateFormat.format(date), ISO8601DateFormat.format(date)).list().size(),
					searchSize);
			assertEquals(query.andBetween(BeCPGModel.PROP_END_EFFECTIVITY, "MIN", ISO8601DateFormat.format(date)).list().size(), searchSize);
			assertEquals(query.andBetween(BeCPGModel.PROP_END_EFFECTIVITY, ISO8601DateFormat.format(Calendar.getInstance().getTime()), "MAX").list()
					.size(), 0);

			return true;
		});
	}

}
