
package fr.becpg.web.authentication.config;

import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;


public class IdentityServiceElementReader implements ConfigElementReader {

	public ConfigElement parse(Element elem) {
		ConfigElement configElement = null;
		if (elem != null) {
			configElement = IdentityServiceElement.newInstance(elem);
		}
		return configElement;
	}

}
