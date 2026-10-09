package fr.becpg.repo.web.scripts.statistics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;

/**
 * Unit tests of the filters and the creation date range {@link GetStatisticsWebScript} reads from
 * the request.
 */
public class GetStatisticsWebScriptTest {

	private static final String TEMPLATE_FILTER = "template=workspace://SpacesStore/8529f58b-2615-4d69-a9f5-8b26153d6902";

	private static final String USERNAME_FILTER = "username=jalal.moumtez@becpg.fr";

	private static final String FROM_DATE = "2026-09-01T00:00:00.000Z";

	private static final String TO_DATE = "2026-09-30T23:59:59.999Z";

	private static final String FILTER = "filter";

	private static final String FROM_DATE_PARAM = "fromDate";

	private static final String TO_DATE_PARAM = "toDate";

	private final BeCPGAuditService beCPGAuditService = mock(BeCPGAuditService.class);

	private final WebScriptRequest req = mock(WebScriptRequest.class);

	private final WebScriptResponse res = mock(WebScriptResponse.class);

	private final GetStatisticsWebScript webScript = new GetStatisticsWebScript();

	@Before
	public void setUp() throws IOException {
		Match match = mock(Match.class);
		when(match.getTemplateVars()).thenReturn(Map.of("type", "export_search"));
		when(req.getServiceMatch()).thenReturn(match);
		when(res.getWriter()).thenReturn(new StringWriter());
		when(beCPGAuditService.listAuditPage(any(), any())).thenReturn(AuditPage.empty());
		webScript.setBeCPGAuditService(beCPGAuditService);
	}

	@Test
	public void combinesRepeatedFilters() throws IOException {
		when(req.getParameterValues(FILTER)).thenReturn(new String[] { TEMPLATE_FILTER, USERNAME_FILTER });

		AuditQuery query = executeQuery();

		Assert.assertEquals(TEMPLATE_FILTER, query.getFilter());
		Assert.assertEquals(List.of(USERNAME_FILTER), query.getInMemoryFilters());
	}

	@Test
	public void readsCreationDateRange() throws IOException {
		when(req.getParameter(FROM_DATE_PARAM)).thenReturn(FROM_DATE);
		when(req.getParameter(TO_DATE_PARAM)).thenReturn(TO_DATE);

		AuditQuery query = executeQuery();

		Assert.assertEquals(ISO8601DateFormat.parse(FROM_DATE), query.getFromTime());
		Assert.assertEquals(ISO8601DateFormat.parse(TO_DATE), query.getToTime());
	}

	@Test
	public void readsOffsetDecodedAsSpace() throws IOException {
		when(req.getParameter(FROM_DATE_PARAM)).thenReturn("2026-09-01T02:00:00.000 02:00");

		AuditQuery query = executeQuery();

		Assert.assertEquals(ISO8601DateFormat.parse(FROM_DATE), query.getFromTime());
	}

	@Test
	public void leavesRangeOpenWithoutToDate() throws IOException {
		when(req.getParameter(FROM_DATE_PARAM)).thenReturn(FROM_DATE);

		AuditQuery query = executeQuery();

		Assert.assertEquals(ISO8601DateFormat.parse(FROM_DATE), query.getFromTime());
		Assert.assertTrue(query.getToTime().after(ISO8601DateFormat.parse(TO_DATE)));
	}

	@Test
	public void leavesRangeOpenWithoutFromDate() throws IOException {
		when(req.getParameter(TO_DATE_PARAM)).thenReturn(TO_DATE);

		AuditQuery query = executeQuery();

		Assert.assertEquals(0L, query.getFromTime().getTime());
		Assert.assertEquals(ISO8601DateFormat.parse(TO_DATE), query.getToTime());
	}

	@Test
	public void appliesNoRangeWithoutDates() throws IOException {
		AuditQuery query = executeQuery();

		Assert.assertNull(query.getFromTime());
		Assert.assertNull(query.getToTime());
	}

	@Test
	public void readsDateOnlyToDateAsWholeDay() throws IOException {
		when(req.getParameter(TO_DATE_PARAM)).thenReturn("2026-09-30");

		AuditQuery query = executeQuery();

		Date endOfDay = new Date(ISO8601DateFormat.parse("2026-10-01").getTime() - 1);
		Assert.assertEquals(endOfDay, query.getToTime());
	}

	@Test
	public void rejectsUnsupportedFilter() throws IOException {
		when(beCPGAuditService.listAuditPage(any(), any())).thenThrow(new BeCPGAuditException("Unknown audit filter key: unknown"));

		assertBadRequest();
	}

	@Test
	public void rejectsMalformedMaxResults() throws IOException {
		when(req.getParameter("maxResults")).thenReturn("many");

		assertBadRequest();
	}

	@Test
	public void rejectsMalformedDate() throws IOException {
		when(req.getParameter(FROM_DATE_PARAM)).thenReturn("yesterday");

		assertBadRequest();
	}

	@Test
	public void rejectsReversedRange() throws IOException {
		when(req.getParameter(FROM_DATE_PARAM)).thenReturn(TO_DATE);
		when(req.getParameter(TO_DATE_PARAM)).thenReturn(FROM_DATE);

		assertBadRequest();
	}

	private AuditQuery executeQuery() throws IOException {
		webScript.execute(req, res);
		ArgumentCaptor<AuditQuery> captor = ArgumentCaptor.forClass(AuditQuery.class);
		verify(beCPGAuditService).listAuditPage(eq(AuditType.EXPORT_SEARCH), captor.capture());
		return captor.getValue();
	}

	private void assertBadRequest() throws IOException {
		try {
			webScript.execute(req, res);
			Assert.fail("A bad request was expected");
		} catch (WebScriptException e) {
			Assert.assertEquals(Status.STATUS_BAD_REQUEST, e.getStatus());
		}
	}
}
