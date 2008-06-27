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
 * @author dgalassi
 *
 */
public class LogicalTableSource {

	private String		sLogicalTableSourceID;
	private String		sLogicalTableSourceName;
	private Vector		vLogicalColumnID;
	private Vector		vLogicalColumnCalculation;

	public LogicalTableSource (String sDeclareStmt, String sLogicalTableSource, BufferedReader brUDML) {
		String line;
		sLogicalTableSourceID = sDeclareStmt.trim().substring(sLogicalTableSource.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		sLogicalTableSourceName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4).trim().replaceAll("\"", "");

		try {
			//DISCARD HEADING
			do {
				line = brUDML.readLine();
			} while (line.indexOf("PROJECT (") == -1);

			//LOGICAL COLUMNS
			if (line.indexOf("PROJECT (") != -1) {
				vLogicalColumnID = new Vector();
				vLogicalColumnCalculation = new Vector();
				do {
					line = brUDML.readLine().trim();//.replaceAll("\"", "");
					if(line.indexOf(" AS ") != -1) {
						vLogicalColumnID.add(line.substring(line.indexOf("{")+1, line.indexOf("}")).replaceAll("\"", ""));
						vLogicalColumnCalculation.add(line.substring(line.lastIndexOf("{")+1, line.lastIndexOf("}")));
					}
				} while (line.indexOf("FROM") == -1);
			}

			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
			} while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1);
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Logical Table Source XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		String sTemp = null;
		Node nLogicalTableSourceID = xmldoc.createTextNode(sLogicalTableSourceID);
		Node nLogicalTableSourceName = xmldoc.createTextNode(sLogicalTableSourceName);

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

		Vector vPhysicalColumnMappingList = new Vector();
		Element ePhysicalColumnMapping = null;
		Element ePhysicalColumnID = null;
		Node nPhysicalColumnID = null;

		if(vLogicalColumnID != null) {

			for (int i=0; i< vLogicalColumnID.size(); i++) {
				eLogicalColumn = xmldoc.createElement("LogicalColumn");
				eLogicalColumnID = xmldoc.createElement("LogicalColumnID");
				nLogicalColumnID = xmldoc.createTextNode((String)vLogicalColumnID.get(i));
				eLogicalColumnID.appendChild(nLogicalColumnID);
				eLogicalColumnCalculation = xmldoc.createElement("LogicalColumnCalculation");
				sTemp = (String)vLogicalColumnCalculation.get(i);
				nLogicalColumnCalculation = xmldoc.createTextNode(sTemp.replaceAll("\"", ""));
				eLogicalColumnCalculation.appendChild(nLogicalColumnCalculation);

				eLogicalColumn.appendChild(eLogicalColumnID);
				eLogicalColumn.appendChild(eLogicalColumnCalculation);

				ePhysicalColumnMapping = xmldoc.createElement("PhysicalColumnMapping");
				vPhysicalColumnMappingList = Utils.CalculationParser(sLogicalTableSourceID, (String)vLogicalColumnCalculation.get(i), false);
				if(vPhysicalColumnMappingList != null) {
					for(int j=0; j< vPhysicalColumnMappingList.size(); j++) {
						ePhysicalColumnID = xmldoc.createElement("PhysicalColumnID");
						nPhysicalColumnID = xmldoc.createTextNode((String)vPhysicalColumnMappingList.get(j));
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
 * <source tables & joins> 
 * DETAIL FILTER {<filter condition>}  CAN NOT COMBINE
 * PRIVILEGES ( READ);
 */
