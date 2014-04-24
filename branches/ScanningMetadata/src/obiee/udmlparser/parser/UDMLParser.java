package obiee.udmlparser.parser;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import obiee.udmlparser.metadataExtract.MetadataExtract;
import obiee.udmlparser.utils.XMLUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Universal Database Markup Language (UDML for short) Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class UDMLParser {

	private final Logger logger = LogManager.getLogger(UDMLParser.class.getName());

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

		logger.info("Creating Parser");
		logger.info("BusMatrix feature invoked = {}", MetadataExtract.isBusMatrixInvoked());
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
			if(udml.nextLine().startsWith("DECLARE ")) {
				isUDML = true;
			}
			udml.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("File contains UDML code = {}", isUDML);
		return isUDML;
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String header;

		logger.info("Parsing UDML...");
		try {
			Scanner udmlScanner = new Scanner(udmlExtract);

			do {
				UDMLObject object = null;
				header = udmlScanner.nextLine();

				if (header.indexOf(catalogFolders) != -1) { //pres subject area
					logger.info("Processing Subject Area...");
					object = new CatalogFolder(header, catalogFolders, udmlScanner);
				}
				if (header.indexOf(subjectAreas) != -1) { //bmm subject area
					logger.info("Processing Business Model...");
					object = new SubjectArea(header, subjectAreas, udmlScanner);
				}
				if (header.indexOf(logicalJoins) != -1) { //logical join (BMM)
					logger.info("Processing Logical Join...");
					object = new LogicalJoin(header, logicalJoins, udmlScanner);
				}
				if (header.indexOf(logicalForeignKeys) != -1) { //logical foreign key join (BMM)
					logger.info("Processing Logical (Foreign Key) Join...");
					object = new LogicalForeignKey(header, logicalForeignKeys, udmlScanner);
				}
				if (!MetadataExtract.isBusMatrixInvoked()) {
					if (header.indexOf(entityFolders) != -1) { //pres folder
						logger.info("Processing Presentation Folder...");
						object = new EntityFolder(header, entityFolders, udmlScanner);
					}
					if (header.indexOf(folderAttributes) != -1) { //pres column
						logger.info("Processing Presentation Column...");
						object = new FolderAttribute(header, folderAttributes, udmlScanner);
					}
					if (header.indexOf(logicalTables) != -1 && header.indexOf(logicalTableSources) == -1) { //logl tbl
						logger.info("Processing Logical Table...");
						object = new LogicalTable(header, logicalTables, udmlScanner);
					}
					if (header.indexOf(logicalTableSources) != -1) { //logl tbl src
						logger.info("Processing Logical Table Source...");
						object = new LogicalTableSource(header,logicalTableSources,udmlScanner);
					}
					if (header.indexOf(physicalTables) != -1 && header.indexOf(physicalTableKeys) == -1) { //physical tbl
						logger.info("Processing Physical Table...");
						object = new PhysicalTable(header, physicalTables, udmlScanner);
					}
					if (header.indexOf(dimensionLevels) != -1) { //hier. dim. level
						logger.info("Processing Hierarchy Dim Level...");
						object = new DimensionLevel(header, dimensionLevels, udmlScanner);
					}
					if (header.indexOf(hierarchyDims) != -1) { //hier dim
						logger.info("Processing Hierarchy Dimension...");
						object = new HierarchyDimension(header, hierarchyDims, udmlScanner);
					}
					if (header.indexOf(foreignKeys) != -1) { //join
						logger.info("Processing Foreign Key...");
						object = new ForeignKey(header, foreignKeys, udmlScanner);
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
						logger.error(ex.getClass() + " thrown while creating XUDML DOM.");
					}
				}
			} while (udmlScanner.hasNextLine());

			udmlScanner.close ();
		}
		catch (IOException e) {
			logger.error(e.getClass() + " thrown while parsing UDML.");
		}

		logger.info("Parsing complete");
	}
}
