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

	private String			sSubjectAreaID;
	private String			sSubjectAreaName;
	private Vector <String>	vLogicalTablesID = null;
	private Vector <String> vHierDimensionsID = null;

	private void parseLogicalTable(String line) {
		int iIndexSA = line.indexOf(") SUBJECT AREA ");
		if (line.endsWith(","))
			vLogicalTablesID.add(line.substring(0, line.length()-1));
		if (iIndexSA != -1)
			vLogicalTablesID.add(line.substring(0, iIndexSA));
	}

	private void parseHierDim(String line) {
		vHierDimensionsID.add(line.substring(0, line.length()-1));
	}

	public SubjectArea (String sDeclareStmt,
						String sSubjectArea,
						BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		int iICONIDX = sTrimmedDS.indexOf(" ICON INDEX ");
		sSubjectAreaID = sTrimmedDS.substring(sSubjectArea.length(),iIndexAS).
												trim().replaceAll("\"", "");
		if (iICONIDX != -1)
			sSubjectAreaName = sTrimmedDS.substring(iIndexAS+4, iICONIDX).
										trim().replaceAll("\"", "");
		else
			sSubjectAreaName = sTrimmedDS.substring(iIndexAS+4).
										trim().replaceAll("\"", "");
		try {
			line = brUDML.readLine();
			
			//HIERARCHY DIMENSIONS
			if (line.endsWith("DIMENSIONS (")) {
				vHierDimensionsID = new Vector<String>();
				line = brUDML.readLine().trim().replaceAll("\"", "");
				while (( line.indexOf("LOGICAL TABLES (") == -1) || 
						(line.indexOf("PRIVILEGES") != -1 && 
						 line.indexOf(";") != -1)) {
					parseHierDim(line);
					line = brUDML.readLine().trim().replaceAll("\"", "");
				};
			}

			//LOGICAL TABLES LIST
			if (line.endsWith("LOGICAL TABLES (")) {
				vLogicalTablesID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					parseLogicalTable(line);
				} while (line.indexOf(") SUBJECT AREA ") == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = brUDML.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Subject Area XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sSubjectAreaID == null) {
			sSubjectAreaID = "";
		}
		Node nBusinessCatalogID = xmldoc.createTextNode(sSubjectAreaID);
		if (sSubjectAreaName == null) {
			sSubjectAreaName = "";
		}
		Node nBusinessCatalogName = xmldoc.createTextNode(sSubjectAreaName);

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
		
		if (vHierDimensionsID != null)
			for (int i=0; i< vHierDimensionsID.size(); i++) {
				eHierDim = xmldoc.createElement("HierarchyDimensionID");
				if (vHierDimensionsID.get(i) == null) {
					nHierDim = xmldoc.createTextNode("");
				} else {
					nHierDim = xmldoc.createTextNode(vHierDimensionsID.get(i));
				}
				eHierDim.appendChild(nHierDim);
				eHierDimensionList.appendChild(eHierDim);
			}
		
		eBusinessCatalog.appendChild(eHierDimensionList);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (vLogicalTablesID != null)
			for (int i=0; i< vLogicalTablesID.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				if (vLogicalTablesID.get(i) == null) {
					nLogicalTable = xmldoc.createTextNode("");
				} else {
					nLogicalTable = xmldoc.createTextNode(vLogicalTablesID.get(i));
				}
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
