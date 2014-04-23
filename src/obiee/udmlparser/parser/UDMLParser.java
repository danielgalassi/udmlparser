package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

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
	 * @param input source UDML file
	 * @param output target XML file
	 */
	public UDMLParser(String input, String output) {
		udml		= XMLUtils.createDocument();
		root		= udml.createElement("UDML");
		udmlExtract	= new File(input);

		if(MetadataExtract.isBusMatrixInvoked()) {
			System.out.println("BusMatrix feature");
		}
		if(isUDML()) {
			parse();
		}
		udml.appendChild(root);
		XMLUtils.saveDocument(udml, output);
	}

	/**
	 * Validates the file actually contains UDML statements
	 * @return true if the source contains UDML code 
	 */
	private boolean isUDML() {
		boolean isUDML = false;
		try {
			Scanner udml = new Scanner(udmlExtract);

			if(udml.nextLine().indexOf("DECLARE ") == 0) {
				isUDML = true;
			}
			udml.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File contains UDML code = " + isUDML);
		return isUDML;
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String header;

		try {
			// Create a FileReader and then wrap it with BufferedReader.
			Reader reader = new InputStreamReader(new FileInputStream(udmlExtract), "UTF-8");
			BufferedReader udmlBuffer = new BufferedReader (reader);

			do {
				UDMLObject object = null;
				header = udmlBuffer.readLine();
				if (header == null) {
					break;
				}
				if (header.indexOf(catalogFolders) != -1) { //pres subject area
					System.out.println( "Processing Subject Area...");
					object = new CatalogFolder(header, catalogFolders, udmlBuffer);
				}
				if (header.indexOf(subjectAreas) != -1) { //bmm subject area
					System.out.println("Processing Business Model...");
					object = new SubjectArea(header, subjectAreas, udmlBuffer);
				}
				if (header.indexOf(logicalJoins) != -1) { //logical join (BMM)
					System.out.println("Processing Logical Join...");
					object = new LogicalJoin(header, logicalJoins, udmlBuffer);
				}
				if (header.indexOf(logicalForeignKeys) != -1) { //logical foreign key join (BMM)
					System.out.println("Processing Logical (Foreign Key) Join...");
					object = new LogicalForeignKey(header, logicalForeignKeys, udmlBuffer);
				}

				if (!MetadataExtract.isBusMatrixInvoked()) {
					if (header.indexOf(entityFolders) != -1) { //pres folder
						System.out.println("Processing Presentation Folder...");
						object = new EntityFolder(header, entityFolders, udmlBuffer);
					}
					if (header.indexOf(folderAttributes) != -1) { //pres column
						System.out.println("Processing Presentation Column...");
						object = new FolderAttribute(header, folderAttributes, udmlBuffer);
					}
					if (header.indexOf(logicalTables) != -1 && header.indexOf(logicalTableSources) == -1) { //logl tbl
						System.out.println("Processing Logical Table...");
						object = new LogicalTable(header, logicalTables, udmlBuffer);
					}
					if (header.indexOf(logicalTableSources) != -1) { //logl tbl src
						System.out.println("Processing Logical Table Source...");
						object=new LogicalTableSource(header,logicalTableSources,udmlBuffer);
					}
					if (header.indexOf(physicalTables) != -1 && 
							header.indexOf(physicalTableKeys) == -1) { //physical tbl
						System.out.println("Processing Physical Table...");
						object = new PhysicalTable(header, physicalTables, udmlBuffer);
					}
					if (header.indexOf(dimensionLevels) != -1) { //hier. dim. level
						System.out.println("Processing Hierarchy Dim Level...");
						object = new DimensionLevel(header, dimensionLevels, udmlBuffer);
					}
					if (header.indexOf(hierarchyDims) != -1) { //hier dim
						System.out.println("Processing Hierarchy Dimension...");
						object = new HierarchyDimension(header, hierarchyDims, udmlBuffer);
					}
					if (header.indexOf(foreignKeys) != -1) { //join
						System.out.println("Processing Foreign Key...");
						object = new ForeignKey(header, foreignKeys, udmlBuffer);
					}
				}
				if (!(object == null)) {
					try
					{
						Node node = object.serialize(udml);
						root.appendChild(node);
					}
					catch(Exception ex)
					{
						System.out.println(object == null);
						ex.printStackTrace();
					}
				}
			} while (true);


			udmlBuffer.close ();
			reader.close();
		}
		catch (IOException e) {
			System.out.println ("IO exception =" + e );
		}

		header = null;
	}
}
