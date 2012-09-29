package udmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import metadataextract.MetadataExtract;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xmlutils.XMLUtils;


/**
 * Universal Database Markup Language (UDML for short) Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class UDMLParser {

	private Document		docUDML;
	private Element			root;
	private File			fNQ_UDML;
	//private FileReader		frNQ_UDML;
	//private BufferedReader	brUDML;
	//UDML declaration statement's first token
	private String			sCatalogFolder		= "DECLARE CATALOG FOLDER ";
	private String			sEntityFolder		= "DECLARE ENTITY FOLDER ";
	private String			sFolderAttribute	= "DECLARE FOLDER ATTRIBUTE ";
	private String			sSubjectArea		= "DECLARE SUBJECT AREA ";
	private String			sLogicalTable		= "DECLARE LOGICAL TABLE ";
	private String			sLogicalTableSrc	= "DECLARE LOGICAL TABLE SOURCE ";
	private String			sPhysicalTable		= "DECLARE TABLE ";
	private String			sPhysicalTableKey	= "DECLARE TABLE KEY ";
	private String			sDimensionLevel		= "DECLARE LEVEL ";
	private String			sHierarchyDim		= "DECLARE DIMENSION ";
	private String			sForeignKey			= "DECLARE FOREIGN KEY ";
	private String			sLogicalJoin		= "DECLARE ROLE RELATIONSHIP ";


	/**
	 * Constructor
	 * @param sInput source UDML file
	 * @param sOutput target XML file
	 */
	public UDMLParser(String sInput, String sOutput) {
		docUDML		= XMLUtils.createDOMDocument();
		root		= docUDML.createElement("UDML");
		fNQ_UDML	= new File (sInput);
		if(MetadataExtract.isBusMatrixInvoked())
			System.out.println("BusMatrix feature");
		if(isUDML())
			parse();
		docUDML.appendChild(root);
		XMLUtils.Document2File(docUDML, sOutput);
		fNQ_UDML	= null;
		root		= null;
		docUDML		= null;
	}

	/**
	 * Validates the file's UDML content
	 * @return true if the source contains UDML code 
	 */
	private boolean isUDML() {
		boolean bIsUDML = false;
		try {
			Reader frNQ_UDML = new InputStreamReader(new FileInputStream(fNQ_UDML), "UTF-8");
			BufferedReader brUDML = new BufferedReader(frNQ_UDML);
			//Reader in = new InputStreamReader(new FileInputStream("file"), "UTF-8"));
			if(brUDML.readLine().indexOf("DECLARE ") == 0)
				bIsUDML = true;
			brUDML.close();
			frNQ_UDML.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}
		if (bIsUDML)
			System.out.println(fNQ_UDML + " is a valid file.");
		return bIsUDML;
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String line;

		try {
			// Create a FileReader and then wrap it with BufferedReader.
			Reader frNQ_UDML = new InputStreamReader(new FileInputStream(fNQ_UDML), "UTF-8");
			BufferedReader brUDML = new BufferedReader (frNQ_UDML);

			CatalogFolder c;
			EntityFolder e;
			FolderAttribute f;
			SubjectArea s;
			LogicalTable l;
			LogicalTableSource lts;
			PhysicalTable p;
			DimensionLevel d;
			HierarchyDimension h;
			ForeignKey j;
			LogicalJoin lj;

			do {
				line = brUDML.readLine();
				if (line == null)
					break;
				if (line.indexOf(sCatalogFolder) != -1) { //pres subject area
					System.out.println( "Processing Subject Area...");
					c = new CatalogFolder(line, sCatalogFolder, brUDML);
					root.appendChild(c.serialize(docUDML));
				}
				if (line.indexOf(sSubjectArea) != -1) { //bmm subject area
					System.out.println("Processing Business Model...");
					s = new SubjectArea(line, sSubjectArea, brUDML);
					root.appendChild(s.serialize(docUDML));
				}
				if (line.indexOf(sLogicalJoin) != -1) { //logical join (BMM)
					System.out.println("Processing Logical Join...");
					lj = new LogicalJoin(line, sLogicalJoin, brUDML);
					root.appendChild(lj.serialize(docUDML));
				}
				if (!MetadataExtract.isBusMatrixInvoked()) {
					if (line.indexOf(sEntityFolder) != -1) { //pres folder
						System.out.println("Processing Presentation Folder...");
						e = new EntityFolder(line, sEntityFolder, brUDML);
						root.appendChild(e.serialize(docUDML));
					}
					if (line.indexOf(sFolderAttribute) != -1) { //pres column
						System.out.println("Processing Presentation Column...");
						f = new FolderAttribute(line, sFolderAttribute, brUDML);
						try
						{
							Node node = f.serialize(docUDML);
							root.appendChild(node);
						}
						catch(Exception ex)
						{
							System.out.println("Presentation Column error");
						}
						
					}
					if (line.indexOf(sLogicalTable) != -1 &&
							line.indexOf(sLogicalTableSrc) == -1) { //logl tbl
						System.out.println("Processing Logical Table...");
						l = new LogicalTable(line, sLogicalTable, brUDML);
						root.appendChild(l.serialize(docUDML));
					}
					if (line.indexOf(sLogicalTableSrc) != -1) { //logl tbl src
						System.out.println("Processing Logical Table Source...");
						lts=new LogicalTableSource(line,sLogicalTableSrc,brUDML);
						root.appendChild(lts.serialize(docUDML));
					}
					if (line.indexOf(sPhysicalTable) != -1 && 
							line.indexOf(sPhysicalTableKey) == -1) { //physical tbl
						System.out.println("Processing Physical Table...");
						p = new PhysicalTable(line, sPhysicalTable, brUDML);
						try
						{
							Node node = p.serialize(docUDML);
							root.appendChild(node);
						}
						catch(Exception ex)
						{
							System.out.println("PhysicalTable block error");
						}
					}
					if (line.indexOf(sDimensionLevel) != -1) { //hier. dim. level
						System.out.println("Processing Hierarchy Dim Level...");
						d = new DimensionLevel(line, sDimensionLevel, brUDML);
						root.appendChild(d.serialize(docUDML));
					}
					if (line.indexOf(sHierarchyDim) != -1) { //hier dim
						System.out.println("Processing Hierarchy Dimension...");
						h = new HierarchyDimension(line, sHierarchyDim, brUDML);
						root.appendChild(h.serialize(docUDML));
					}
					if (line.indexOf(sForeignKey) != -1) { //join
						System.out.println("Processing Foreign Key...");
						j = new ForeignKey(line, sForeignKey, brUDML);
						root.appendChild(j.serialize(docUDML));
					}
				}
			} while (true);

			c	= null;
			e	= null;
			f	= null;
			s	= null;
			l	= null;
			lts	= null;
			p	= null;
			d	= null;
			h	= null;
			j	= null;

			brUDML.close ();
			frNQ_UDML.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}

		line = null;
	}
}
