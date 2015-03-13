/**
 * 
 */
package org.openntf.domino.utils.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author jgallagher
 * 
 */
public class XMLDocument extends XMLNode {
	private static final long serialVersionUID = -8106159267601656260L;

	private static ThreadLocal<DocumentBuilder> docBuilder = new ThreadLocal<DocumentBuilder>() {
		@Override
		protected DocumentBuilder initialValue() {
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setValidating(false);
			try {
				return fac.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			}

		};
	};

	private static ThreadLocal<Transformer> docTransformer = new ThreadLocal<Transformer>() {
		@Override
		protected Transformer initialValue() {
			return createTransformer(null);
		};
	};

	public XMLDocument() {
	}

	public XMLDocument(final Node node) {
		super(node);
	}

	public XMLDocument(final String xml) throws SAXException, IOException, ParserConfigurationException {
		loadString(xml);
	}

	public XMLNode getDocumentElement() {
		return new XMLNode(((Document) node_).getDocumentElement());
	}

	public void loadURL(final String urlString) throws SAXException, IOException, ParserConfigurationException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		node_ = getBuilder().parse((InputStream) conn.getContent());
	}

	public void loadInputStream(final InputStream is) throws SAXException, IOException, ParserConfigurationException {
		node_ = getBuilder().parse(is);
	}

	public void loadString(final String s) throws SAXException, IOException, ParserConfigurationException {
		loadInputStream(new ByteArrayInputStream(s.getBytes("UTF-8")));
	}

	public static DocumentBuilder getBuilder() throws ParserConfigurationException {
		return docBuilder.get();
	}

	public static Transformer getTransformer() {
		return docTransformer.get();
	}

	public static String escapeXPathValue(final String input) {
		return input.replace("'", "\\'");
	}

	public String readXml() throws IOException {
		try {
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(this.node_);
			getTransformer().transform(source, result);

			return result.getWriter().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		try {
			return readXml();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}