/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImporterException;

/**
 * Unit tests of the association targets of an imported line (#35654): they are all found before the
 * line writes anything, so that a missing or ambiguous target fails the line alone.
 */
public class AbstractImportVisitorTest {

	private static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	private static final QName ASSOC = QName.createQName(BECPG_URI, "sdclLca");
	private static final QName TARGET_TYPE = QName.createQName(BECPG_URI, "lca");
	private static final QName NAME = QName.createQName(BECPG_URI, "name");
	private static final String TARGET_CODE = "CLIMATE_CHANGE";
	private static final NodeRef TARGET = new NodeRef("workspace://SpacesStore/target");

	private AbstractImportVisitor visitor;

	private AssociationService associationService;

	private AssociationDefinition assocDef;

	private ImportContext importContext;

	@Before
	public void setUp() {
		visitor = spy(new ImportNodeVisitor());
		associationService = mock(AssociationService.class);
		visitor.setAssociationService(associationService);

		ClassDefinition targetClass = mock(ClassDefinition.class);
		when(targetClass.getName()).thenReturn(TARGET_TYPE);
		assocDef = mock(AssociationDefinition.class);
		when(assocDef.getName()).thenReturn(ASSOC);
		when(assocDef.getTargetClass()).thenReturn(targetClass);

		PropertyDefinition propDef = mock(PropertyDefinition.class);
		when(propDef.getName()).thenReturn(NAME);

		List<AbstractAttributeMapping> columns = new ArrayList<>();
		columns.add(new AttributeMapping("name", propDef));
		columns.add(new AttributeMapping("lca", assocDef));
		importContext = new ImportContext();
		importContext.setColumns(columns);
	}

	@Test
	public void findsTheTargetsOfEachAssociationColumn() throws ImporterException {
		doReturn(List.of(TARGET)).when(visitor).findTargetNodesByValue(any(), anyBoolean(), any(), anyString(), any(), any());

		Map<QName, List<NodeRef>> targets = visitor.findAssociationTargets(importContext, Arrays.asList("Coefficient", TARGET_CODE));

		assertEquals(Map.of(ASSOC, List.of(TARGET)), targets);
	}

	@Test
	public void emptyValueClearsTheAssociation() throws ImporterException {
		Map<QName, List<NodeRef>> targets = visitor.findAssociationTargets(importContext, Arrays.asList("Coefficient", ""));

		assertTrue(targets.get(ASSOC).isEmpty());
		verify(visitor, never()).findTargetNodesByValue(any(), anyBoolean(), any(), anyString(), any(), any());
	}

	@Test
	public void missingMandatoryValueIsRejected() {
		when(assocDef.isTargetMandatory()).thenReturn(true);

		assertThrows(ImporterException.class, () -> visitor.findAssociationTargets(importContext, Arrays.asList("Coefficient", null)));
	}

	@Test
	public void missingOptionalValueLeavesTheAssociationUnchanged() throws ImporterException {
		Map<QName, List<NodeRef>> targets = visitor.findAssociationTargets(importContext, Arrays.asList("Coefficient", null));

		assertTrue(targets.isEmpty());
	}

	@Test
	public void targetNotFoundIsRejected() throws ImporterException {
		doReturn(List.of()).when(visitor).findTargetNodesByValue(any(), anyBoolean(), any(), anyString(), any(), any());

		List<String> values = Arrays.asList("Coefficient", TARGET_CODE);

		assertThrows(ImporterException.class, () -> visitor.findAssociationTargets(importContext, values));
	}

	@Test
	public void updatesEachAssociationWithItsTargets() {
		NodeRef nodeRef = new NodeRef("workspace://SpacesStore/imported");
		Map<QName, List<NodeRef>> targets = new LinkedHashMap<>();
		targets.put(ASSOC, List.of(TARGET));

		visitor.updateAssociations(nodeRef, targets);

		verify(associationService).update(nodeRef, ASSOC, List.of(TARGET));
	}
}
