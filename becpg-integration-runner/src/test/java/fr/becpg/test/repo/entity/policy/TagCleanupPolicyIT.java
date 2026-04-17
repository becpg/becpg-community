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
package fr.becpg.test.repo.entity.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration tests for {@link fr.becpg.repo.entity.policy.TagCleanupPolicy}.
 *
 * <p>Reproduces ticket #33392 : once the last tag of an entity is removed,
 * the {@code cm:taggable} aspect must be dropped so that Solr re-indexes the
 * node and the entity no longer matches tag-based filters.</p>
 */
public class TagCleanupPolicyIT extends PLMBaseTestCase {

    private static final String TAG_NAME = "tag-cleanup-policy-it";
    private static final String SECOND_TAG_NAME = "tag-cleanup-policy-it-2";

    @Autowired
    private TaggingService taggingService;

    @Test
    public void testAspectRemovedWhenLastTagCleared() {
        final NodeRef entityNodeRef = createTaggableRawMaterial("Entity single tag");

        inWriteTx(() -> {
            taggingService.addTag(entityNodeRef, TAG_NAME);
            return null;
        });

        inReadTx(() -> {
            Assert.assertTrue("cm:taggable aspect should be present after adding a tag",
                    nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_TAGGABLE));
            return null;
        });

        inWriteTx(() -> {
            nodeService.setProperty(entityNodeRef, ContentModel.PROP_TAGS, new ArrayList<NodeRef>());
            return null;
        });

        inReadTx(() -> {
            Assert.assertFalse("cm:taggable aspect must be removed once the last tag is cleared",
                    nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_TAGGABLE));
            return null;
        });
    }

    @Test
    public void testAspectKeptWhenSomeTagsRemain() {
        final NodeRef entityNodeRef = createTaggableRawMaterial("Entity multi tags");

        inWriteTx(() -> {
            taggingService.addTags(entityNodeRef, List.of(TAG_NAME, SECOND_TAG_NAME));
            return null;
        });

        inWriteTx(() -> {
            taggingService.removeTag(entityNodeRef, TAG_NAME);
            return null;
        });

        inReadTx(() -> {
            Assert.assertTrue("cm:taggable aspect must stay while at least one tag remains",
                    nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_TAGGABLE));
            List<String> remaining = taggingService.getTags(entityNodeRef);
            Assert.assertEquals(Collections.singletonList(SECOND_TAG_NAME), remaining);
            return null;
        });
    }

    private NodeRef createTaggableRawMaterial(final String name) {
        return inWriteTx(() -> {
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);
            return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), PLMModel.TYPE_RAWMATERIAL, properties)
                    .getChildRef();
        });
    }

}
