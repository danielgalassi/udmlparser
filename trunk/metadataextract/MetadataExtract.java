package metadataextract;

import java.io.File;
import java.util.Vector;

import org.w3c.dom.Document;

import udmlparser.UDMLParser;
import xmlutils.XMLUtils;

/**
 * Extract tool getting UDML stmts parsed into an XML tree
 * @author danielgalassi@gmail.com
 *
 */
public class MetadataExtract {

	private static Vector <String>	vsUDMLtxt	= null;
	private static Vector <String>	vsUDMLxml	= null;
	private static Vector <String>	vsUDMLxsl	= null;
	private static Vector <String>	vsUDMLtgt	= null;
	private static Document			dBatch		= null;
	private static boolean			bBusMatrix	= false;

	public static boolean isBusMatrixInvoked () {
		return bBusMatrix;
	}

	/**
	 * Method processing XML file containing batch params
	 * @param sBatch batch params file
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
				vsUDMLtxt.add(	args[i].replaceFirst("-udml=",   ""));

			if (args[i].startsWith("-rpdxml="))
				vsUDMLxml.add(	args[i].replaceFirst("-rpdxml=", ""));

			if (args[i].startsWith("-udmlxsl="))
				vsUDMLxsl.add(	args[i].replaceFirst("-udmlxsl=",""));

			if (args[i].startsWith("-udmltgt="))
				vsUDMLtgt.add(	args[i].replaceFirst("-udmltgt=",""));
			
			if (args[i].equals("-cmd=busmatrix"))
				bBusMatrix = true;
		}
	}


	/**
	 * Available help
	 *
	 */
	private static void displayHelp() {
		System.out.println("UDML Parser utility\n\n");
		System.out.println("Application usage:");

		//RPD extract parameters
		System.out.println("Repository extract parameters:");
		System.out.println("-udml=\t\tRepository UDML file");
		System.out.println("-rpdxml=\tFull XML extract");
		System.out.println("-udmlxsl=\tXSL file for optional " +
		"transformation");
		System.out.println("-udmltgt=\tXML file resulting " +
		"from XSL transformation\n");
		System.out.println("Batch mode available:");
		System.out.println("-batch=\t\tConfiguration file\n");
		System.out.println("UNIX path form: /dir1/../dirN/file");
		System.out.println("WIN path form: drive\\dir1\\..\\dirN\\file");
	}


	/**
	 * Constructor, validation of parameters and 
	 * execution control of the extraction process
	 * @param args web catalog and repository extract process parameters
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
			if (args.length < 2 || args[0].startsWith("-h") 
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
			//required parameters check
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
				vsUDMLtgt.get(b).length() > 0)
				XMLUtils.xsl4Files(vsUDMLxml.get(b),
									vsUDMLxsl.get(b),
									vsUDMLtgt.get(b));
		}
		//REPOSITORY METADATA EXTRACTION (END)

		vsUDMLtxt	= null;
		vsUDMLxml	= null;
		vsUDMLxsl	= null;
		vsUDMLtgt	= null;
	}
}
