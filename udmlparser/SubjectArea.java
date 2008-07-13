package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Subject Area Parser class
 * @author dgalassi
 *
 */
public class SubjectArea {

	private String			sSubjectAreaID;
	private String			sSubjectAreaName;
	private Vector <String>	vLogicalTablesID = null;

	public SubjectArea (String sDeclareStmt, String sSubjectArea, BufferedReader brUDML) {
		String line;
		sSubjectAreaID = sDeclareStmt.trim().substring(sSubjectArea.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		sSubjectAreaName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4).trim().replaceAll("\"", "");

		try {
			//DISCARD HIERARCHY DIMENSIONS
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
			} while ((line.indexOf("LOGICAL TABLES (") == -1) || 
					(line.indexOf("PRIVILEGES") != -1 && line.indexOf(";") != -1));

			//LOGICAL TABLES LIST
			if(line.indexOf("LOGICAL TABLES ") != -1) {
				vLogicalTablesID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					if(line.charAt(line.length()-1) == ',')
						vLogicalTablesID.add(line.substring(0,line.length()-1));
					if(line.indexOf(") SUBJECT AREA ") != -1)
						vLogicalTablesID.add(line.substring(0, line.indexOf(") SUBJECT AREA ")).trim().replaceAll("\"", ""));
				} while (line.indexOf(") SUBJECT AREA ") == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1) {
				line = brUDML.readLine();
			}
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Subject Area XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nBusinessCatalogID = xmldoc.createTextNode(sSubjectAreaID);
		Node nBusinessCatalogName = xmldoc.createTextNode(sSubjectAreaName);

		Element eBusinessCatalog = xmldoc.createElement("BusinessCatalog");
		Element eBusinessCatalogID = xmldoc.createElement("BusinessCatalogID");
		Element eBusinessCatalogName = xmldoc.createElement("BusinessCatalogName");

		eBusinessCatalogID.appendChild(nBusinessCatalogID);
		eBusinessCatalogName.appendChild(nBusinessCatalogName);

		eBusinessCatalog.appendChild(eBusinessCatalogID);
		eBusinessCatalog.appendChild(eBusinessCatalogName);
		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if(vLogicalTablesID != null)
			for (int i=0; i< vLogicalTablesID.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				nLogicalTable = xmldoc.createTextNode(vLogicalTablesID.get(i));
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
