package fr.becpg.common.dom;

/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
 * @version $Id: $Id
 */
public class DOMUtils {

	private static final Log logger = LogFactory.getLog(DOMUtils.class);

	private DOMUtils() {
		//Do Nothing
	}
	
	/**
	 * <p>getElementText.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getElementText(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list.getLength() == 0) {
			return null;
		}
		Text txtElem = (Text) list.item(0).getFirstChild();
		return txtElem == null ? null : txtElem.getData();
	}

	/**
	 * <p>getElementText.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getElementText(Element root) {
		Text txtElem = (Text) root.getFirstChild();
		if (txtElem != null) {
			return txtElem.getData();
		} else {
			return null;
		}
	}

	/**
	 * <p>getTexts.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
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
	 * <pre>
	 * {@code
	 *  <toto>
	 *   <titi id="a" val="ba"/>
	 *   <titi id="b" val="bb"/>
	 *  </toto>
	 *  }
	 * </pre>
	 *
	 *<pre>
	 * et getAttributes(&lt;toto&gt;, "titi", { "id", "val" }) renvoie
	 *
	 * { { "a", "ba" } { "b", "bb" } }
	 * </pre>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @param wantedAttributes an array of {@link java.lang.String} objects.
	 * @return an array of {@link java.lang.String} objects.
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
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @param attribute a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getElementAttribute(Element root, String elementName, String attribute) {
		NodeList list = root.getElementsByTagName(elementName);
		return ((Element) list.item(0)).getAttribute(attribute);
	}

	/**
	 * Renvoie une élément qui doit être unique dans le document.
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 */
	public static Element getUniqueElement(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		return (Element) list.item(0);
	}

	/**
	 * <p>getUniqueXPathElement.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param xpath a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 * @throws javax.xml.xpath.XPathExpressionException if any.
	 */
	public static Element getUniqueXPathElement(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (Element) xpa.evaluate(xpath, root, XPathConstants.NODE);
	}

	/**
	 * <p>getUniqueXPathTextElement.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param xpath a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws javax.xml.xpath.XPathExpressionException if any.
	 */
	public static String getUniqueXPathTextElement(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (String) xpa.evaluate(xpath, root, XPathConstants.STRING);
	}

	/**
	 * <p>getXPathNodes.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param xpath a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.NodeList} object.
	 * @throws javax.xml.xpath.XPathExpressionException if any.
	 */
	public static NodeList getXPathNodes(Element root, String xpath) throws XPathExpressionException {
		XPath xpa = XPathFactory.newInstance().newXPath();
		return (NodeList) xpa.evaluate(xpath, root, XPathConstants.NODESET);
	}

	/**
	 * <p>findElementWithUniqueAttribute.</p>
	 *
	 * @param root a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @param attribute a {@link java.lang.String} object.
	 * @param attributeValue a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 */
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

	/**
	 * <p>createElementAndText.</p>
	 *
	 * @param parent a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 */
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

	/**
	 * <p>createElement.</p>
	 *
	 * @param parent a {@link org.w3c.dom.Element} object.
	 * @param elementName a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 */
	public static Element createElement(Element parent, String elementName) {
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		return el;
	}

	/**
	 * <p>serialise.</p>
	 *
	 * @param doc a {@link org.w3c.dom.Document} object.
	 * @param out a {@link java.io.OutputStream} object.
	 * @throws javax.xml.transform.TransformerException if any.
	 */
	public static void serialise(Document doc, OutputStream out) throws TransformerException {
		serialise(doc, out, true);
	}

	/**
	 * <p>serialise.</p>
	 *
	 * @param doc a {@link org.w3c.dom.Document} object.
	 * @param out a {@link java.io.OutputStream} object.
	 * @param keepXmlDecl a boolean.
	 * @throws javax.xml.transform.TransformerException if any.
	 */
	public static void serialise(Document doc, OutputStream out, boolean keepXmlDecl) throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
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

	/**
	 * <p>logDom.</p>
	 *
	 * @param doc a {@link org.w3c.dom.Document} object.
	 * @throws javax.xml.transform.TransformerException if any.
	 */
	public static void logDom( Document doc) throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialise(doc, out);
		logger.info(new String(out.toByteArray()));
	}

	/**
	 * <p>parse.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link org.w3c.dom.Document} object.
	 * @throws org.xml.sax.SAXException if any.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws javax.xml.parsers.FactoryConfigurationError if any.
	 */
	public static Document parse(InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		domFactory.setNamespaceAware(true);
		domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder.parse(is, StandardCharsets.UTF_8.toString());
	}

	/**
	 * <p>createDoc.</p>
	 *
	 * @return a {@link org.w3c.dom.Document} object.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws javax.xml.parsers.FactoryConfigurationError if any.
	 */
	public static Document createDoc() throws ParserConfigurationException, FactoryConfigurationError {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * <p>createDoc.</p>
	 *
	 * @param rootNodeName a {@link java.lang.String} object.
	 * @return a {@link org.w3c.dom.Element} object.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws javax.xml.parsers.FactoryConfigurationError if any.
	 */
	public static Element createDoc(String rootNodeName) throws ParserConfigurationException, FactoryConfigurationError {
		Document ret = createDoc();
		Element root = ret.createElement(rootNodeName);
		ret.appendChild(root);
		return root;
	}

}
