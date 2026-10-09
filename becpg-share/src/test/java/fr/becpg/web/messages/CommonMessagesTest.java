package fr.becpg.web.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

/**
 * <p>CommonMessagesTest class.</p>
 *
 * Share formats dates with the {@code mmm} mask from {@code months.short}: two months sharing the
 * same abbreviation make the displayed date ambiguous (French "Jui" for both June and July).
 *
 * @author matthieu
 */
public class CommonMessagesTest {

	private static final Path MESSAGES_FOLDER = Paths.get("src/main/assembly/config/alfresco/messages");
	private static final String COMMON_BUNDLES = "common*.properties";
	private static final String MONTHS_SHORT = "months.short";
	private static final String SEPARATOR = ",";
	private static final int MONTH_COUNT = 12;

	@Test
	public void shortMonthNamesAreDistinctInEveryLanguage() throws IOException {
		try (DirectoryStream<Path> bundles = Files.newDirectoryStream(MESSAGES_FOLDER, COMMON_BUNDLES)) {
			for (Path bundle : bundles) {
				List<String> months = readShortMonths(bundle);
				Set<String> distinctMonths = new HashSet<>(months);
				assertEquals(bundle.getFileName() + " " + MONTHS_SHORT, MONTH_COUNT, distinctMonths.size());
			}
		}
	}

	@Test
	public void frenchShortMonthNamesSpellJuneAndJulyInFrench() throws IOException {
		List<String> months = readShortMonths(MESSAGES_FOLDER.resolve("common_fr.properties"));

		assertEquals("Juin", months.get(5));
		assertEquals("Juil", months.get(6));
	}

	private static List<String> readShortMonths(Path bundle) throws IOException {
		Properties messages = new Properties();
		try (InputStream in = Files.newInputStream(bundle)) {
			messages.load(in);
		}
		String shortMonths = messages.getProperty(MONTHS_SHORT);
		assertNotNull(bundle.getFileName() + " has no " + MONTHS_SHORT, shortMonths);
		return Arrays.stream(shortMonths.split(SEPARATOR)).map(String::trim).toList();
	}
}
