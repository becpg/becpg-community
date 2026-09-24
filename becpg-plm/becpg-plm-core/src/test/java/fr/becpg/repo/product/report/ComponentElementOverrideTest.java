package fr.becpg.repo.product.report;

import static fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.ATTR_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ComponentElementOverride}.
 */
public class ComponentElementOverrideTest {

	private static final String COMPO_LIST = "compoList";
	private static final String INSTRUCTION = "instruction";
	private static final String INSTRUCTION_FR = "instruction_fr";
	private static final String INSTRUCTION_EN = "instruction_en";
	private static final String PLANTS = "plants";
	private static final String PRODUCT_COMMENTS = "productComments";
	private static final String COMMENTS = "comments";
	private static final String BCPG = "bcpg";
	private static final String CM = "cm";
	private static final String COMPONENT_INSTRUCTION = "Preparation Step 1";
	private static final String LINE_INSTRUCTION = "(25 gr)";

	private Element nodeElt;

	@Before
	public void setUp() {
		nodeElt = DocumentHelper.createElement(COMPO_LIST);
	}

	@Test
	public void lineInstructionReplacesComponentInstruction() {
		List<Element> componentElements = addComponentElements(propertyElement(BCPG, INSTRUCTION, COMPONENT_INSTRUCTION));
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(1, nodeElt.elements(INSTRUCTION).size());
		assertEquals(LINE_INSTRUCTION, nodeElt.element(INSTRUCTION).getText());
	}

	@Test
	public void componentInstructionIsKeptWhenLineHasNone() {
		List<Element> componentElements = addComponentElements(propertyElement(BCPG, INSTRUCTION, COMPONENT_INSTRUCTION));

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(COMPONENT_INSTRUCTION, nodeElt.element(INSTRUCTION).getText());
	}

	@Test
	public void componentPropertiesTheLineDoesNotDefineAreKept() {
		List<Element> componentElements = addComponentElements(propertyElement(BCPG, INSTRUCTION, COMPONENT_INSTRUCTION),
				propertyElement(BCPG, PRODUCT_COMMENTS, COMPONENT_INSTRUCTION));
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(1, nodeElt.elements(PRODUCT_COMMENTS).size());
	}

	@Test
	public void sameNameInAnotherNamespaceIsKept() {
		List<Element> componentElements = addComponentElements(propertyElement(CM, INSTRUCTION, COMPONENT_INSTRUCTION));
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(2, nodeElt.elements(INSTRUCTION).size());
	}

	@Test
	public void elementsWithoutPrefixAreNeverRemoved() {
		List<Element> componentElements = addComponentElements(DocumentHelper.createElement(COMMENTS));
		nodeElt.addElement(COMMENTS);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(2, nodeElt.elements(COMMENTS).size());
	}

	@Test
	public void repeatedLineElementsAreAllKept() {
		List<Element> componentElements = addComponentElements(propertyElement(BCPG, INSTRUCTION, COMPONENT_INSTRUCTION));
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(2, nodeElt.elements(INSTRUCTION).size());
		for (Element instruction : nodeElt.elements(INSTRUCTION)) {
			assertEquals(LINE_INSTRUCTION, instruction.getText());
		}
	}

	@Test
	public void lineTranslationsReplaceEveryComponentTranslation() {
		List<Element> componentElements = addComponentElements(propertyElement(BCPG, INSTRUCTION, COMPONENT_INSTRUCTION),
				propertyElement(BCPG, INSTRUCTION_FR, COMPONENT_INSTRUCTION), propertyElement(BCPG, INSTRUCTION_EN, COMPONENT_INSTRUCTION));
		addPropertyElement(BCPG, INSTRUCTION, LINE_INSTRUCTION);
		addPropertyElement(BCPG, INSTRUCTION_FR, LINE_INSTRUCTION);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertNull(nodeElt.element(INSTRUCTION_EN));
	}

	@Test
	public void emptyLineElementKeepsComponentElement() {
		Element componentPlants = propertyElement(BCPG, PLANTS, COMPONENT_INSTRUCTION);
		List<Element> componentElements = addComponentElements(componentPlants);
		nodeElt.addElement(PLANTS).addAttribute(ATTR_PREFIX, BCPG);

		ComponentElementOverride.removeOverriddenComponentElements(nodeElt, componentElements);

		assertEquals(componentPlants, nodeElt.elements(PLANTS).get(0));
		assertEquals(2, nodeElt.elements(PLANTS).size());
	}

	private List<Element> addComponentElements(Element... elements) {
		List<Element> componentElements = new ArrayList<>();
		for (Element element : elements) {
			nodeElt.add(element);
			componentElements.add(element);
		}
		return componentElements;
	}

	private void addPropertyElement(String prefix, String name, String value) {
		nodeElt.add(propertyElement(prefix, name, value));
	}

	private static Element propertyElement(String prefix, String name, String value) {
		Element element = DocumentHelper.createElement(name);
		element.addAttribute(ATTR_PREFIX, prefix);
		element.addCDATA(value);
		return element;
	}

}
