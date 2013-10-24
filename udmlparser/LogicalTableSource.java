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

	private String			logicalTableSourceID;
	private String			logicalTableSourceName;
	private Vector <String>	vLogicalColumnID;
	private Vector <String>	vLogicalColumnCalculation;

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
				vLogicalColumnID = new Vector<String>();
				vLogicalColumnCalculation = new Vector<String>();
				do {
					line = udml.readLine().trim();
					if (line.indexOf(" AS ") != -1 && 
							line.indexOf(" CAST") == -1) {
						vLogicalColumnID.add(line.substring(
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
						vLogicalColumnCalculation.add(line.substring(
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
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		String sTemp = null;
		if (logicalTableSourceID == null) {
			logicalTableSourceID = "";
		}
		Node nLogicalTableSourceID = xmldoc.createTextNode(logicalTableSourceID);
		if (logicalTableSourceName == null) {
			logicalTableSourceName = "";
		}
		Node nLogicalTableSourceName = xmldoc.createTextNode(logicalTableSourceName);

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
				vPhysicalColumnMappingList = Utils.CalculationParser(logicalTableSourceID, vLogicalColumnCalculation.get(i), false);
				if(vPhysicalColumnMappingList != null) {
					for (String sPhysColMapping : vPhysicalColumnMappingList) {
						ePhysicalColumnID = xmldoc.createElement("PhysicalColumnID");
						if (sPhysColMapping == null) {
							nPhysicalColumnID = xmldoc.createTextNode("");
						} else {
							nPhysicalColumnID = xmldoc.createTextNode(sPhysColMapping);
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
