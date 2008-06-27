package metadataextract;

import udmlparser.UDMLParser;
import xmlutils.XMLUtils;

/**
 * Extract tool getting UDML stmts parsed into an XML tree
 * @author daniel
 *
 */
public class MetadataExtract {

	private static String sUDMLtxt		= null;
	private static String sUDMLxml		= null;
	private static String sUDMLxsl		= null;
	private static String sUDMLtarget	= null;
	private static String sWebCatDir	= null;
	private static String sWorkingDir	= null;
	private static String sXMLFile		= null;
	private static String sXSLFile		= null;
	private static String sTXTFile		= null;

	/**
	 * Constructor, validation of parameters and 
	 * execution control of the extraction process
	 * @param args web catalog and repository extract process parameters
	 */
	public static void main(String[] args) {
		if (args.length < 2 || args[0].startsWith("-h") 
				|| args[0].startsWith("-?")) {
			System.out.println("Application usage:");

			//RPD extract parameters
			System.out.println("Repository extract parameters:");
			System.out.println("-udml=\t\tRepository UDML file");
			System.out.println("-rpdxml=\tFull XML extract");
			System.out.println("-udmlxsl=\tXSL file for optional " +
					"transformation");
			System.out.println("-udmltgt=\tXML file resulting " +
					"from XSL transformation");
			
			System.out.println("UNIX path form: /dir1/../dirN/file");
			System.out.println("WIN path form: drive\\dir1\\..\\dirN\\file");
			return;
		}

		for(int i=0; i<args.length; i++) {
			if (args[i].startsWith("-udml="))
				sUDMLtxt	= args[i].replaceFirst("-udml=",	"");

			if (args[i].startsWith("-rpdxml="))
				sUDMLxml	= args[i].replaceFirst("-rpdxml=",	"");

			if (args[i].startsWith("-udmlxsl="))
				sUDMLxsl	= args[i].replaceFirst("-udmlxsl=",	"");

			if (args[i].startsWith("-udmltgt="))
				sUDMLtarget	= args[i].replaceFirst("-udmltgt=",	"");
		}

		//REPOSITORY METADATA EXTRACTION
		//required parameters check
		if (sUDMLtxt != null && sUDMLxml != null) {
			new UDMLParser(sUDMLtxt, sUDMLxml);
			//Simplified XML
			if (sUDMLxsl != null && sUDMLtarget != null)
				XMLUtils.xsl4Files(sUDMLxml, sUDMLxsl, sUDMLtarget);
		}
		//REPOSITORY METADATA EXTRACTION (END)

		sUDMLtxt		= null;
		sUDMLxml		= null;
		sUDMLxsl		= null;
		sUDMLtarget		= null;
		sWebCatDir		= null;
		sWorkingDir		= null;
		sXMLFile		= null;
		sXSLFile		= null;
		sTXTFile		= null;
	}
}
