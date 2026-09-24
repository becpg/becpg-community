package fr.becpg.repo.regulatory.becpg.regulatory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Unit tests of the relay of the compliance view.
 */
public class RegulatoryComplianceViewServiceTest {

	private static final NodeRef PRODUCT = new NodeRef("workspace://SpacesStore/0e3b5e0e-6b4a-4c79-9d53-b2d2f7d6a1c4");

	@Test
	public void viewIsRelayedWithTheRefreshFlag() throws Exception {
		BecpgRegulatoryClient client = mock(BecpgRegulatoryClient.class);
		JSONObject recipe = new JSONObject();
		when(client.fetchRecipe(PRODUCT)).thenReturn(recipe);
		when(client.checkView(recipe, true)).thenReturn("{\"markets\":[]}");

		assertEquals("{\"markets\":[]}", new RegulatoryComplianceViewService(client).fetchView(PRODUCT, true));
	}
}
