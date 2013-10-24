package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Subject Area Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class SubjectArea {

	private String			subjectAreaID;
	private String			subjectAreaName;
	private Vector <String>	logicalTablesIDs = null;
	private Vector <String> hierarchyDimensionIDs = null;

	private void parseLogicalTable(String line) {
		int iIndexSA = line.indexOf(") SUBJECT AREA ");
		if (line.endsWith(","))
			logicalTablesIDs.add(line.substring(0, line.length()-1));
		if (iIndexSA != -1)
			logicalTablesIDs.add(line.substring(0, iIndexSA));
	}

	private void parseHierDim(String line) {
		hierarchyDimensionIDs.add(line.substring(0, line.length()-1));
	}

	public SubjectArea (String declare,
						String sSubjectArea,
						BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int asIdx = trimmedDeclareStatement.indexOf(" AS ");
		int iconIds = trimmedDeclareStatement.indexOf(" ICON INDEX ");
		subjectAreaID = trimmedDeclareStatement.substring(sSubjectArea.length(),asIdx).
												trim().replaceAll("\"", "");
		if (iconIds != -1)
			subjectAreaName = trimmedDeclareStatement.substring(asIdx+4, iconIds).
										trim().replaceAll("\"", "");
		else
			subjectAreaName = trimmedDeclareStatement.substring(asIdx+4).
										trim().replaceAll("\"", "");
		try {
			line = udml.readLine();
			
			//HIERARCHY DIMENSIONS
			if (line.endsWith("DIMENSIONS (")) {
				hierarchyDimensionIDs = new Vector<String>();
				line = udml.readLine().trim().replaceAll("\"", "");
				while (( line.indexOf("LOGICAL TABLES (") == -1) || 
						(line.indexOf("PRIVILEGES") != -1 && 
						 line.indexOf(";") != -1)) {
					parseHierDim(line);
					line = udml.readLine().trim().replaceAll("\"", "");
				};
			}

			//LOGICAL TABLES LIST
			if (line.endsWith("LOGICAL TABLES (")) {
				logicalTablesIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					parseLogicalTable(line);
				} while (line.indexOf(") SUBJECT AREA ") == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = udml.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line		= null;
	}

	/**
	 * Subject Area XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (subjectAreaID == null) {
			subjectAreaID = "";
		}
		Node nBusinessCatalogID = xmldoc.createTextNode(subjectAreaID);
		if (subjectAreaName == null) {
			subjectAreaName = "";
		}
		Node nBusinessCatalogName = xmldoc.createTextNode(subjectAreaName);

		Element eBusinessCatalog = xmldoc.createElement("BusinessCatalog");
		Element eBusinessCatalogID = xmldoc.createElement("BusinessCatalogID");
		Element eBusinessCatalogName = xmldoc.createElement("BusinessCatalogName");

		eBusinessCatalogID.appendChild(nBusinessCatalogID);
		eBusinessCatalogName.appendChild(nBusinessCatalogName);

		eBusinessCatalog.appendChild(eBusinessCatalogID);
		eBusinessCatalog.appendChild(eBusinessCatalogName);

		Element eHierDimensionList = xmldoc.createElement("HierarchyDimensionIDList");
		Element eHierDim = null;
		Node nHierDim = null;
		
		if (hierarchyDimensionIDs != null)
			for (String sHierDimID : hierarchyDimensionIDs) {
				eHierDim = xmldoc.createElement("HierarchyDimensionID");
				if (sHierDimID == null)
					nHierDim = xmldoc.createTextNode("");
				else
					nHierDim = xmldoc.createTextNode(sHierDimID);

				eHierDim.appendChild(nHierDim);
				eHierDimensionList.appendChild(eHierDim);
			}
		
		eBusinessCatalog.appendChild(eHierDimensionList);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (logicalTablesIDs != null)
			for (String sLogicalTableID : logicalTablesIDs) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				if (sLogicalTableID == null)
					nLogicalTable = xmldoc.createTextNode("");
				else
					nLogicalTable = xmldoc.createTextNode(sLogicalTableID);

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
