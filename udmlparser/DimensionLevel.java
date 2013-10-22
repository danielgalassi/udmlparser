package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Dimension Level Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class DimensionLevel {
	
	private String 			dimensionLevelID;
	private String			dimensionLevelName;
	private Vector <String>	logicalColumnIDs = null;

	public DimensionLevel ( String declare, 
							String catalogFolder, 
							BufferedReader udml) {
		int iFullDrillCount;
		int iGrandTotalCount;
		int iTokenIndex;
		String line;
		String tempLogColID;
		String sTrimmedDS = declare.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		dimensionLevelID = sTrimmedDS.substring(catalogFolder.length(), 
												 iIndexAS).
												 trim().replaceAll("\"", "");
		
		iFullDrillCount = sTrimmedDS.indexOf(" FULL DRILL ");
		iGrandTotalCount = sTrimmedDS.indexOf(" GRAND TOTAL ");
		
		if (iGrandTotalCount == -1 && 
			iFullDrillCount == -1)
			dimensionLevelName = sTrimmedDS.substring(iIndexAS+4).
												trim().replaceAll("\"", "");
		
		if (iFullDrillCount != -1 && 
			iGrandTotalCount == -1)
			dimensionLevelName = sTrimmedDS.substring(iIndexAS+4,
										sTrimmedDS.indexOf(" FULL DRILL ")).
										trim().replaceAll("\"", "");
		
		if (iFullDrillCount == -1 && 
			iGrandTotalCount != -1)
			dimensionLevelName = sTrimmedDS.substring(iIndexAS+4, 
										sTrimmedDS.indexOf(" GRAND TOTAL ")).
										trim().replaceAll("\"", "");
		
		if (iFullDrillCount != -1 &&
			iGrandTotalCount != -1 && 
			iFullDrillCount > iGrandTotalCount)
			dimensionLevelName = sTrimmedDS.substring(iIndexAS+4,
										sTrimmedDS.indexOf(" GRAND TOTAL ")).
										trim().replaceAll("\"", "");
		
		try {
			//HAVING STRING
			line = udml.readLine().trim().replaceAll("\"", "");

			//LOGICAL COLUMNS LIST
			if (line.indexOf("HAVING (") != -1) {
				logicalColumnIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					iFullDrillCount = line.indexOf(") FULL DRILL ");
					iGrandTotalCount = line.indexOf(") GRAND TOTAL ");
					iTokenIndex = 0;
					
					if (line.charAt(line.length()-1) == ',')
						iTokenIndex = line.length()-1;
					else {
						if (iFullDrillCount != -1 && 
							iGrandTotalCount == -1)
							iTokenIndex = line.indexOf(") FULL DRILL ");
						if (iFullDrillCount == -1 && 
							iGrandTotalCount != -1)
							iTokenIndex = line.indexOf(") GRAND TOTAL ");
						if (iFullDrillCount != -1 && 
							iGrandTotalCount != -1 && 
							iFullDrillCount > iGrandTotalCount)
							iTokenIndex = line.indexOf(") GRAND TOTAL ");
					}
					
					tempLogColID = line.substring(0, iTokenIndex);
					logicalColumnIDs.add(tempLogColID.trim());
				} while (iFullDrillCount == -1 && 
						 iGrandTotalCount == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 && 
					line.indexOf(";") == -1)
				line = udml.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		line			= null;
		tempLogColID	= null;
		sTrimmedDS		= null;
	}
	
	/**
	 * Dimension Level XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (dimensionLevelID == null) {
			dimensionLevelID = "";
		}
		Node nDimensionLevelID = xmldoc.createTextNode(dimensionLevelID);
		if (dimensionLevelName == null) {
			dimensionLevelName = "";
		}
		Node nDimensionLevelName = xmldoc.createTextNode(dimensionLevelName);

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

		if(logicalColumnIDs != null)
			//for (int i=0; i< vLogicalColumnID.size(); i++) {
			for (String sLogColID : logicalColumnIDs) {
				eLogicalTable = xmldoc.createElement("LogicalColumnID");
				if (sLogColID == null) {
					nLogicalTable = xmldoc.createTextNode("");
				} else {
					nLogicalTable = xmldoc.createTextNode(sLogColID);
				}
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
 