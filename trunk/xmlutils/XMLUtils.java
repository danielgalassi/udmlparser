package xmlutils;

import java.io.File;

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

public class XMLUtils {

	public static void publishException(Exception errMsg){
		System.out.println("Error: " + errMsg.getClass() +
				"\tDescription: " + errMsg.getMessage());
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
		}
		return doc;
	}

	/**
	 * Reify a DOM document from a file
	 * @param filename
	 * @return DOM document
	 */
	public static Document File2Document(File filename) {
		DocumentBuilderFactory dBFXML = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBXML = null;
		Document docXML = null;

		try {docBXML = dBFXML.newDocumentBuilder();
		} catch(Exception e) {publishException(e);}

		try {docXML = docBXML.parse(filename);
		} catch(Exception e) {publishException(e);}
		return docXML;
	}

	/**
	 * Store a DOM document as a file
	 * @param doc
	 * @param filename
	 */
	public static void Document2File(Document doc, String filename) {
		try {
			Source source = new DOMSource(doc);

			File XMLFile = new File(filename);
			Result result = new StreamResult(XMLFile);

			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}

	/**
	 * Transform an XML file using XSL
	 * @param strXMLFile
	 * @param strXSLFile
	 * @param strRESFile
	 */
	public static void xsl4Files(String strXMLFile, String strXSLFile, String strRESFile){
		File fXMLFile = new File(strXMLFile);
		File fXSLFile = new File(strXSLFile);
		File fResult = new File(strRESFile);
		Transformer trans = null;
		Source xmlSource = new javax.xml.transform.stream.StreamSource(fXMLFile);
		Source xsltSource = new javax.xml.transform.stream.StreamSource(fXSLFile);
		Result result = new javax.xml.transform.stream.StreamResult(fResult);
		TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
		try {trans = transFact.newTransformer(xsltSource);
		} catch (TransformerConfigurationException tcE) {publishException(tcE);}
		try {trans.transform(xmlSource, result);
		} catch (TransformerException tE) {publishException(tE);}
	}
}
