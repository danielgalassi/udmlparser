package obiee.udmlparser.metadataExtract;

import java.io.File;
import java.io.InputStream;

import obiee.udmlparser.cli.Request;
import obiee.udmlparser.cli.RequestProcessor;
import obiee.udmlparser.parser.UDMLParser;
import obiee.udmlparser.utils.XMLUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The semantic layer used by Oracle Business Intelligence 11g (and 10g before
 * that) can be exported to a proprietary declarative language called UDML.
 * UDML statements, while providing a wealth of information, cannot be easily
 * parsed by ETL tools or other metadata management applications.
 * This tool, the MetadataExtract application, parses UDML statements into
 * a more readable format. The output is then saved into an XML file.
 * 
 * Note: in 11g repository metadata XML exports are fully supported (XUDML.)
 * @author danielgalassi@gmail.com
 *
 */
public class MetadataExtract {

	private static final Logger logger = LogManager.getLogger(MetadataExtract.class.getName());

	/** command line arguments processed to make execution flow easier */
	private static Request request = null;

	/**
	 * Indicates whether the Bus Matrix generation app has been invoked
	 * @return true if the BusMatrix app has been invoked, false otherwise.
	 */
	public static boolean isBusMatrixInvoked () {
		return request.isBusMatrixInvoked();
	}

	/**
	 * Loads a resource bundled in the jar file. Used for apps-related files.
	 * @param resource the relative file path (within the jar file)
	 * @return a reference to the XSL file used to transform XML files
	 * @see java.io.InputStream
	 */
	private InputStream getInternalResource(String resource) {
		InputStream bundledResource = null;
		try {
			bundledResource = getClass().getClassLoader().getResourceAsStream(resource);
		} catch (Exception e) {
			logger.warn("Stylesheet {} could not be loaded.", resource);
		}
		return bundledResource;
	}

	public static void buildBusMatrix() {
		MetadataExtract me = new MetadataExtract();
		InputStream busMatrixLogic = me.getInternalResource("obiee/udmlparser/bundledApps/BusMatrix.xsl");
		InputStream htmlOutput = me.getInternalResource("obiee/udmlparser/bundledApps/Output.xsl");

		logger.info("Generating Bus Matrix document...");

		XMLUtils.applyStylesheet(request.getArg("rpdxml"), busMatrixLogic, "temp.xml");
		XMLUtils.applyStylesheet("temp.xml", htmlOutput, request.getArg("target"));

		File temp = new File ("temp.xml");
		logger.info("Cleaning up temporary file {}", temp.getAbsolutePath());
		temp.deleteOnExit();
	}

	/**
	 * Controls the metadata extraction process flow
	 * @param args repository extract process parameters
	 */
	public static void main(String[] args) {

		try {
			RequestProcessor processor = new RequestProcessor(args);
			request = processor.getRequest();
		} catch (Exception e) {
			logger.fatal("{} thrown while processing command line arguments ({})", e.getClass().getCanonicalName(), e.getMessage());
			logger.fatal("Exiting...");
			return;
		}

		//UDML file is parsed
		new UDMLParser(request.getArg("udml"), request.getArg("rpdxml"));

		//Custom transformation
		if (request.isTransformationInvoked()) {
			XMLUtils.applyStylesheet(request.getArg("rpdxml"), request.getArg("stylesheet"), request.getArg("target"));
		}

		//transforms the RPD XML file into a bus matrix HTML page
		if (request.isBusMatrixInvoked()) {
			buildBusMatrix();
		}
	}
}
/*
 * udml -> rpdxml.
 * rpdxml + udmlxsl = udmltgt
 * rpdxml1 (=udmltgt) + udmlxsl1 = udmltgt1 (HTML)

java -jar udmlparser.jar -udml=p6only.udml -cmd=busmatrix -udmltgt=ShowMeTheMatrix.html 

 -udml=sampleCases/st.udml 
 -rpdxml=sampleCases/RPD.XML 
 -cmd=busmatrix 
 -udmltgt=sampleCases/OBIEE.html
 */
