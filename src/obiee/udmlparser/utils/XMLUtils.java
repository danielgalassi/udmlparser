package obiee.udmlparser.utils;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * XML Utilities class
 * @author danielgalassi@gmail.com
 *
 */
public class XMLUtils {

	private static final Logger logger = LogManager.getLogger(XMLUtils.class.getName());

	/**
	 * Create an empty DOM document
	 * @return DOM document
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
	 * Reify a DOM document from a file
	 * @param xml
	 * @return DOM document
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
			e.printStackTrace();
		}

		return doc;
	}

	/**
	 * Store a DOM document as a file
	 * @param doc
	 * @param file
	 */
	public static void saveDocument(Document doc, String file) {
		Source source = new DOMSource(doc);

		File xml = new File(file);
		Result result = new StreamResult(xml);
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			logger.error("TransformerConfigurationException thrown while saving document");
		} catch (TransformerException e) {
			logger.error("TransformerException thrown while saving document");
		}
	}

	/**
	 * Transform an XML file using XSL
	 * @param xmlFile
	 * @param xslFile
	 * @param resultFile
	 */
	public static void applyStylesheet(String xmlFile, String xslFile, String resultFile){
		File xml = new File(xmlFile);
		File xsl = new File(xslFile);
		File output = new File(resultFile);
		Transformer transformer = null;
		Source xmlSource = new javax.xml.transform.stream.StreamSource(xml);
		Source xsltSource = new javax.xml.transform.stream.StreamSource(xsl);
		Result result = new javax.xml.transform.stream.StreamResult(output);
		TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
		try {
			transformer = transFact.newTransformer(xsltSource);
		} catch (TransformerConfigurationException transformerConfigException) {
			logger.error("TransformerConfigurationException exception thrown while applying stylesheet");
		}
		try {
			transformer.transform(xmlSource, result);
		} catch (TransformerException transformerException) {
			logger.error("TransformerException exception thrown while applying stylesheet");
		}
	}

	/**
	 * Transform an XML file using an XSL file stored within the jar file
	 * @param strXMLFile
	 * @param inputsXSLFile
	 * @param strRESFile
	 */
	public static void applyStylesheet(String strXMLFile, InputStream inputsXSLFile, String strRESFile){
		File fXMLFile = new File(strXMLFile);
		File fResult = new File(strRESFile);
		Source xmlSource = null;
		Source xsltSource = null;
		Transformer trans = null;
		TransformerFactory transFact = null;
		Result result = null;

		xmlSource = new javax.xml.transform.stream.StreamSource(fXMLFile);
		xsltSource = new javax.xml.transform.stream.StreamSource(inputsXSLFile);
		result = new javax.xml.transform.stream.StreamResult(fResult);
		transFact = javax.xml.transform.TransformerFactory.newInstance();
		try {
			trans = transFact.newTransformer(xsltSource);
		} catch (TransformerConfigurationException transformerConfigException) {
			logger.error("TransformerConfigurationException thrown while applying stylesheet");
		}
		try {
			trans.transform(xmlSource, result);
		} catch (TransformerException transformerException) {
			logger.error("TransformerException thrown while applying stylesheet");
		} catch (NullPointerException nullPointerException) {
			logger.error("NullPointerException thrown while applying stylesheet");
		}
	}
}
