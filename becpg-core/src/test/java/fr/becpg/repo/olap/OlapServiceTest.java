package fr.becpg.repo.olap;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.test.RepoBaseTestCase;


public class OlapServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(OlapServiceTest.class);

	/** The repository helper. */
	private OlapService olapService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		olapService = (OlapService) ctx.getBean("olapService");
	}

	public void testOlapService() {
		try {
			List<OlapChart> charts = olapService.retrieveOlapCharts();
			for (Iterator<OlapChart> iterator = charts.iterator(); iterator.hasNext();) {
				OlapChart olapChart = (OlapChart) iterator.next();
				logger.info(olapChart.toJSONObject().toString());
				OlapChartData olapChartData = olapService.retrieveChartData(olapChart.getQueryId());
				Assert.assertNotNull(olapChartData);
				logger.info(olapChartData.toJSONObject().toString());
			}
		} catch (Exception e) {
			logger.warn("Maybe no running saiku !", e);
		}

	}

}
