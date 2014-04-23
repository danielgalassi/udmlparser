package obiee.udmlparser.metadataExtract;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import obiee.udmlparser.parser.UDMLParser;
import obiee.udmlparser.utils.XMLUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

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

	/** one or more files containing OBIEE UDML code.*/
	private static Vector <String>	udmlFiles = null;
	/** one or more files containing parsed metadata in XML format.*/
	private static Vector <String>	xudmlFiles = null;
	/** one or more XSL files to transform the XML (parsed) results.*/
	private static Vector <String>	stylesheets = null;
	/** one or more files resulting from applying XSL transformations. Most common formats: CSV, HTML, XML. */
	private static Vector <String>	target = null;
	/** DOM reference to the file storing a job set for batch processing.*/
	private static Document			batchConfig = null;
	/** stores whether the BusMatrix bundled app has been invoked or not.*/
	private static boolean			isBusMatrixInvoked = false;
	/** is the reference to XSL stylesheets for bundled applications.*/
	private static Vector<InputStream> bundledStylesheets = null;

	/**
	 * Indicates whether the Bus Matrix generation app has been invoked
	 * @return true if the BusMatrix app has been invoked, false otherwise.
	 */
	public static boolean isBusMatrixInvoked () {
		return isBusMatrixInvoked;
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

	/**
	 * Method processing XML file containing batch parameters
	 * @param batchFileLocation path to the file containing batch job sets
	 */
	private static void batch(String batchFileLocation) {
		File batch;
		int batchConfigEntries;

		batch = new File(batchFileLocation);

		if (!batch.exists()) {
			return;
		}

		batchConfig = XMLUtils.loadDocument(batch);
		batchConfigEntries = batchConfig.getElementsByTagName("jobdetails").getLength();

		//loading batch arguments
		udmlFiles = new Vector<String>();
		xudmlFiles = new Vector<String>();
		stylesheets = new Vector<String>();
		target = new Vector<String>();

		for (int s=0; s<batchConfigEntries; s++) {

			if (batchConfig.getElementsByTagName("udml").item(s).hasChildNodes()) {
				udmlFiles.add(batchConfig.getElementsByTagName("udml").item(s).getFirstChild().getNodeValue());
			}
			else {
				udmlFiles.add("");
			}

			if (batchConfig.getElementsByTagName("rpdxml").item(s).hasChildNodes()) {
				xudmlFiles.add(batchConfig.getElementsByTagName("rpdxml").item(s).getFirstChild().getNodeValue());
			}
			else {
				xudmlFiles.add("");
			}

			if (batchConfig.getElementsByTagName("udmlxsl").item(s).hasChildNodes()) {
				stylesheets.add(batchConfig.getElementsByTagName("udmlxsl").item(s).getFirstChild().getNodeValue());
			}
			else {
				stylesheets.add("");
			}

			if (batchConfig.getElementsByTagName("udmltgt").item(s).hasChildNodes()) {
				target.add(batchConfig.getElementsByTagName("udmltgt").item(s).getFirstChild().getNodeValue());
			}
			else {
				target.add("");
			}
		}
		//loading batch arguments --end
		batch = null;
	}

	/**
	 * Method processing command line arguments
	 * @param args command line arguments
	 */
	private static void processArgs(String[] args) {
		udmlFiles = new Vector<String>();
		xudmlFiles = new Vector<String>();
		stylesheets = new Vector<String>();
		target = new Vector<String>();

		for (String arg : args) {
			if (arg.startsWith("-udml=")) {
				udmlFiles.add(arg.replaceFirst("-udml=", ""));
			}

			if (arg.startsWith("-rpdxml=")) {
				xudmlFiles.add(arg.replaceFirst("-rpdxml=",""));
			}

			if (arg.startsWith("-udmlxsl=")) {
				stylesheets.add(arg.replaceFirst("-udmlxsl=",""));
			}

			if (arg.startsWith("-udmltgt=")) {
				target.add(arg.replaceFirst("-udmltgt=",""));
			}

			if (arg.equals("-cmd=busmatrix")) {
				isBusMatrixInvoked = true;
				MetadataExtract me = new MetadataExtract();
				bundledStylesheets = new Vector<InputStream>();
				bundledStylesheets.add(me.getInternalResource("obiee/udmlparser/bundledApps/BusMatrix.xsl"));
				bundledStylesheets.add(me.getInternalResource("obiee/udmlparser/bundledApps/Output.xsl"));
			}
		}
	}

	/**
	 * Displays available help
	 *
	 */
	private static void displayHelp() {
		System.out.println("UDML Parser utility");
		System.out.println();
		System.out.println("Application usage:");

		//RPD extract parameters
		System.out.println("Repository extract parameters:");
		System.out.println("-udml=\t\tRepository UDML file");
		System.out.println("-rpdxml=\tFull XML extract");
		System.out.println("-udmlxsl=\tStylesheetfile for optional transformation");
		System.out.println("-udmltgt=\tXML output file from stylesheet transformation");
		System.out.println();
		System.out.println("Batch mode available:");
		System.out.println("-batch=\t\tConfiguration file\n");
		System.out.println("UNIX path form: /dir1/../dirN/file");
		System.out.println("WIN path form: drive\\dir1\\..\\dirN\\file");
		System.out.println();
		System.out.println("To parse UDML code and produce bus matrices:");
		System.out.println("java -jar udmlparser###.jar -udml=path_UDML_file -cmd=busmatrix -udmltgt=path_resulting_HTML_file");
	}

	public static void buildBusMatrix(int index) {
		//picks up the XML file generated by the parser and applies BusMatrix.xsl to it
		XMLUtils.applyStylesheet(xudmlFiles.get(index), bundledStylesheets.get(0), "temp.xml");
		//creates the HTML page presenting results
		logger.info("Generating Bus Matrix document...");
		XMLUtils.applyStylesheet("temp.xml", bundledStylesheets.get(1), target.get(index));

		File temp = new File ("temp.xml");
		logger.info("Cleaning up temporary file {}", temp.getAbsolutePath());
		temp.deleteOnExit();
	}

	/**
	 * Controls the metadata extraction process flow
	 * @param args repository extract process parameters
	 */
	public static void main(String[] args) {

		int paramSize = args.length;
		String firstArg = "";
		
		if (paramSize > 0)
			firstArg = args[0];

		//help requests or missing arguments HERE
		if (paramSize < 1 || firstArg.startsWith("-h") || firstArg.startsWith("-?") || firstArg.startsWith("-help")) {
			displayHelp();
			return;
		}


		//command line arguments HERE
		if (paramSize >= 2) {
			processArgs(args);
		}

		//batch requests HERE
		if (paramSize == 1 && firstArg.startsWith("-b=")) {
			batch(firstArg.replaceFirst("-b=", ""));
			if (batchConfig == null) {
				logger.fatal("Batch file not found");
				return;
			}
		}

		//REPOSITORY METADATA EXTRACTION
		for (int b = 0; b<udmlFiles.size(); b++) {

			//setting a default output file
			//If the Bus Matrix app is invoked and the output filename is not specified, a default file is used.
			if (xudmlFiles.isEmpty() && isBusMatrixInvoked) {
				xudmlFiles.add(System.currentTimeMillis() + ".xml");
			}

			//Parameters check...
			//And the UDML file is parsed
			if (!udmlFiles.isEmpty() && !xudmlFiles.isEmpty() && !udmlFiles.get(b).equals("") && !xudmlFiles.get(b).equals("")) {
				new UDMLParser(udmlFiles.get(b), xudmlFiles.get(b));
			}

			//Custom XML
			if (!stylesheets.isEmpty() && !target.isEmpty() && !stylesheets.get(b).equals("") && !target.get(b).equals("") && !isBusMatrixInvoked) {
				XMLUtils.applyStylesheet(xudmlFiles.get(b), stylesheets.get(b), target.get(b));
			}

			if (isBusMatrixInvoked) {
				buildBusMatrix(b);
			}

		}
		//REPOSITORY METADATA EXTRACTION (END)
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
