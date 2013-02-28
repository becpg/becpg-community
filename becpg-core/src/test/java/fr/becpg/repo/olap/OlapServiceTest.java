package fr.becpg.repo.olap;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.test.RepoBaseTestCase;


public class OlapServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(OlapServiceTest.class);

	@Resource
	private OlapService olapService;

	@Test
	public void testOlapService() {
		try {
			List<OlapChart> charts = olapService.retrieveOlapCharts();
			for (Iterator<OlapChart> iterator = charts.iterator(); iterator.hasNext();) {
				OlapChart olapChart = (OlapChart) iterator.next();
				logger.info(olapChart.toJSONObject().toString());
				OlapChartData olapChartData = olapService.retrieveChartData(olapChart.getQueryId());
				assertNotNull(olapChartData);
				logger.info(olapChartData.toJSONObject().toString());
			}
		} catch (Exception e) {
			logger.warn("Maybe no running saiku !", e);
		}

	}

}
