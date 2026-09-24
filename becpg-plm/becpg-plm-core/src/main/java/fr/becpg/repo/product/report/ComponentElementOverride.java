package fr.becpg.repo.product.report;

import static fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.ATTR_PREFIX;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;

/**
 * Gives the data list item precedence over its component in a report element.
 *
 * The properties of a component (the product of a composition, packaging or process line) are
 * written first into the element of the line, then the properties of the line itself. Properties
 * written as attributes are overwritten, so the line wins. Properties written as child elements
 * (multi-line values in CDATA, such as bcpg:instruction) used to be appended next to the ones of
 * the component, leaving two elements of the same name where a report XPath only reads the first
 * one, that is the component's. This class removes the component elements of every property the
 * line gives a value to, so both kinds of properties follow the same rule.
 *
 * A property is matched on its prefix and its base name, so that its translated elements
 * (instruction_fr, instruction_en) are all taken from the same node: a line translated in fewer
 * languages than its component must not mix both values. An empty element of the line, such as an
 * association without target, overrides nothing. Only property elements are considered, recognised
 * by their prefix attribute: comments and other structural children are left untouched.
 */
public final class ComponentElementOverride {

	private static final String KEY_SEPARATOR = ":";

	private static final String LOCALE_SUFFIX_SEPARATOR = "_";

	private ComponentElementOverride() {
	}

	/**
	 * Removes from the element the component children of the properties the data list item gives
	 * a value to.
	 *
	 * @param nodeElt the element holding both the component and the item properties
	 * @param componentElements the children of the element written for the component, before the
	 *            item properties were loaded
	 */
	public static void removeOverriddenComponentElements(Element nodeElt, List<Element> componentElements) {
		if (componentElements.isEmpty()) {
			return;
		}
		Set<String> itemPropertyKeys = extractItemPropertyKeys(nodeElt, componentElements);
		for (Element componentElement : componentElements) {
			if (isPropertyElement(componentElement) && itemPropertyKeys.contains(propertyKey(componentElement))) {
				nodeElt.remove(componentElement);
			}
		}
	}

	private static Set<String> extractItemPropertyKeys(Element nodeElt, List<Element> componentElements) {
		Set<Element> componentElementSet = Collections.newSetFromMap(new IdentityHashMap<>());
		componentElementSet.addAll(componentElements);

		Set<String> itemPropertyKeys = new HashSet<>();
		for (Element child : nodeElt.elements()) {
			if (!componentElementSet.contains(child) && isPropertyElement(child) && hasValue(child)) {
				itemPropertyKeys.add(propertyKey(child));
			}
		}
		return itemPropertyKeys;
	}

	private static boolean isPropertyElement(Element element) {
		return element.attribute(ATTR_PREFIX) != null;
	}

	private static boolean hasValue(Element element) {
		return !element.getText().isBlank() || !element.elements().isEmpty();
	}

	private static String propertyKey(Element element) {
		return String.join(KEY_SEPARATOR, element.attributeValue(ATTR_PREFIX), basePropertyName(element.getName()));
	}

	private static String basePropertyName(String elementName) {
		int suffixIndex = elementName.indexOf(LOCALE_SUFFIX_SEPARATOR);
		return suffixIndex > 0 ? elementName.substring(0, suffixIndex) : elementName;
	}

}
