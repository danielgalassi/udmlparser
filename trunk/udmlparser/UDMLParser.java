package udmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xmlutils.XMLUtils;

/**
 * Universal Database Markup Language (UDML for short) Parser class
 * @author dgalassi
 *
 */
public class UDMLParser {

	private Document		docUDML;
	private Element			root;
	private File			fNQ_UDML;
	private FileReader		frNQ_UDML;
	private BufferedReader	brUDML;
	private String			sCatalogFolder		= "DECLARE CATALOG FOLDER ";
	private String			sEntityFolder		= "DECLARE ENTITY FOLDER ";
	private String			sFolderAttribute	= "DECLARE FOLDER ATTRIBUTE ";
	private String			sSubjectArea		= "DECLARE SUBJECT AREA ";
	private String			sLogicalTable		= "DECLARE LOGICAL TABLE ";
	private String			sLogicalTableSource	= "DECLARE LOGICAL TABLE SOURCE ";
	private String			sPhysicalTable		= "DECLARE TABLE ";
	private String			sPhysicalTableKey	= "DECLARE TABLE KEY ";

	/**
	 * Constructor
	 * @param sInput source UDML file
	 * @param sOutput target XML file
	 */
	public UDMLParser(String sInput, String sOutput) {
		docUDML = XMLUtils.createDOMDocument();
		root = docUDML.createElement("UDML");
		//root.setAttribute("xmlns", "http://www.oracle.com/rpd_cubicimpact");
		fNQ_UDML = new File (sInput);
		if(isUDML())
			parse();
		docUDML.appendChild(root);
		XMLUtils.Document2File(docUDML, sOutput);
	}

	/**
	 * Validates the file's UDML content
	 * @return true if the source contains UDML code 
	 */
	private boolean isUDML() {
		boolean bIsUDML = false;
		try {
			FileReader frNQ_UDML = new FileReader (fNQ_UDML);
			BufferedReader brUDML = new BufferedReader (frNQ_UDML);
			if(brUDML.readLine().indexOf("DECLARE ") != -1)
				bIsUDML = true;
			brUDML.close();
			frNQ_UDML.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}
		return bIsUDML;
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String line;

		try {
			// Create a FileReader and then wrap it with BufferedReader.
			frNQ_UDML = new FileReader (fNQ_UDML);
			brUDML = new BufferedReader (frNQ_UDML);
			CatalogFolder c;
			EntityFolder e;
			FolderAttribute f;
			SubjectArea s;
			LogicalTable l;
			LogicalTableSource llts;
			PhysicalTable p;

			do {
				line = brUDML.readLine();
				if (line == null)
					break;
				if (line.indexOf(sCatalogFolder) != -1) { //pres subject area
					c = new CatalogFolder(line, sCatalogFolder, brUDML);
					root.appendChild(c.serialize(docUDML));
				}
				if (line.indexOf(sEntityFolder) != -1) { //pres folder
					e = new EntityFolder(line, sEntityFolder, brUDML);
					root.appendChild(e.serialize(docUDML));
				}
				if (line.indexOf(sFolderAttribute) != -1) { //pres column
					f = new FolderAttribute(line, sFolderAttribute, brUDML);
					root.appendChild(f.serialize(docUDML));
				}
				if (line.indexOf(sSubjectArea) != -1) { //bmm subject area
					s = new SubjectArea(line, sSubjectArea, brUDML);
					root.appendChild(s.serialize(docUDML));
				}
				if (line.indexOf(sLogicalTable) != -1 &&
						line.indexOf(sLogicalTableSource) == -1) { //logical table
					l = new LogicalTable(line, sLogicalTable, brUDML);
					root.appendChild(l.serialize(docUDML));
				}
				if (line.indexOf(sLogicalTableSource) != -1) { //logical table source
					llts = new LogicalTableSource(line, sLogicalTableSource, brUDML);
					root.appendChild(llts.serialize(docUDML));
				}
				if (line.indexOf(sPhysicalTable) != -1 && 
						line.indexOf(sPhysicalTableKey) == -1) { //physical table
					p = new PhysicalTable(line, sPhysicalTable, brUDML);
					root.appendChild(p.serialize(docUDML));
				}
			} while (true);

			brUDML.close ();
			frNQ_UDML.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}
	}
}
