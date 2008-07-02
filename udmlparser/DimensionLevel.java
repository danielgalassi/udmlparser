package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DimensionLevel {
	
	private String 			sDimensionLevelID;
	private String			sDimensionLevelName;
	private Vector <String>	vLogicalColumnID = null;

	public DimensionLevel(String sDeclareStmt, String sCatalogFolder, BufferedReader brUDML) {
		int iFullDrillCount;
		int iGrandTotalCount;
		String line;
		sDimensionLevelID = sDeclareStmt.trim().substring(sCatalogFolder.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		iFullDrillCount = sDeclareStmt.indexOf(" FULL DRILL ");
		iGrandTotalCount = sDeclareStmt.indexOf(" GRAND TOTAL ");
		if (iGrandTotalCount == -1 && iFullDrillCount == -1)
			sDimensionLevelName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4).trim().replaceAll("\"", "");
		if(iFullDrillCount != -1 && iGrandTotalCount == -1)
			sDimensionLevelName = sDeclareStmt.substring(sDeclareStmt.indexOf(" AS ")+4, sDeclareStmt.indexOf(" FULL DRILL ")).trim().replaceAll("\"", "");
		if(iFullDrillCount == -1 && iGrandTotalCount != -1)
			sDimensionLevelName = sDeclareStmt.substring(sDeclareStmt.indexOf(" AS ")+4, sDeclareStmt.indexOf(" GRAND TOTAL ")).trim().replaceAll("\"", "");
		if(iFullDrillCount != -1 && 
			iGrandTotalCount != -1 && 
			iFullDrillCount > iGrandTotalCount)
			sDimensionLevelName = sDeclareStmt.substring(sDeclareStmt.indexOf(" AS ")+4, sDeclareStmt.indexOf(" GRAND TOTAL ")).trim().replaceAll("\"", "");

		try {
			//HAVING STRING
			line = brUDML.readLine().trim().replaceAll("\"", "");

			//LOGICAL COLUMNS LIST
			if(line.indexOf("HAVING (") != -1) {
				vLogicalColumnID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					iFullDrillCount = line.indexOf(") FULL DRILL ");
					iGrandTotalCount = line.indexOf(") GRAND TOTAL ");
					if(line.charAt(line.length()-1) == ',')
						vLogicalColumnID.add(line.substring(0, line.length()-1));
					else {
						if(iFullDrillCount != -1 && iGrandTotalCount == -1)
							vLogicalColumnID.add(line.substring(0, line.indexOf(") FULL DRILL ")));
						if(iFullDrillCount == -1 && iGrandTotalCount != -1)
							vLogicalColumnID.add(line.substring(0, line.indexOf(") GRAND TOTAL ")));
						if(iFullDrillCount != -1 && 
								iGrandTotalCount != -1 && 
								iFullDrillCount > iGrandTotalCount)
								vLogicalColumnID.add(line.substring(0, line.indexOf(") GRAND TOTAL ")));
					}
				} while (iFullDrillCount == -1 && iGrandTotalCount == -1);
			}


			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
			} while (line.indexOf(";") == -1);
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}
	
	/**
	 * Dimension Level XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nDimensionLevelID = xmldoc.createTextNode(sDimensionLevelID);
		Node nDimensionLevelName = xmldoc.createTextNode(sDimensionLevelName);

		Element eDimensionLevel = xmldoc.createElement("DimensionLevel");
		Element eDimensionLevelID = xmldoc.createElement("DimensionLevelID");
		Element eDimensionLevelName = xmldoc.createElement("DimensionLevelName");

		eDimensionLevelID.appendChild(nDimensionLevelID);
		eDimensionLevelName.appendChild(nDimensionLevelName);

		eDimensionLevel.appendChild(eDimensionLevelID);
		eDimensionLevel.appendChild(eDimensionLevelName);
		Element eLogicalTableList = xmldoc.createElement("LogicalColumnIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if(vLogicalColumnID != null)
			for (int i=0; i< vLogicalColumnID.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalColumnID");
				nLogicalTable = xmldoc.createTextNode(vLogicalColumnID.get(i));
				eLogicalTable.appendChild(nLogicalTable);
				eLogicalTableList.appendChild(eLogicalTable);
			}

		eDimensionLevel.appendChild(eLogicalTableList);
		return eDimensionLevel;
	}
}
/*
 * DECLARE LEVEL FQ Dimension Level name AS Dimension Level Alias
 * HAVING (
 * FQ Logical Column name,
 * FQ Logical Column name ) GRAND TOTAL ALIAS FULL DRILL UP COVERAGE CONSTANT 'All'
 * PRIVILEGES (...);
 */
 