package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Unit tests of the label of the GS1 place of activity type shown next to a geographic origin.
 */
public class GeoOriginAttributeExtractorPluginTest {

	private static final String GS1_BUNDLE = "alfresco/module/becpg-plm-core/model/gs1Model";

	private static final String GS1_CODES_CSV = "beCPG/databases/gs1/gs1_codes.csv";

	private static final String ACTIVITY_TYPE_LIST = "ProductActivityTypeCode";

	private static final String ACTIVITY_TYPE_MESSAGE_PREFIX = "listconstraint.gs1_productActivityTypeCodes.";

	private static final String CSV_SEPARATOR = ";";

	private static final String[] BUNDLE_SUFFIXES = { "", "_en", "_fr" };

	private Locale previousLocale;

	@Before
	public void setUp() {
		previousLocale = I18NUtil.getLocale();
		I18NUtil.setLocale(Locale.ENGLISH);
	}

	@After
	public void tearDown() {
		I18NUtil.setLocale(previousLocale);
	}

	@Test
	public void testUntranslatedCodeFallsBackToTheCode() {
		assertEquals("UNKNOWN_ACTIVITY", GeoOriginAttributeExtractorPlugin.activityTypeLabel("UNKNOWN_ACTIVITY"));
	}

	@Test
	public void testSlaughterIsTranslatedInEnglish() {
		I18NUtil.registerResourceBundle(GS1_BUNDLE);

		assertEquals("Slaughter", GeoOriginAttributeExtractorPlugin.activityTypeLabel("SLAUGHTER"));
	}

	@Test
	public void testEveryGs1ActivityTypeIsTranslatedInEveryBundle() throws IOException {
		List<String> activityTypeCodes = readActivityTypeCodes();
		assertFalse(activityTypeCodes.isEmpty());

		for (String suffix : BUNDLE_SUFFIXES) {
			Properties bundle = loadBundle(GS1_BUNDLE + suffix + ".properties");
			for (String code : activityTypeCodes) {
				assertTrue(code + " missing from gs1Model" + suffix, bundle.containsKey(ACTIVITY_TYPE_MESSAGE_PREFIX + code));
			}
		}
	}

	private List<String> readActivityTypeCodes() throws IOException {
		List<String> codes = new ArrayList<>();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(GS1_CODES_CSV);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] columns = line.split(CSV_SEPARATOR, -1);
				if ((columns.length > 2) && ACTIVITY_TYPE_LIST.equals(columns[1]) && !columns[2].isBlank()) {
					codes.add(columns[2]);
				}
			}
		}
		return codes;
	}

	private Properties loadBundle(String path) throws IOException {
		Properties bundle = new Properties();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
			bundle.load(in);
		}
		return bundle;
	}
}
