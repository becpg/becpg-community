package fr.becpg.repo.report.entity.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * <p>DefaultExtractorContextTest class.</p>
 *
 * Covers how assocsToExtract is read: system configuration OR report preferences, exact names
 * and the entity_ prefix restricted to the root entity.
 *
 * @author valentin
 */
public class DefaultExtractorContextTest {

    private static final String ASSOCS_TO_EXTRACT = "assocsToExtract";

    private static final String SYSTEM_CONF = "bcpg:plants,bcpg:suppliers";

    private static final NodeRef ROOT_NODE_REF = new NodeRef("workspace://SpacesStore/root");

    private static final NodeRef CHILD_NODE_REF = new NodeRef("workspace://SpacesStore/child");

    @Test
    public void testSystemConfAssocIsKeptWhenReportDeclaresItsOwnAssocs() {
        DefaultExtractorContext context = contextWithReportAssocs("bcpg:nutListNut");

        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "bcpg:plants", CHILD_NODE_REF));
    }

    @Test
    public void testReportAssocIsExtracted() {
        DefaultExtractorContext context = contextWithReportAssocs("bcpg:nutListNut");

        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "bcpg:nutListNut", CHILD_NODE_REF));
    }

    @Test
    public void testUnlistedAssocIsNotExtracted() {
        DefaultExtractorContext context = contextWithReportAssocs("bcpg:nutListNut");

        assertFalse(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "bcpg:clients", ROOT_NODE_REF));
    }

    @Test
    public void testRootEntityPrefixOnlyMatchesTheRootEntity() {
        DefaultExtractorContext context = contextWithReportAssocs("entity_au:linkedProducts");

        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "au:linkedProducts", ROOT_NODE_REF));
        assertFalse(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "au:linkedProducts", CHILD_NODE_REF));
    }

    @Test
    public void testUnprefixedAssocMatchesAtEveryLevel() {
        DefaultExtractorContext context = contextWithReportAssocs("au:linkedProducts");

        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "au:linkedProducts", ROOT_NODE_REF));
        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, SYSTEM_CONF, "au:linkedProducts", CHILD_NODE_REF));
    }

    @Test
    public void testSemicolonsAndBlanksAreAcceptedAsSeparators() {
        DefaultExtractorContext context = contextWithReportAssocs(" pack:pmMaterialRefs; bcpg:nutListNut  survey:slQuestion ");

        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, null, "pack:pmMaterialRefs", CHILD_NODE_REF));
        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, null, "bcpg:nutListNut", CHILD_NODE_REF));
        assertTrue(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, null, "survey:slQuestion", CHILD_NODE_REF));
    }

    @Test
    public void testPartialNameDoesNotMatch() {
        DefaultExtractorContext context = contextWithReportAssocs("bcpg:plantsCertifications");

        assertFalse(context.prefsContainsAssoc(ASSOCS_TO_EXTRACT, null, "bcpg:plants", CHILD_NODE_REF));
    }

    private static DefaultExtractorContext contextWithReportAssocs(String reportAssocs) {
        Map<String, String> preferences = new HashMap<>();
        preferences.put(ASSOCS_TO_EXTRACT, reportAssocs);
        return new DefaultExtractorContext(preferences, ROOT_NODE_REF);
    }
}
