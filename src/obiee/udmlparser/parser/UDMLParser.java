package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import obiee.udmlparser.metadataExtract.MetadataExtract;
import obiee.udmlparser.utils.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Universal Database Markup Language (UDML for short) Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class UDMLParser {

	private Document udml;
	private Element root;
	/** is the reference to the file containing the UDML code.*/
	private File udmlExtract;
	//UDML declaration statement's first token
	private final String catalogFolders		= "DECLARE CATALOG FOLDER ";
	private final String entityFolders		= "DECLARE ENTITY FOLDER ";
	private final String folderAttributes	= "DECLARE FOLDER ATTRIBUTE ";
	private final String subjectAreas		= "DECLARE SUBJECT AREA ";
	private final String logicalTables		= "DECLARE LOGICAL TABLE ";
	private final String logicalTableSources= "DECLARE LOGICAL TABLE SOURCE ";
	private final String physicalTables		= "DECLARE TABLE ";
	private final String physicalTableKeys	= "DECLARE TABLE KEY ";
	private final String dimensionLevels	= "DECLARE LEVEL ";
	private final String hierarchyDims		= "DECLARE DIMENSION ";
	private final String foreignKeys		= "DECLARE FOREIGN KEY ";
	private final String logicalJoins		= "DECLARE ROLE RELATIONSHIP ";
	private final String logicalForeignKeys = "DECLARE LOGICAL FOREIGN KEY ";

	/**
	 * 
	 * @param sInput source UDML file
	 * @param sOutput target XML file
	 */
	public UDMLParser(String sInput, String sOutput) {
		udml		= XMLUtils.createDOMDocument();
		root		= udml.createElement("UDML");
		udmlExtract	= new File (sInput);
		if(MetadataExtract.isBusMatrixInvoked()) {
			System.out.println("BusMatrix feature");
		}
		if(isUDML()) {
			parse();
		}
		udml.appendChild(root);
		XMLUtils.saveDocument(udml, sOutput);
		udmlExtract	= null;
		root		= null;
		udml		= null;
	}

	/**
	 * Validates the file actually contains UDML statements
	 * @return true if the source contains UDML code 
	 */
	private boolean isUDML() {
		boolean isUDML = false;
		try {
			Reader udmlStreamReader = new InputStreamReader(new FileInputStream(udmlExtract), "UTF-8");
			BufferedReader udmlReader = new BufferedReader(udmlStreamReader);
			if(udmlReader.readLine().indexOf("DECLARE ") == 0)
				isUDML = true;
			udmlReader.close();
			udmlStreamReader.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}
		if (isUDML)
			System.out.println(udmlExtract + " is a valid file.");
		return isUDML;
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String line;

		try {
			// Create a FileReader and then wrap it with BufferedReader.
			Reader frNQ_UDML = new InputStreamReader(new FileInputStream(udmlExtract), "UTF-8");
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
			LogicalForeignKey lfk;

			do {
				line = brUDML.readLine();
				if (line == null)
					break;
				if (line.indexOf(catalogFolders) != -1) { //pres subject area
					System.out.println( "Processing Subject Area...");
					c = new CatalogFolder(line, catalogFolders, brUDML);
					root.appendChild(c.serialize(udml));
				}
				if (line.indexOf(subjectAreas) != -1) { //bmm subject area
					System.out.println("Processing Business Model...");
					s = new SubjectArea(line, subjectAreas, brUDML);
					root.appendChild(s.serialize(udml));
				}
				if (line.indexOf(logicalJoins) != -1) { //logical join (BMM)
					System.out.println("Processing Logical Join...");
					lj = new LogicalJoin(line, logicalJoins, brUDML);
					root.appendChild(lj.serialize(udml));
				}
				if (line.indexOf(logicalForeignKeys) != -1) { //logical foreign key join (BMM)
					System.out.println("Processing Logical (Foreign Key) Join...");
					lfk = new LogicalForeignKey(line, logicalForeignKeys, brUDML);
					root.appendChild(lfk.serialize(udml));
				}
				if (!MetadataExtract.isBusMatrixInvoked()) {
					if (line.indexOf(entityFolders) != -1) { //pres folder
						System.out.println("Processing Presentation Folder...");
						e = new EntityFolder(line, entityFolders, brUDML);
						root.appendChild(e.serialize(udml));
					}
					if (line.indexOf(folderAttributes) != -1) { //pres column
						System.out.println("Processing Presentation Column...");
						f = new FolderAttribute(line, folderAttributes, brUDML);
						try
						{
							Node node = f.serialize(udml);
							root.appendChild(node);
						}
						catch(Exception ex)
						{
							System.out.println("Presentation Column error");
						}
						
					}
					if (line.indexOf(logicalTables) != -1 &&
							line.indexOf(logicalTableSources) == -1) { //logl tbl
						System.out.println("Processing Logical Table...");
						l = new LogicalTable(line, logicalTables, brUDML);
						root.appendChild(l.serialize(udml));
					}
					if (line.indexOf(logicalTableSources) != -1) { //logl tbl src
						System.out.println("Processing Logical Table Source...");
						lts=new LogicalTableSource(line,logicalTableSources,brUDML);
						root.appendChild(lts.serialize(udml));
					}
					if (line.indexOf(physicalTables) != -1 && 
							line.indexOf(physicalTableKeys) == -1) { //physical tbl
						System.out.println("Processing Physical Table...");
						p = new PhysicalTable(line, physicalTables, brUDML);
						try
						{
							Node node = p.serialize(udml);
							root.appendChild(node);
						}
						catch(Exception ex)
						{
							System.out.println("PhysicalTable block error");
						}
					}
					if (line.indexOf(dimensionLevels) != -1) { //hier. dim. level
						System.out.println("Processing Hierarchy Dim Level...");
						d = new DimensionLevel(line, dimensionLevels, brUDML);
						root.appendChild(d.serialize(udml));
					}
					if (line.indexOf(hierarchyDims) != -1) { //hier dim
						System.out.println("Processing Hierarchy Dimension...");
						h = new HierarchyDimension(line, hierarchyDims, brUDML);
						root.appendChild(h.serialize(udml));
					}
					if (line.indexOf(foreignKeys) != -1) { //join
						System.out.println("Processing Foreign Key...");
						j = new ForeignKey(line, foreignKeys, brUDML);
						root.appendChild(j.serialize(udml));
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
