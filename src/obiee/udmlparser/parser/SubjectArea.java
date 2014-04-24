package obiee.udmlparser.parser;

import java.util.Scanner;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Subject Area Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class SubjectArea implements UDMLObject {

	private String			subjectAreaID;
	private String			subjectAreaName;
	private Vector <String>	logicalTablesIDs = null;
	private Vector <String> hierarchyDimensionIDs = null;

	private void parseLogicalTable(String line) {
		int indexSA = line.indexOf(") SUBJECT AREA ");
		if (line.endsWith(",")) {
			logicalTablesIDs.add(line.substring(0, line.length()-1));
		}
		if (indexSA != -1) {
			logicalTablesIDs.add(line.substring(0, indexSA));
		}
	}

	private void parseHierDim(String line) {
		hierarchyDimensionIDs.add(line.substring(0, line.length()-1));
	}

	public SubjectArea (String declare, String sSubjectArea, Scanner udml) {
		String line;
		String header = declare.trim();
		int indexAS = header.indexOf(" AS ");
		int indexICON = header.indexOf(" ICON INDEX ");
		subjectAreaID = header.substring(sSubjectArea.length(),indexAS).trim().replaceAll("\"", "");

		if (indexICON != -1) {
			subjectAreaName = header.substring(indexAS+4, indexICON).trim().replaceAll("\"", "");
		}
		else {
			subjectAreaName = header.substring(indexAS+4).trim().replaceAll("\"", "");
		}

		line = udml.nextLine();

		//HIERARCHY DIMENSIONS
		if (line.endsWith("DIMENSIONS (")) {
			hierarchyDimensionIDs = new Vector<String>();
			line = udml.nextLine().trim().replaceAll("\"", "");
			while (!line.contains("LOGICAL TABLES (") || (line.contains("PRIVILEGES") && line.contains(";"))) {
				parseHierDim(line);
				line = udml.nextLine().trim().replaceAll("\"", "");
			};
		}

		//LOGICAL TABLES LIST
		if (line.endsWith("LOGICAL TABLES (")) {
			logicalTablesIDs = new Vector<String>();
			do {
				line = udml.nextLine().trim().replaceAll("\"", "");
				parseLogicalTable(line);
			} while (!line.contains(") SUBJECT AREA "));
		}

		//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
		while (!line.contains("PRIVILEGES") && !line.contains(";")) {
			line = udml.nextLine();
		}
	}

	/**
	 * Subject Area XML serializer
	 * @param doc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document doc) {
		if (subjectAreaID == null) {
			subjectAreaID = "";
		}
		Node nBusinessCatalogID = doc.createTextNode(subjectAreaID);
		if (subjectAreaName == null) {
			subjectAreaName = "";
		}
		Node nBusinessCatalogName = doc.createTextNode(subjectAreaName);

		Element eBusinessCatalog = doc.createElement("BusinessCatalog");
		Element eBusinessCatalogID = doc.createElement("BusinessCatalogID");
		Element eBusinessCatalogName = doc.createElement("BusinessCatalogName");

		eBusinessCatalogID.appendChild(nBusinessCatalogID);
		eBusinessCatalogName.appendChild(nBusinessCatalogName);

		eBusinessCatalog.appendChild(eBusinessCatalogID);
		eBusinessCatalog.appendChild(eBusinessCatalogName);

		Element eHierDimensionList = doc.createElement("HierarchyDimensionIDList");
		Element eHierDim = null;
		Node nHierDim = null;

		if (hierarchyDimensionIDs != null)
			for (String sHierDimID : hierarchyDimensionIDs) {
				eHierDim = doc.createElement("HierarchyDimensionID");
				if (sHierDimID == null) {
					sHierDimID = "";
				}
				nHierDim = doc.createTextNode(sHierDimID);

				eHierDim.appendChild(nHierDim);
				eHierDimensionList.appendChild(eHierDim);
			}

		eBusinessCatalog.appendChild(eHierDimensionList);

		Element eLogicalTableList = doc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (logicalTablesIDs != null)
			for (String logicalTableID : logicalTablesIDs) {
				eLogicalTable = doc.createElement("LogicalTableID");
				if (logicalTableID == null) {
					logicalTableID = "";
				}
				nLogicalTable = doc.createTextNode(logicalTableID);

				eLogicalTable.appendChild(nLogicalTable);
				eLogicalTableList.appendChild(eLogicalTable);
			}

		eBusinessCatalog.appendChild(eLogicalTableList);
		return eBusinessCatalog;
	}
}
/*
 * DECLARE SUBJECT AREA <FQ Subject Area Name> AS <Subject Area Name>
 * DIMENSIONS (
 * <hierarchy dim>,
 * <hierarchy dim>)
 * LOGICAL TABLES (
 * <FQ logical tbl>,
 * <FQ logical tbl>) SUBJECT AREA <status>
 * PRIVILEGES (<...>);
 */
