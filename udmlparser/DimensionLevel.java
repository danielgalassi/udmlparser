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
		int fullDrillIdx;
		int grandTotalIdx;
		int tokenIdx;
		String line;
		String tempLogColID;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		dimensionLevelID = trimmedDeclareStatement.substring(catalogFolder.length(), 
												 iIndexAS).
												 trim().replaceAll("\"", "");
		
		fullDrillIdx = trimmedDeclareStatement.indexOf(" FULL DRILL ");
		grandTotalIdx = trimmedDeclareStatement.indexOf(" GRAND TOTAL ");
		
		if (grandTotalIdx == -1 && 
			fullDrillIdx == -1)
			dimensionLevelName = trimmedDeclareStatement.substring(iIndexAS+4).
												trim().replaceAll("\"", "");
		
		if (fullDrillIdx != -1 && 
			grandTotalIdx == -1)
			dimensionLevelName = trimmedDeclareStatement.substring(iIndexAS+4,
										trimmedDeclareStatement.indexOf(" FULL DRILL ")).
										trim().replaceAll("\"", "");
		
		if (fullDrillIdx == -1 && 
			grandTotalIdx != -1)
			dimensionLevelName = trimmedDeclareStatement.substring(iIndexAS+4, 
										trimmedDeclareStatement.indexOf(" GRAND TOTAL ")).
										trim().replaceAll("\"", "");
		
		if (fullDrillIdx != -1 &&
			grandTotalIdx != -1 && 
			fullDrillIdx > grandTotalIdx)
			dimensionLevelName = trimmedDeclareStatement.substring(iIndexAS+4,
										trimmedDeclareStatement.indexOf(" GRAND TOTAL ")).
										trim().replaceAll("\"", "");
		
		try {
			//HAVING STRING
			line = udml.readLine().trim().replaceAll("\"", "");

			//LOGICAL COLUMNS LIST
			if (line.indexOf("HAVING (") != -1) {
				logicalColumnIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					fullDrillIdx = line.indexOf(") FULL DRILL ");
					grandTotalIdx = line.indexOf(") GRAND TOTAL ");
					tokenIdx = 0;
					
					if (line.charAt(line.length()-1) == ',')
						tokenIdx = line.length()-1;
					else {
						if (fullDrillIdx != -1 && 
							grandTotalIdx == -1)
							tokenIdx = line.indexOf(") FULL DRILL ");
						if (fullDrillIdx == -1 && 
							grandTotalIdx != -1)
							tokenIdx = line.indexOf(") GRAND TOTAL ");
						if (fullDrillIdx != -1 && 
							grandTotalIdx != -1 && 
							fullDrillIdx > grandTotalIdx)
							tokenIdx = line.indexOf(") GRAND TOTAL ");
					}
					
					tempLogColID = line.substring(0, tokenIdx);
					logicalColumnIDs.add(tempLogColID.trim());
				} while (fullDrillIdx == -1 && 
						 grandTotalIdx == -1);
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
		trimmedDeclareStatement		= null;
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
 