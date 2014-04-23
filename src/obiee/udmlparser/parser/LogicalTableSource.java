package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import obiee.udmlparser.utils.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Table Source Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalTableSource implements UDMLObject {

	private String			logicalTableSourceID;
	private String			logicalTableSourceName;
	private Vector <String>	logicalColumnIDs;
	private Vector <String>	logicalColumnCalculations;

	public LogicalTableSource (String declare,
			String logicalTableSource, 
			BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		int eol;
		int cbr;
		this.logicalTableSourceID = trimmedDeclareStatement.substring( logicalTableSource.length(), 
				iIndexAS).
				trim().replaceAll("\"", "");
		this.logicalTableSourceName = trimmedDeclareStatement.substring(iIndexAS + 4).
				trim().replaceAll("\"", "");

		try {
			//DISCARD HEADING
			do {
				line = udml.readLine();
			} while (line.indexOf("PROJECT (") == -1);

			//LOGICAL COLUMNS
			if (line.indexOf("PROJECT (") != -1) {
				logicalColumnIDs = new Vector<String>();
				logicalColumnCalculations = new Vector<String>();
				do {
					line = udml.readLine().trim();
					if (line.indexOf(" AS ") != -1 && 
							line.indexOf(" CAST") == -1) {
						logicalColumnIDs.add(line.substring(
								line.indexOf("{") + 1,
								line.indexOf("}")).
								replaceAll("\"", ""));
						//"end-of-line" in case } is missing. Issue # 6.
						eol = line.lastIndexOf("}");
						if (eol < line.lastIndexOf("{")-1)
							eol = line.length()-1;
						cbr = line.lastIndexOf("{")+1;
						if (cbr >= line.length())
							cbr = line.lastIndexOf("{");
						logicalColumnCalculations.add(line.substring(
								cbr,
								eol));
					}
				} while (line.indexOf("FROM") == -1);
			}

			while ( line.indexOf("PRIVILEGES") == -1 && 
					line.indexOf(";") == -1)
				line = udml.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line = null;
	}

	/**
	 * Logical Table Source XML serializer
	 * @param doc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document doc) {
		String sTemp = null;
		if (logicalTableSourceID == null) {
			logicalTableSourceID = "";
		}
		Node nLogicalTableSourceID = doc.createTextNode(logicalTableSourceID);
		if (logicalTableSourceName == null) {
			logicalTableSourceName = "";
		}
		Node nLogicalTableSourceName = doc.createTextNode(logicalTableSourceName);

		Element eLogicalTableSource = doc.createElement("LogicalTableSource");
		Element eLogicalTableSourceID = doc.createElement("LogicalTableSourceID");
		Element eLogicalTableSourceName = doc.createElement("LogicalTableSourceName");

		eLogicalTableSourceID.appendChild(nLogicalTableSourceID);
		eLogicalTableSourceName.appendChild(nLogicalTableSourceName);

		eLogicalTableSource.appendChild(eLogicalTableSourceID);
		eLogicalTableSource.appendChild(eLogicalTableSourceName);

		Element eLogicalColumnList = doc.createElement("LogicalColumnList");
		Element eLogicalColumn = null;
		Element eLogicalColumnID = null;
		Element eLogicalColumnCalculation = null;
		Node nLogicalColumnID = null;
		Node nLogicalColumnCalculation = null;

		Vector <String> vPhysicalColumnMappingList = new Vector<String>();
		Element ePhysicalColumnMapping = null;
		Element ePhysicalColumnID = null;
		Node nPhysicalColumnID = null;

		if(logicalColumnIDs != null) {

			for (int i=0; i< logicalColumnIDs.size(); i++) {
				eLogicalColumn = doc.createElement("LogicalColumn");
				eLogicalColumnID = doc.createElement("LogicalColumnID");
				if (logicalColumnIDs.get(i) == null) {
					nLogicalColumnID = doc.createTextNode("");
				} else {
					nLogicalColumnID = doc.createTextNode(logicalColumnIDs.get(i));
				}
				eLogicalColumnID.appendChild(nLogicalColumnID);
				eLogicalColumnCalculation = doc.createElement("LogicalColumnCalculation");
				sTemp = logicalColumnCalculations.get(i);
				if (sTemp.replaceAll("\"", "") == null) {
					nLogicalColumnCalculation = doc.createTextNode("");
				} else {
					nLogicalColumnCalculation = doc.createTextNode(sTemp.replaceAll("\"", ""));
				}
				eLogicalColumnCalculation.appendChild(nLogicalColumnCalculation);

				eLogicalColumn.appendChild(eLogicalColumnID);
				eLogicalColumn.appendChild(eLogicalColumnCalculation);

				ePhysicalColumnMapping = doc.createElement("PhysicalColumnMapping");
				vPhysicalColumnMappingList = Utils.CalculationParser(logicalTableSourceID, logicalColumnCalculations.get(i), false);
				if(vPhysicalColumnMappingList != null) {
					for (String sPhysColMapping : vPhysicalColumnMappingList) {
						ePhysicalColumnID = doc.createElement("PhysicalColumnID");
						if (sPhysColMapping == null) {
							sPhysColMapping = "";
						}
						nPhysicalColumnID = doc.createTextNode(sPhysColMapping);
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
