package fr.becpg.common.dom;

/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Méthodes utilitaires pour extraire des infos d'un DOM.
 *
 * @author tom, matthieu
 *
 */
public class DOMUtils {

	private static final Log logger = LogFactory.getLog(DOMUtils.class);

	public static String getElementText(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list.getLength() == 0) {
			return null;
		}
		Text txtElem = (Text) list.item(0).getFirstChild();
		return txtElem == null ? null : txtElem.getData();
	}

	public static String getElementText(Element root) {
		Text txtElem = (Text) root.getFirstChild();
		if (txtElem != null) {
			return txtElem.getData();
		} else {
			return null;
		}
	}

	public static String[] getTexts(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		String[] ret = new String[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			Text t = (Text) list.item(i).getFirstChild();
			if (t != null) {
				ret[i] = t.getData();
			}
		}
		return ret;
	}

	/**
	 * Renvoie sous la forme d'un tableau la valeur des attributs donnés pour
	 * toutes les occurences d'un élément donnée dans le dom
	 *
	 * <code>
	 *  <toto>
	 *   <titi id="a" val="ba"/>
	 *   <titi id="b" val="bb"/>
	 *  </toto>
	 * </code>
	 *
	 * et getAttributes(&lt;toto&gt;, "titi", { "id", "val" }) renvoie
	 *
	 * { { "a", "ba" } { "b", "bb" } }
	 *
	 * @param root
	 * @param elementName
	 * @param wantedAttributes
	 */
	public static String[][] getAttributes(Element root, String elementName, String[] wantedAttributes) {
		NodeList list = root.getElementsByTagName(elementName);
		String[][] ret = new String[list.getLength()][wantedAttributes.length];
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			for (int j = 0; j < wantedAttributes.length; j++) {
				ret[i][j] = elem.getAttribute(wantedAttributes[j]);
			}
		}
		return ret;
	}

	/**
	 * Renvoie la valeur de l'attribut donné, d'un élément donné qui doit être
	 * unique sous l'élément racine
	 *
	 * @param root
	 * @param elementName
	 * @param attribute
	 */
	public static String getElementAttribute(Element root, String elementName, String attribute) {
		NodeList list = root.getElementsByTagName(elementName);
		return ((Element) list.item(0)).getAttribute(attribute);
	}

	/**
	 * Renvoie une élément qui doit être unique dans le document.
	 *
	 * @param root
	 * @param elementName
	 */
	public static Element getUniqueElement(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		return (Element) list.item(0);
	}

	public static Element getUniqueXPathElement(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (Element) xpa.evaluate(xpath, root, XPathConstants.NODE);
	}

	public static String getUniqueXPathTextElement(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (String) xpa.evaluate(xpath, root, XPathConstants.STRING);
	}

	public static NodeList getXPathNodes(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (NodeList) xpa.evaluate(xpath, root, XPathConstants.NODESET);
	}

	public static Element findElementWithUniqueAttribute(Element root, String elementName, String attribute, String attributeValue) {
		NodeList list = root.getElementsByTagName(elementName);
		for (int i = 0; i < list.getLength(); i++) {
			Element tmp = (Element) list.item(i);
			if (tmp.getAttribute(attribute).equals(attributeValue)) {
				return tmp;
			}
		}
		return null;
	}

	public static Element createElementAndText(Element parent, String elementName, String text) {
		if (text == null) {
			throw new NullPointerException("element '" + elementName + "' with null text.");
		}
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		Text txt = el.getOwnerDocument().createTextNode(text);
		el.appendChild(txt);
		return el;
	}

	public static Element createElement(Element parent, String elementName) {
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		return el;
	}

	public static void serialise(Document doc, OutputStream out) throws TransformerException {
		serialise(doc, out, true);
	}

	public static void serialise(Document doc, OutputStream out, boolean keepXmlDecl) throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer tf = factory.newTransformer();
		tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		if (!keepXmlDecl) {
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}
		tf.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
		Source input = new DOMSource(doc.getDocumentElement());
		Result output = new StreamResult(out);
		tf.transform(input, output);
	}

	public static void logDom(Object logSource, Document doc) throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialise(doc, out);
		logger.info(new String(out.toByteArray()));
	}

	public static Document parse(InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder.parse(is, StandardCharsets.UTF_8.toString());
	}

	public static Document createDoc() throws ParserConfigurationException, FactoryConfigurationError {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder.newDocument();
	}

	public static Element createDoc(String rootNodeName) throws ParserConfigurationException, FactoryConfigurationError {
		Document ret = createDoc();
		Element root = ret.createElement(rootNodeName);
		ret.appendChild(root);
		return root;
	}

}
