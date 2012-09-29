package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import utils.Utils;

/**
 * Logical Table Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalTable {

	private String			sLogTblID;
	private String			sLogTblName;
	private Vector <String>	vLogicalColumnID = null;
	private Vector <String>	vLogicalColumnName = null;
	private Vector <String>	vDerivedLogicalColumnExpression = null;
	private Vector <String>	vBiz2BizColumnMappingList = null;

	public LogicalTable (String sDeclareStmt,
						 String sLogTbl,
						 BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sLogTblID = sTrimmedDS.substring( sLogTbl.length(), iIndexAS).
												trim().replaceAll("\"", "");
		sLogTblName = sTrimmedDS.substring( iIndexAS + 4, 
											sTrimmedDS.indexOf(" HAVING")).
											trim().replaceAll("\"", "");

		try {
			line = brUDML.readLine();
			vLogicalColumnID = new Vector<String>();
			vLogicalColumnName = new Vector<String>();
			vDerivedLogicalColumnExpression = new Vector<String>();
			do {
				line = brUDML.readLine().trim();
				if (line.indexOf(" AS ") != -1) {
					//FQLOGCOLNAME
					vLogicalColumnID.add(line.substring(0, 
												line.indexOf(" AS ")).
												trim().replaceAll("\"", ""));
					//LOGCOLNAME
					if (line.indexOf(" {") != -1)
						vLogicalColumnName.add(line.substring(
											line.indexOf(" AS ")+4,
											line.indexOf(" {")).
											trim().replaceAll("\"", ""));
					else
						vLogicalColumnName.add(line.substring(
											line.indexOf(" AS ")+4).
											trim().replaceAll("\"", ""));
					//DERIVED EXPRESSION
					if (line.indexOf(" DERIVED") != -1)
						vDerivedLogicalColumnExpression.add(line.substring(
														line.indexOf(" {")+2, 
														line.indexOf("} ")).
														trim());
					else
						vDerivedLogicalColumnExpression.add("");
				}
			} while (line.indexOf("KEYS (") == -1 &&
					 line.indexOf("SOURCES (") == -1);

			//DISCARD SOURCES, DESCRIPTION AND PRIVILEGES
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
	 * Folder Attribute XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sLogTblID == null) {
			sLogTblID = "";
		}
		Node nLogicalTableID = xmldoc.createTextNode(sLogTblID);
		if (sLogTblName == null) {
			sLogTblName = "";
		}
		Node nLogicalTableName = xmldoc.createTextNode(sLogTblName);

		Element eLogicalTable = xmldoc.createElement("LogicalTable");
		Element eLogicalTableID = xmldoc.createElement("LogicalTableID");
		Element eLogicalTableName = xmldoc.createElement("LogicalTableName");

		eLogicalTableID.appendChild(nLogicalTableID);
		eLogicalTableName.appendChild(nLogicalTableName);

		eLogicalTable.appendChild(eLogicalTableID);
		eLogicalTable.appendChild(eLogicalTableName);

		Element eLogicalColumnList = xmldoc.createElement("LogicalColumnList");
		Element eLogicalColumn = null;
		Element eLogicalColumnID = null;
		Element eLogicalColumnName = null;
		Element eLogicalColumnDerivedExpression = null;

		Element eLogicalColumnDerivedMappingList = null;
		Element eBiz2BizColumnMappingID = null;
		Node nBiz2BizColumnMappingID = null;

		Node nLogicalColumnID = null;
		Node nLogicalColumnName = null;
		Node nLogicalColumnDerivedExpression = null;

		if(vLogicalColumnID != null)
			for (int i=0; i< vLogicalColumnID.size(); i++) {
				eLogicalColumn = xmldoc.createElement("LogicalColumn");
				eLogicalColumnID = xmldoc.createElement("LogicalColumnID");
				eLogicalColumnName = xmldoc.createElement("LogicalColumnName");
				eLogicalColumnDerivedExpression = xmldoc.createElement("LogicalColumnDerivedExpression");

				if (vLogicalColumnID.get(i) == null) {
					nLogicalColumnID = xmldoc.createTextNode("");
				} else {
					nLogicalColumnID = xmldoc.createTextNode(vLogicalColumnID.get(i));
				}

				if (vLogicalColumnName.get(i) == null) {
					nLogicalColumnName = xmldoc.createTextNode("");
				} else {
					nLogicalColumnName = xmldoc.createTextNode(vLogicalColumnName.get(i));
				}

				if ((vDerivedLogicalColumnExpression.get(i)).replaceAll("\"", "") == null) {
					nLogicalColumnDerivedExpression = xmldoc.createTextNode("");
				} else {
					nLogicalColumnDerivedExpression = xmldoc.createTextNode((vDerivedLogicalColumnExpression.get(i)).replaceAll("\"", ""));
				}

				eLogicalColumnID.appendChild(nLogicalColumnID);
				eLogicalColumnName.appendChild(nLogicalColumnName);
				eLogicalColumnDerivedExpression.appendChild(nLogicalColumnDerivedExpression);

				eLogicalColumn.appendChild(eLogicalColumnID);
				eLogicalColumn.appendChild(eLogicalColumnName);
				eLogicalColumn.appendChild(eLogicalColumnDerivedExpression);

				eLogicalColumnDerivedMappingList = xmldoc.createElement("LogicalColumnDerivedMappingList");
				vBiz2BizColumnMappingList = Utils.CalculationParser(sLogTblID, vDerivedLogicalColumnExpression.get(i), true);
				if(vBiz2BizColumnMappingList != null) {
					for(int j=0; j< vBiz2BizColumnMappingList.size(); j++) {
						eBiz2BizColumnMappingID = xmldoc.createElement("LogicalColumnDerivedMappingID");
						if (vBiz2BizColumnMappingList.get(j) == null) {
							nBiz2BizColumnMappingID = xmldoc.createTextNode("");
						} else {
							nBiz2BizColumnMappingID = xmldoc.createTextNode(vBiz2BizColumnMappingList.get(j));
						}
						eBiz2BizColumnMappingID.appendChild(nBiz2BizColumnMappingID);
						eLogicalColumnDerivedMappingList.appendChild(eBiz2BizColumnMappingID);
					}
					eLogicalColumn.appendChild(eLogicalColumnDerivedMappingList);
				}
				eLogicalColumn.appendChild(eLogicalColumnDerivedMappingList);

				eLogicalColumnList.appendChild(eLogicalColumn);
			}

		eLogicalTable.appendChild(eLogicalColumnList);
		return eLogicalTable;
	}
}
/*
 * DECLARE LOGICAL TABLE <FQ logical tbl name> AS <logical tbl name> HAVING
 * (
 * <FQ logical column name> AS <logical column (fantasy) name>
 * PRIVILEGES ( READ),
 * <FQ logical column name> AS <logical column (fantasy) name>
 * PRIVILEGES ( READ) )
 * KEYS (
 * <FQ key column (logical column)> )
 * SOURCES (
 * <FQ logical table source> ) DIAGRAM POSITION (<int>, <int>)
 * PRIVILEGES ( READ);
 */
