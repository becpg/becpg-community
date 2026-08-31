/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.web.scripts.remote;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.web.scripts.remote.AbstractEntityWebScript;
import net.sf.acegisecurity.AccessDeniedException;

/**
 * Tells a refused permission apart from a genuine internal error, whichever of the two unrelated
 * {@code AccessDeniedException} classes was raised and however deep the export wrapped it. An
 * export that mistakes one for the other answers 500 where it owes a 403.
 *
 * @author matthieu
 */
public class AbstractEntityWebScriptTest {

	/** Opens the analysis to the test, the base class keeping it to its own subclasses. */
	private static class ExportUnderTest extends AbstractEntityWebScript {

		@Override
		public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
			// nothing to execute, only the exception analysis is under test
		}

		boolean refuses(Throwable t) {
			return isAccessDenied(t);
		}
	}

	private final ExportUnderTest export = new ExportUnderTest();

	@Test
	public void detectsTheAcegiRefusal() {
		Assert.assertTrue(export.refuses(new AccessDeniedException("Access refused")));
	}

	@Test
	public void detectsTheRepositoryRefusal() {
		Assert.assertTrue(export.refuses(new org.alfresco.repo.security.permissions.AccessDeniedException("Access refused")));
	}

	@Test
	public void detectsARefusalTheExportWrapped() {
		BeCPGException wrapped = new BeCPGException("Cannot export entity",
				new org.alfresco.repo.security.permissions.AccessDeniedException("Access refused"));

		Assert.assertTrue(export.refuses(wrapped));
	}

	@Test
	public void leavesAnInternalErrorAlone() {
		Assert.assertFalse(export.refuses(new BeCPGException("Cannot export entity", new IllegalStateException("boom"))));
	}
}
