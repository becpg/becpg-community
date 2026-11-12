package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Unit tests for {@link XMLDataSourceWriter} and {@link XMLTextHelper}.
 */
public class XMLDataSourceWriterTest {

	/** Label copied from Teams: Word optional hyphen (U+001F) and non breaking hyphen (U+001E) */
	private static final String PASTED_LABEL = "1-(2,3,4,7,8,8a\u001EHexahy\u001Fdro)éthan\u001E1-one";

	private static final String CLEAN_LABEL = "1-(2,3,4,7,8,8aHexahydro)éthan1-one";

	@Test
	public void dropsControlCharactersFromTextAttributesAndCData() throws Exception {
		Element root = DocumentHelper.createElement("entity");
		root.addAttribute("name", PASTED_LABEL);
		root.addElement("text").addText(PASTED_LABEL);
		root.addElement("illManualValue").addCDATA(PASTED_LABEL);

		String xml = write(root);

		assertFalse(xml.contains("&#30;"));
		assertFalse(xml.contains("&#31;"));
		org.w3c.dom.Element parsed = parse(xml);
		assertEquals(CLEAN_LABEL, parsed.getAttribute("name"));
		assertEquals(CLEAN_LABEL, parsed.getElementsByTagName("text").item(0).getTextContent());
		assertEquals(CLEAN_LABEL, parsed.getElementsByTagName("illManualValue").item(0).getTextContent());
	}

	@Test
	public void keepsEscapingAndCharactersOutsideTheBasicPlane() throws Exception {
		String value = "a < b & \"c\" 😀 été\ttab";
		Element root = DocumentHelper.createElement("entity");
		root.addAttribute("name", value);
		root.addElement("text").addText(value);

		org.w3c.dom.Element parsed = parse(write(root));

		assertEquals(value, parsed.getElementsByTagName("text").item(0).getTextContent());
		assertEquals("a < b & \"c\" 😀 été tab", parsed.getAttribute("name"));
	}

	@Test
	public void writesValidUtf8ThroughAStream() throws Exception {
		Element root = DocumentHelper.createElement("entity");
		root.addElement("text").addCDATA(PASTED_LABEL);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLDataSourceWriter writer = new XMLDataSourceWriter(out);
		writer.write(root);
		writer.flush();

		assertEquals(CLEAN_LABEL, parse(out.toString(StandardCharsets.UTF_8)).getTextContent());
	}

	@Test
	public void returnsTheSameStringWhenNothingIsToBeDropped() {
		String value = "AQUA/WATER/EAU - GLYCERIN 😀";
		assertSame(value, XMLTextHelper.writeCData(value));
	}

	@Test
	public void dropsLoneSurrogatesAndNonCharacters() {
		assertEquals("ab", XMLTextHelper.writeCData("a\uD83Db￾"));
		assertEquals("", XMLTextHelper.writeCData("\u0000\u0001"));
		assertEquals("", XMLTextHelper.writeCData(null));
	}

	private static String write(Element element) throws IOException {
		StringWriter out = new StringWriter();
		XMLDataSourceWriter writer = new XMLDataSourceWriter(out);
		writer.write(element);
		writer.flush();
		return out.toString();
	}

	private static org.w3c.dom.Element parse(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
	}

	@Test
	public void tellsWhetherATextHoldsForbiddenCharacters() {
		assertTrue(XMLTextHelper.hasInvalidChar(PASTED_LABEL));
		assertFalse(XMLTextHelper.hasInvalidChar(CLEAN_LABEL));
		assertFalse(XMLTextHelper.hasInvalidChar(null));
		assertFalse(XMLTextHelper.hasInvalidChar(""));
	}
}
