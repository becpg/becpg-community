package fr.becpg.test.repo.report;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.report.entity.EntityImageInfo;

/**
 * A nutrition facts panel reaches BIRT as an image that exists nowhere in the repository, so the
 * carrier has to tell an in-memory picture apart from a content node, and has to behave inside the
 * set the report data keeps its images in.
 */
public class EntityImageInfoTest {

	private static final String SVG_MIME_TYPE = "image/svg+xml";

	private static final String PANEL_ID = "nutritionFacts_vertical_en_US";

	private static final NodeRef IMAGE_NODE_REF = new NodeRef("workspace://SpacesStore/image");

	@Test
	public void testGeneratedImageCarriesItsBytes() {
		EntityImageInfo panel = new EntityImageInfo(PANEL_ID, svgBytes(), SVG_MIME_TYPE);

		Assert.assertTrue(panel.isInMemory());
		Assert.assertEquals(SVG_MIME_TYPE, panel.getMimeType());
		Assert.assertNull("A generated image points at no node", panel.getImageNodeRef());
	}

	@Test
	public void testNodeBackedImageIsNotInMemory() {
		Assert.assertFalse(new EntityImageInfo(PANEL_ID, IMAGE_NODE_REF).isInMemory());
	}

	@Test
	public void testEmptyContentIsNotAnImage() {
		Assert.assertFalse("Empty bytes must not be streamed as an image", new EntityImageInfo(PANEL_ID, new byte[0], SVG_MIME_TYPE).isInMemory());
	}

	@Test
	public void testTwoPanelsOfTheSameIdCollapseIntoOne() {
		Set<EntityImageInfo> images = new HashSet<>();
		images.add(new EntityImageInfo(PANEL_ID, svgBytes(), SVG_MIME_TYPE));
		images.add(new EntityImageInfo(PANEL_ID, "<svg>other</svg>".getBytes(StandardCharsets.UTF_8), SVG_MIME_TYPE));

		Assert.assertEquals("The report binds an image by its id, so the id alone decides identity", 1, images.size());
	}

	@Test
	public void testPanelsOfDifferentLocalesCoexist() {
		Set<EntityImageInfo> images = new HashSet<>();
		images.add(new EntityImageInfo(PANEL_ID, svgBytes(), SVG_MIME_TYPE));
		images.add(new EntityImageInfo("nutritionFacts_vertical_fr_FR", svgBytes(), SVG_MIME_TYPE));

		Assert.assertEquals(2, images.size());
	}

	private byte[] svgBytes() {
		return "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8);
	}

}
