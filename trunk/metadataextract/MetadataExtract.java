package metadataextract;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import org.w3c.dom.Document;

import udmlparser.UDMLParser;
import xmlutils.XMLUtils;

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

	/** one or more files containing OBIEE UDML code.*/
	private static Vector <String>	vsUDMLtxt	= null;
	/** one or more files containing parsed metadata in XML format.*/
	private static Vector <String>	vsUDMLxml	= null;
	/** one or more XSL files to transform the XML (parsed) results.*/
	private static Vector <String>	vsUDMLxsl	= null;
	/** one or more files resulting from applying XSL transformations.
	 * Most common formats: CSV, HTML, XML.
	 */
	private static Vector <String>	vsUDMLtgt	= null;
	/** DOM reference to the file storing a job set for batch processing.*/
	private static Document			dBatch		= null;
	/** stores whether the BusMatrix bundled app has been invoked or not.*/
	private static boolean			isBusMatrixInvoked = false;
	/**	is the reference to the first XSL file for bundled applications.*/
	private static InputStream		insXSL1 = null;
	/** is the reference to the second XSL file for bundled applications.*/
	private static InputStream		insXSL2 = null;

	/**
	 * Indicates whether the Bus Matrix generation app has been invoked
	 * @return true if the BusMatrix app has been invoked, false otherwise.
	 */
	public static boolean isBusMatrixInvoked () {
		return isBusMatrixInvoked;
	}

	/**
	 * Loads a resource bundled in the jar file. Used for apps-related files.
	 * @param rsc the relative file path (within the jar file)
	 * @return a reference to the XSL file used to transform XML files
	 * @see java.io.InputStream
	 */
	private InputStream istrInternalResource(String rsc) {
		InputStream isRsc = null;
		try {
			isRsc = getClass().getClassLoader().getResourceAsStream(rsc);
		} catch (Exception e) {
			System.out.println("istrInternalResource: " + rsc);
			e.printStackTrace();
		}
		return isRsc;
	}

	/**
	 * Method processing XML file containing batch parameters
	 * @param sBatch path to the file containing batch job sets
	 */
	private static void batch(String sBatch) {
		File fBatch;
		int iBatchSize;

		fBatch = new File(sBatch);

		if (!fBatch.exists())
			return;

		dBatch = XMLUtils.File2Document(fBatch);
		iBatchSize = dBatch.getElementsByTagName("jobdetails").getLength();

		//loading batch arguments
		vsUDMLtxt =	new Vector<String>();
		vsUDMLxml =	new Vector<String>();
		vsUDMLxsl =	new Vector<String>();
		vsUDMLtgt =	new Vector<String>();

		for (int s=0; s<iBatchSize; s++) {

			if (dBatch.getElementsByTagName("udml").item(s).hasChildNodes())
				vsUDMLtxt.add(dBatch.getElementsByTagName("udml").item(s).
						getFirstChild().getNodeValue());
			else
				vsUDMLtxt.add("");

			if (dBatch.getElementsByTagName("rpdxml").item(s).hasChildNodes())
				vsUDMLxml.add(dBatch.getElementsByTagName("rpdxml").item(s).
						getFirstChild().getNodeValue());
			else
				vsUDMLxml.add("");

			if (dBatch.getElementsByTagName("udmlxsl").item(s).hasChildNodes())
				vsUDMLxsl.add(dBatch.getElementsByTagName("udmlxsl").item(s).
						getFirstChild().getNodeValue());
			else
				vsUDMLxsl.add("");

			if (dBatch.getElementsByTagName("udmltgt").item(s).hasChildNodes())
				vsUDMLtgt.add(dBatch.getElementsByTagName("udmltgt").item(s).
						getFirstChild().getNodeValue());
			else
				vsUDMLtgt.add("");
		}
		//loading batch arguments --end
		fBatch = null;
	}

	/**
	 * Method processing command line arguments
	 * @param args command line arguments
	 */
	private static void commandLine(String[] args) {
		vsUDMLtxt =	new Vector<String>();
		vsUDMLxml =	new Vector<String>();
		vsUDMLxsl =	new Vector<String>();
		vsUDMLtgt =	new Vector<String>();

		for(int i=0; i<args.length; i++) {
			if (args[i].startsWith("-udml="))
				vsUDMLtxt.add(args[i].replaceFirst("-udml=",""));

			if (args[i].startsWith("-rpdxml="))
				vsUDMLxml.add(args[i].replaceFirst("-rpdxml=",""));

			if (args[i].startsWith("-udmlxsl="))
				vsUDMLxsl.add(args[i].replaceFirst("-udmlxsl=",""));

			if (args[i].startsWith("-udmltgt="))
				vsUDMLtgt.add(args[i].replaceFirst("-udmltgt=",""));

			if (args[i].equals("-cmd=busmatrix")) {
				isBusMatrixInvoked = true;
				MetadataExtract me = new MetadataExtract();
				insXSL1 = me.istrInternalResource("bundledApps/BusMatrix.xsl");
				insXSL2 = me.istrInternalResource("bundledApps/Output.xsl");
			}
		}
	}

	/**
	 * Displays available help
	 *
	 */
	private static void displayHelp() {
		System.out.println("UDML Parser utility\n\n");
		System.out.println("Application usage:");

		//RPD extract parameters
		System.out.println("Repository extract parameters:");
		System.out.println("-udml=\t\tRepository UDML file");
		System.out.println("-rpdxml=\tFull XML extract");
		System.out.println("-udmlxsl=\tXSL file for optional transformation");
		System.out.println("-udmltgt=\tXML file resulting " +
		"from XSL transformation\n");
		System.out.println("Batch mode available:");
		System.out.println("-batch=\t\tConfiguration file\n");
		System.out.println("UNIX path form: /dir1/../dirN/file");
		System.out.println("WIN path form: drive\\dir1\\..\\dirN\\file");
		System.out.println("\nTo parse UDML code and produce bus matrices:");
		System.out.println("java -jar udmlparser###.jar -udml=(path UDML file) " + 
				"-cmd=busmatrix -udmltgt=(path resulting HTML file)");
	}

	/**
	 * Controls the metadata extraction process flow
	 * @param args repository extract process parameters
	 */
	public static void main(String[] args) {

		//batch requests HERE
		if (args.length ==1 && args[0].startsWith("-b=")) {
			batch(args[0].replaceFirst("-b=", ""));
			if (dBatch == null) {
				System.out.println("Batch file not found");
				return;
			}
		}
		else 
			//help requests or missing arguments HERE
			if (args.length < 1 || args[0].startsWith("-h") 
					|| args[0].startsWith("-?")) {
				displayHelp();
				return;
			}
			else 
				//command line arguments HERE
				if (args.length >= 2)
					commandLine(args);

		//REPOSITORY METADATA EXTRACTION
		for (int b = 0; b<vsUDMLtxt.size(); b++) {
			
			//setting a default output file
			//If the Bus Matrix app is invoked and the output filename is not
			//specified, a default "rpd.xml" file is used.
			if (vsUDMLxml.size() == 0 && isBusMatrixInvoked)
				vsUDMLxml.add("rpd.xml");
			
			//Parameters check...
			//And the UDML file is parsed
			if (vsUDMLtxt.size() > 0 &&
				vsUDMLxml.size() > 0 &&
				vsUDMLtxt.get(b).length() > 0 &&
				vsUDMLxml.get(b).length() > 0)
				new UDMLParser(vsUDMLtxt.get(b),
								vsUDMLxml.get(b));

			//Custom XML
			if (vsUDMLxsl.size() > 0 &&
				vsUDMLtgt.size() > 0 &&
				vsUDMLxsl.get(b).length() > 0 &&
				vsUDMLtgt.get(b).length() > 0 &&
				!isBusMatrixInvoked)
				XMLUtils.xsl4Files(vsUDMLxml.get(b),
									vsUDMLxsl.get(b),
									vsUDMLtgt.get(b));

			if (isBusMatrixInvoked) {
				//picks up the XML file generated by the parser and
				//applies BusMatrix.xsl to it
				XMLUtils.xsl4Files(vsUDMLxml.get(b), insXSL1, "temp.xml");
				//creates the HTML page presenting results
				XMLUtils.xsl4Files("temp.xml", insXSL2, vsUDMLtgt.get(b));
				
				File f = new File ("temp.xml");
				System.out.println("Cleaning up temporary file: " + 
												f.getAbsolutePath());
				f.deleteOnExit();
				f = null;
			}

		}
		//REPOSITORY METADATA EXTRACTION (END)

		vsUDMLtxt = null;
		vsUDMLxml = null;
		vsUDMLxsl = null;
		vsUDMLtgt = null;
	}
}
/*
 * udml -> rpdxml.
 * rpdxml + udmlxsl = udmltgt
 * rpdxml1 (=udmltgt) + udmlxsl1 = udmltgt1 (HTML)

java -jar udmlparser.jar -udml=p6only.udml -cmd=busmatrix -udmltgt1=ShowMeTheMatrix.html 

 -udml=test/st.udml 
 -rpdxml=test/RPD.XML 
 -cmd=busmatrix 
 -udmltgt1=test/OBIEE.html
*/
