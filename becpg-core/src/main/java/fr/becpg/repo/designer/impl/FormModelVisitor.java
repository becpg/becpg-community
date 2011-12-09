package fr.becpg.repo.designer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import fr.becpg.common.dom.DOMUtils;
import fr.becpg.repo.designer.data.FormControl;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class FormModelVisitor {

	
	/**
	 * Build a list of controls
	 * @param is
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public List<FormControl> visitControls(InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError{
		List<FormControl> ret = new ArrayList<FormControl>();
		
		Document doc = DOMUtils.parse(is);
		
		NodeList list = doc.getElementsByTagName("control");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			FormControl formControl = new FormControl();
			formControl.setId(elem.getAttribute("id"));
			formControl.setDescription(DOMUtils.getElementText(elem, "description"));
			ret.add(formControl);
		}
		
		
		return ret;
	}
	

}
