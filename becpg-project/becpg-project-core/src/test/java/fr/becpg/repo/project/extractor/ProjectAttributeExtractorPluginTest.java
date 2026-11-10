package fr.becpg.repo.project.extractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AssociationService;

/**
 * Unit tests of the CSS classes that give a project, or a project template, its custom icon.
 */
public class ProjectAttributeExtractorPluginTest {

	private static final String PROJECT_PREFIX = "pjt";

	private final NodeRef projectNodeRef = new NodeRef("workspace://SpacesStore/project-id");

	private final NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/template-id");

	@Mock
	private NodeService nodeService;

	@Mock
	private NamespaceService namespaceService;

	@Mock
	private AssociationService associationService;

	@InjectMocks
	private ProjectAttributeExtractorPlugin plugin;

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		when(namespaceService.getPrefixes(ProjectModel.PROJECT_URI)).thenReturn(List.of(PROJECT_PREFIX));
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void testProjectCarriesTheClassOfItsTemplate() {
		when(associationService.getTargetAssoc(projectNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF)).thenReturn(templateNodeRef);

		assertEquals("project project-template-id", plugin.extractMetadata(ProjectModel.TYPE_PROJECT, projectNodeRef));
	}

	@Test
	public void testTemplateCarriesItsOwnClass() {
		when(nodeService.hasAspect(templateNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);

		assertEquals("project project-template-id", plugin.extractMetadata(ProjectModel.TYPE_PROJECT, templateNodeRef));
	}

	@Test
	public void testTemplateCreatedFromAnotherTemplateCarriesBothClasses() {
		when(nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);
		when(associationService.getTargetAssoc(projectNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF)).thenReturn(templateNodeRef);

		assertEquals("project project-template-id project-project-id", plugin.extractMetadata(ProjectModel.TYPE_PROJECT, projectNodeRef));
	}

	@Test
	public void testTemplateReferringToItselfCarriesItsClassOnce() {
		when(nodeService.hasAspect(templateNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);
		when(associationService.getTargetAssoc(templateNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF)).thenReturn(templateNodeRef);

		assertEquals("project project-template-id", plugin.extractMetadata(ProjectModel.TYPE_PROJECT, templateNodeRef));
	}

	@Test
	public void testProjectStateIsKeptBeforeTheTemplateClass() {
		when(nodeService.getProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE)).thenReturn("InProgress");
		when(associationService.getTargetAssoc(projectNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF)).thenReturn(templateNodeRef);

		assertEquals("project project-inprogress project-template-id", plugin.extractMetadata(ProjectModel.TYPE_PROJECT, projectNodeRef));
	}
}
