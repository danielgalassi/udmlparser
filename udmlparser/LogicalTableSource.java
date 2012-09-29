package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import utils.Utils;

/**
 * Logical Table Source Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalTableSource {

	private String			sLogTblSourceID;
	private String			sLogTblSourceName;
	private Vector <String>	vLogicalColumnID;
	private Vector <String>	vLogicalColumnCalculation;

	public LogicalTableSource ( String sDeclareStmt,
								String sLogTblSource, 
								BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sLogTblSourceID = sTrimmedDS.substring( sLogTblSource.length(), 
												iIndexAS).
												trim().replaceAll("\"", "");
		sLogTblSourceName = sTrimmedDS.substring(iIndexAS + 4).
												trim().replaceAll("\"", "");

		try {
			//DISCARD HEADING
			do {
				line = brUDML.readLine();
			} while (line.indexOf("PROJECT (") == -1);

			//LOGICAL COLUMNS
			if (line.indexOf("PROJECT (") != -1) {
				vLogicalColumnID = new Vector<String>();
				vLogicalColumnCalculation = new Vector<String>();
				do {
					line = brUDML.readLine().trim();
					if (line.indexOf(" AS ") != -1) {
						vLogicalColumnID.add(line.substring(
														line.indexOf("{") + 1,
														line.indexOf("}")).
														replaceAll("\"", ""));
						vLogicalColumnCalculation.add(line.substring(
													line.lastIndexOf("{") + 1, 
													line.lastIndexOf("}")));
					}
				} while (line.indexOf("FROM") == -1);
			}

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
	 * Logical Table Source XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		String sTemp = null;
		if (sLogTblSourceID == null) {
			sLogTblSourceID = "";
		}
		Node nLogicalTableSourceID = xmldoc.createTextNode(sLogTblSourceID);
		if (sLogTblSourceName == null) {
			sLogTblSourceName = "";
		}
		Node nLogicalTableSourceName = xmldoc.createTextNode(sLogTblSourceName);

		Element eLogicalTableSource = xmldoc.createElement("LogicalTableSource");
		Element eLogicalTableSourceID = xmldoc.createElement("LogicalTableSourceID");
		Element eLogicalTableSourceName = xmldoc.createElement("LogicalTableSourceName");

		eLogicalTableSourceID.appendChild(nLogicalTableSourceID);
		eLogicalTableSourceName.appendChild(nLogicalTableSourceName);

		eLogicalTableSource.appendChild(eLogicalTableSourceID);
		eLogicalTableSource.appendChild(eLogicalTableSourceName);

		Element eLogicalColumnList = xmldoc.createElement("LogicalColumnList");
		Element eLogicalColumn = null;
		Element eLogicalColumnID = null;
		Element eLogicalColumnCalculation = null;
		Node nLogicalColumnID = null;
		Node nLogicalColumnCalculation = null;

		Vector <String> vPhysicalColumnMappingList = new Vector<String>();
		Element ePhysicalColumnMapping = null;
		Element ePhysicalColumnID = null;
		Node nPhysicalColumnID = null;

		if(vLogicalColumnID != null) {

			for (int i=0; i< vLogicalColumnID.size(); i++) {
				eLogicalColumn = xmldoc.createElement("LogicalColumn");
				eLogicalColumnID = xmldoc.createElement("LogicalColumnID");
				if (vLogicalColumnID.get(i) == null) {
					nLogicalColumnID = xmldoc.createTextNode("");
				} else {
					nLogicalColumnID = xmldoc.createTextNode(vLogicalColumnID.get(i));
				}
				eLogicalColumnID.appendChild(nLogicalColumnID);
				eLogicalColumnCalculation = xmldoc.createElement("LogicalColumnCalculation");
				sTemp = vLogicalColumnCalculation.get(i);
				if (sTemp.replaceAll("\"", "") == null) {
					nLogicalColumnCalculation = xmldoc.createTextNode("");
				} else {
					nLogicalColumnCalculation = xmldoc.createTextNode(sTemp.replaceAll("\"", ""));
				}
				eLogicalColumnCalculation.appendChild(nLogicalColumnCalculation);

				eLogicalColumn.appendChild(eLogicalColumnID);
				eLogicalColumn.appendChild(eLogicalColumnCalculation);

				ePhysicalColumnMapping = xmldoc.createElement("PhysicalColumnMapping");
				vPhysicalColumnMappingList = Utils.CalculationParser(sLogTblSourceID, vLogicalColumnCalculation.get(i), false);
				if(vPhysicalColumnMappingList != null) {
					for(int j=0; j< vPhysicalColumnMappingList.size(); j++) {
						ePhysicalColumnID = xmldoc.createElement("PhysicalColumnID");
						if (vPhysicalColumnMappingList.get(j) == null) {
							nPhysicalColumnID = xmldoc.createTextNode("");
						} else {
							nPhysicalColumnID = xmldoc.createTextNode(vPhysicalColumnMappingList.get(j));
						}
						ePhysicalColumnID.appendChild(nPhysicalColumnID);
						ePhysicalColumnMapping.appendChild(ePhysicalColumnID);
					}
					eLogicalColumn.appendChild(ePhysicalColumnMapping);
				}
				eLogicalColumnList.appendChild(eLogicalColumn);
			}
		}

		eLogicalTableSource.appendChild(eLogicalColumnList);
		return eLogicalTableSource;
	}
}
/*
 * DECLARE LOGICAL TABLE SOURCE <FQ LTS name> AS <LTS name>
 * PROJECT (
 *  {<FQ logical column name 1>}  AS {<FQ mapping to Physical layer 1>} ,
 * {<FQ logical column name 2>}  AS {<FQ mapping to Physical layer 2>} )
 * FROM 
 * (
 * <source tables and joins> 
 * DETAIL FILTER {<filter condition>}  CAN NOT COMBINE
 * PRIVILEGES ( READ);
 */
