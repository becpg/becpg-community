package fr.becpg.repo.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.dom4j.io.XMLWriter;

/**
 * dom4j writer that leaves out the characters XML 1.0 forbids.
 *
 * <p>dom4j writes a control character as a numeric reference (<code>&amp;#31;</code>) or as is in a
 * CDATA section, and both make the document unreadable for the parser on the other side. Such
 * characters come with text pasted from Word or Teams into any field, so they are dropped here, when
 * the whole document is written, rather than at each place a value is added.</p>
 *
 * @author matthieu
 */
public class XMLDataSourceWriter extends XMLWriter {

	/**
	 * <p>Constructor for XMLDataSourceWriter.</p>
	 *
	 * @param out a {@link java.io.OutputStream} object
	 * @throws java.io.UnsupportedEncodingException if any.
	 */
	public XMLDataSourceWriter(OutputStream out) throws UnsupportedEncodingException {
		super(out);
	}

	/**
	 * <p>Constructor for XMLDataSourceWriter.</p>
	 *
	 * @param writer a {@link java.io.Writer} object
	 */
	public XMLDataSourceWriter(Writer writer) {
		super(writer);
	}

	/** {@inheritDoc} */
	@Override
	protected String escapeElementEntities(String text) {
		return super.escapeElementEntities(XMLTextHelper.writeContent(text));
	}

	/** {@inheritDoc} */
	@Override
	protected String escapeAttributeEntities(String text) {
		return super.escapeAttributeEntities(XMLTextHelper.writeAttribute(text));
	}

	/** {@inheritDoc} */
	@Override
	protected void writeCDATA(String text) throws IOException {
		super.writeCDATA(text != null ? XMLTextHelper.writeCData(text) : null);
	}

}
