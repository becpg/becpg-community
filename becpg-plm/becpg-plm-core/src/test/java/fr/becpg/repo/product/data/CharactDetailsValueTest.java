/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * Unit tests of the zero values of {@link CharactDetailsValue}: a zero the component carries is shown, a missing value
 * is left blank.
 *
 * @author matthieu
 */
public class CharactDetailsValueTest {

	private static final NodeRef PRODUCT = new NodeRef("workspace://SpacesStore/product");

	private static final NodeRef COMPONENT = new NodeRef("workspace://SpacesStore/component");

	@Test
	public void leavesAMissingValueBlank() {
		assertNull(detail(0d, false).getValue());
	}

	@Test
	public void showsAZeroTheComponentCarries() {
		assertEquals(Double.valueOf(0d), detail(0d, true).getValue());
	}

	@Test
	public void keepsTheZeroOfAMergedComponent() {
		CharactDetailsValue merged = detail(0d, false);
		merged.add(detail(0d, true));

		assertEquals(Double.valueOf(0d), merged.getValue());
	}

	private static CharactDetailsValue detail(Double value, boolean entered) {
		CharactDetailsValue detail = new CharactDetailsValue(PRODUCT, COMPONENT, null, value, 0, "g/100g");
		detail.setAllowZeroValue(entered);
		return detail;
	}
}
