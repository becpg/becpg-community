package fr.becpg.repo.jscript.app;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * Unit tests for the content URL built by {@link BeCPGJSONConversionComponent}.
 */
public class BeCPGJSONConversionComponentTest {

    private static final String NODE_REF = "workspace://SpacesStore/0a1b2c3d";

    private static final String URL_PATTERN = "/slingshot/node/content/{0}/{1}/{2}/{3}";

    @Test
    public void buildsContentUrlWithUtf8EncodedEmoji() {
        FileInfo nodeInfo = mockFileInfo("test 😀.pdf");

        assertEquals("/slingshot/node/content/workspace/SpacesStore/0a1b2c3d/test%20%F0%9F%98%80.pdf",
                BeCPGJSONConversionComponent.buildContentURL(URL_PATTERN, nodeInfo));
    }

    @Test
    public void keepsSingleQuoteInFileName() {
        FileInfo nodeInfo = mockFileInfo("l'etiquette.pdf");

        assertEquals("/slingshot/node/content/workspace/SpacesStore/0a1b2c3d/l%27etiquette.pdf",
                BeCPGJSONConversionComponent.buildContentURL(URL_PATTERN, nodeInfo));
    }

    private static FileInfo mockFileInfo(String name) {
        FileInfo nodeInfo = mock(FileInfo.class);
        when(nodeInfo.getNodeRef()).thenReturn(new NodeRef(NODE_REF));
        when(nodeInfo.getName()).thenReturn(name);
        return nodeInfo;
    }
}
