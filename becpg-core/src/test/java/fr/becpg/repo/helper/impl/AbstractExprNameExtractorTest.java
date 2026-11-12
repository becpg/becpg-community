package fr.becpg.repo.helper.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.expressions.ExpressionService;

/**
 * Unit tests of the display name of an entity template: its multilingual title wins over
 * the name format, so that each user reads the template in their own language (#24001).
 */
public class AbstractExprNameExtractorTest {

	private static final NodeRef ENTITY = new NodeRef("workspace://SpacesStore/entity");
	private static final String NAME_FORMAT = "{cm:name}";
	private static final String FORMATTED_NAME = "Finished product template";
	private static final String FRENCH_TITLE = "Modèle de produit fini";
	private static final String ENGLISH_TITLE = "Finished product model";

	private final AbstractExprNameExtractor extractor = new AbstractExprNameExtractor() {

		@Override
		public Collection<QName> getMatchingTypes() {
			return Collections.emptyList();
		}

		@Override
		public String extractPropName(QName type, NodeRef nodeRef) {
			return extractEntityName(nodeRef, NAME_FORMAT);
		}

		@Override
		public String extractMetadata(QName type, NodeRef nodeRef) {
			return "";
		}
	};

	private NodeService nodeService;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		ExpressionService expressionService = mock(ExpressionService.class);
		when(expressionService.extractExpr(eq(ENTITY), isNull(), any(String.class))).thenReturn(FORMATTED_NAME);

		extractor.nodeService = nodeService;
		extractor.expressionService = expressionService;
		I18NUtil.setContentLocale(Locale.FRENCH);
	}

	@After
	public void resetLocale() {
		I18NUtil.setContentLocale(null);
	}

	@Test
	public void entityKeepsItsFormattedName() {
		when(nodeService.getProperty(ENTITY, ContentModel.PROP_TITLE)).thenReturn(FRENCH_TITLE);

		assertEquals(FORMATTED_NAME, displayName());
	}

	@Test
	public void templateIsShownUnderItsTitle() {
		givenTemplate();
		when(nodeService.getProperty(ENTITY, ContentModel.PROP_TITLE)).thenReturn(FRENCH_TITLE);

		assertEquals(FRENCH_TITLE, displayName());
	}

	@Test
	public void templateTitleFollowsTheUserLanguage() {
		givenTemplate();
		MLText title = new MLText();
		title.addValue(Locale.FRENCH, FRENCH_TITLE);
		title.addValue(Locale.ENGLISH, ENGLISH_TITLE);
		when(nodeService.getProperty(ENTITY, ContentModel.PROP_TITLE)).thenReturn(title);
		I18NUtil.setContentLocale(Locale.ENGLISH);

		assertEquals(ENGLISH_TITLE, displayName());
	}

	@Test
	public void templateWithoutTitleKeepsItsFormattedName() {
		givenTemplate();

		assertEquals(FORMATTED_NAME, displayName());
	}

	@Test
	public void templateWithBlankTitleKeepsItsFormattedName() {
		givenTemplate();
		when(nodeService.getProperty(ENTITY, ContentModel.PROP_TITLE)).thenReturn("  ");

		assertEquals(FORMATTED_NAME, displayName());
	}

	private void givenTemplate() {
		when(nodeService.hasAspect(ENTITY, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);
	}

	private String displayName() {
		return extractor.extractPropName(BeCPGModel.TYPE_ENTITY_V2, ENTITY);
	}
}
