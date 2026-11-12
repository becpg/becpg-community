package fr.becpg.repo.entity.version;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * Unit tests of the target associations {@link VersionExporter#end()} copies onto the version.
 */
public class VersionExporterTest {

	private static final NodeRef ENTITY = new NodeRef("workspace://SpacesStore/entity");

	private static final NodeRef VERSION = new NodeRef("workspace://version2Store/version");

	private static final NodeRef REPORT = new NodeRef("workspace://SpacesStore/report");

	private static final NodeRef ORIGINAL = new NodeRef("workspace://SpacesStore/original");

	private NodeService dbNodeService;

	@Before
	public void setUp() {
		dbNodeService = mock(NodeService.class);
		when(dbNodeService.getTargetAssocs(any(NodeRef.class), any(QNamePattern.class))).thenReturn(List.of());
		when(dbNodeService.getTargetAssocs(ENTITY, RegexQNamePattern.MATCH_ALL))
				.thenReturn(List.of(new AssociationRef(ENTITY, ReportModel.ASSOC_REPORTS, REPORT),
						new AssociationRef(ENTITY, ContentModel.ASSOC_ORIGINAL, ORIGINAL)));
	}

	@Test
	public void ignoredAssocIsNotCopiedOntoTheVersion() {
		createExporter().ignoreAssocs(ReportModel.ASSOC_REPORTS).end();

		verify(dbNodeService, never()).createAssociation(VERSION, REPORT, ReportModel.ASSOC_REPORTS);
	}

	@Test
	public void otherAssocsAreStillCopiedOntoTheVersion() {
		createExporter().ignoreAssocs(ReportModel.ASSOC_REPORTS).end();

		verify(dbNodeService).createAssociation(VERSION, ORIGINAL, ContentModel.ASSOC_ORIGINAL);
	}

	@Test
	public void everyAssocIsCopiedWhenNoneIsIgnored() {
		createExporter().end();

		verify(dbNodeService).createAssociation(VERSION, REPORT, ReportModel.ASSOC_REPORTS);
	}

	private VersionExporter createExporter() {
		return new VersionExporter(ENTITY, VERSION, dbNodeService, mock(EntityDictionaryService.class));
	}
}
