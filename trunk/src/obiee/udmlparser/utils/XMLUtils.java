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

import org.w3c.dom.Document;

/**
 * XML Utilities class
 * @author danielgalassi@gmail.com
 *
 */
public class XMLUtils {

	public static void publishException(Exception errMsg){
		System.out.println("Error: " + errMsg.getClass() + "\tDescription: " + errMsg.getMessage());
	}

	/**
	 * Create an empty DOM document
	 * @return DOM document
	 */
	public static Document createDOMDocument() {
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.newDocument();
		} catch (Exception e) {
			e.printStackTrace();
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
		} catch(Exception e) {
			publishException(e);
		}

		try {
			doc = builder.parse(xml);
		} catch(Exception e) {
			publishException(e);
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
			System.out.println(e);
		} catch (TransformerException e) {
			System.out.println(e);
		}
	}

	/**
	 * Transform an XML file using XSL
	 * @param xmlFile
	 * @param xslFile
	 * @param resultFile
	 */
	public static void applyStylesheet(String xmlFile,
			String xslFile,
			String resultFile){
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
		} catch (TransformerConfigurationException tcE) {
			System.out.println("3");
			publishException(tcE);
		}
		try {
			transformer.transform(xmlSource, result);
		} catch (TransformerException tE) {
			System.out.println("4");
			publishException(tE);
		}
	}

	/**
	 * Transform an XML file using an XSL file stored within the jar file
	 * @param strXMLFile
	 * @param inputsXSLFile
	 * @param strRESFile
	 */
	public static void applyStylesheet(String strXMLFile,
			InputStream inputsXSLFile,
			String strRESFile){
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
			System.out.println("3");
			publishException(transformerConfigException);
		}
		try {
			trans.transform(xmlSource, result);
		} catch (TransformerException transformerException) {
			System.out.println("4");
			publishException(transformerException);
		}
	}
}
