package obiee.udmlparser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class handles XML-creating, -storing and -transforming calls 
 * @author danielgalassi@gmail.com
 *
 */
public class XMLUtils {

	private static final Logger logger = LogManager.getLogger(XMLUtils.class.getName());

	/**
	 * Creates an empty DOM document
	 * @return DOM representation of XML data
	 */
	public static Document createDocument() {
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.newDocument();
		} catch (Exception e) {
			logger.error("{} thrown while creating a DOM document", e.getClass().getCanonicalName());
		}
		return doc;
	}

	/**
	 * Creates a DOM document representation of an XML file
	 * @param xml represents the XML file in the filesystem
	 * @return DOM a representation of XML data
	 */
	public static Document loadDocument(File xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(xml);
		} catch(Exception e) {
			logger.error("{} thrown while loading an XML file into a DOM document", e.getClass().getCanonicalName());
		}

		return doc;
	}

	/**
	 * Stores XML document to file
	 * @param doc DOM representation of XML data
	 * @param file represents the resulting XML file in the filesystem
	 */
	public static void saveDocument(Document doc, String file) {
		Source source = new DOMSource(doc);
		File xml = new File(file);
		Result result = new StreamResult(xml);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);
		} catch (Exception e) {
			logger.error("{} thrown while saving document", e.getClass().getCanonicalName());
		}
	}

	/**
	 * Transforms an XML file using an XSL stylesheet
	 * @param xml XML file to be transformed
	 * @param stylesheet XSL file containing transformations
	 * @param output resulting file
	 */
	public static void applyStylesheet(String xml, String stylesheet, String output){
		try {
			InputStream xsl = new FileInputStream(stylesheet);
			applyStylesheet(xml, xsl, output);
		} catch (Exception e) {
			logger.error("{} exception thrown while applying stylesheet", e.getClass().getCanonicalName());
		}
	}

	/**
	 * Transforms an XML file using an XSL stylesheet
	 * @param xml XML file to be transformed
	 * @param stylesheet transformations to be applied
	 * @param output resulting file
	 */
	public static void applyStylesheet(String xml, InputStream stylesheet, String output){
		File xmlFile = new File(xml);
		File resultFile = new File(output);

		Source xmlSource = null;
		Source xsltSource = null;
		Result result = null;

		xmlSource = new javax.xml.transform.stream.StreamSource(xmlFile);
		xsltSource = new javax.xml.transform.stream.StreamSource(stylesheet);
		result = new javax.xml.transform.stream.StreamResult(resultFile);

		TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
		try {
			Transformer transformer = factory.newTransformer(xsltSource);
			transformer.transform(xmlSource, result);
		} catch (Exception e) {
			logger.error("{} thrown while applying stylesheet", e.getClass().getCanonicalName());
		}
	}
}
