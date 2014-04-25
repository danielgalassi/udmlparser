package obiee.udmlparser.parser;

import metadata.Repository;
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

	private Document rpdxml;
	private Element root;
	/** is the reference to the UDML code.*/
	private Repository repository;
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
		rpdxml		= XMLUtils.createDocument();
		root		= rpdxml.createElement("UDML");
		repository	= new Repository(input);

		logger.info("Creating Parser");
		logger.info("BusMatrix feature invoked = {}", MetadataExtract.isBusMatrixInvoked());
		if(repository.isValid()) {
			parse();
		}

		rpdxml.appendChild(root);
		XMLUtils.saveDocument(rpdxml, output);
	}

	/**
	 * Parser method
	 */
	private void parse() {
		String header;

		logger.info("Parsing UDML...");
		do {
			UDMLObject object = null;
			header = repository.nextLine();

			if (header.indexOf(catalogFolders) != -1) { //pres subject area
				logger.info("Processing Subject Area...");
				object = new CatalogFolder(header, catalogFolders, repository);
			}
			if (header.indexOf(subjectAreas) != -1) { //bmm subject area
				logger.info("Processing Business Model...");
				object = new SubjectArea(header, subjectAreas, repository);
			}
			if (header.indexOf(logicalJoins) != -1) { //logical join (BMM)
				logger.info("Processing Logical Join...");
				object = new LogicalJoin(header, logicalJoins, repository);
			}
			if (header.indexOf(logicalForeignKeys) != -1) { //logical foreign key join (BMM)
				logger.info("Processing Logical (Foreign Key) Join...");
				object = new LogicalForeignKey(header, logicalForeignKeys, repository);
			}
			if (!MetadataExtract.isBusMatrixInvoked()) {
				if (header.indexOf(entityFolders) != -1) { //pres folder
					logger.info("Processing Presentation Folder...");
					object = new EntityFolder(header, entityFolders, repository);
				}
				if (header.indexOf(folderAttributes) != -1) { //pres column
					logger.info("Processing Presentation Column...");
					object = new FolderAttribute(header, folderAttributes, repository);
				}
				if (header.indexOf(logicalTables) != -1 && header.indexOf(logicalTableSources) == -1) { //logl tbl
					logger.info("Processing Logical Table...");
					object = new LogicalTable(header, logicalTables, repository);
				}
				if (header.indexOf(logicalTableSources) != -1) { //logl tbl src
					logger.info("Processing Logical Table Source...");
					object = new LogicalTableSource(header,logicalTableSources,repository);
				}
				if (header.indexOf(physicalTables) != -1 && header.indexOf(physicalTableKeys) == -1) { //physical tbl
					logger.info("Processing Physical Table...");
					object = new PhysicalTable(header, physicalTables, repository);
				}
				if (header.indexOf(dimensionLevels) != -1) { //hier. dim. level
					logger.info("Processing Hierarchy Dim Level...");
					object = new DimensionLevel(header, dimensionLevels, repository);
				}
				if (header.indexOf(hierarchyDims) != -1) { //hier dim
					logger.info("Processing Hierarchy Dimension...");
					object = new HierarchyDimension(header, hierarchyDims, repository);
				}
				if (header.indexOf(foreignKeys) != -1) { //join
					logger.info("Processing Foreign Key...");
					object = new ForeignKey(header, foreignKeys, repository);
				}
			}
			if (!(object == null)) {
				try
				{
					Node node = object.serialize(rpdxml);
					root.appendChild(node);
				}
				catch(Exception ex)
				{
					logger.error(ex.getClass() + " thrown while creating XUDML DOM.");
				}
			}
		} while (repository.hasNextLine());

		repository.close();

		logger.info("Parsing complete");
	}
}
