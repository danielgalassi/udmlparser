package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;
//import java.util.regex.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//import com.sun.org.apache.xalan.internal.xsltc.dom.MatchingIterator;

/**
 * Physical Table Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class PhysicalTable {

	private String			sPhysTblID;
	private String			sPhysTblName;
	private String			sPhysTblSrc;
	private boolean			bIsPhysicalAlias;
	private Vector <String>	vPhysColID;
	private Vector <String>	vPhysColName;
	private Vector <String>	vPhysColDataType;
	private Vector <String>	vPhysColSize;
	private Vector <String>	vPhysColScale;
	private Vector <String>	vPhysColNullable;

	public PhysicalTable (	String sDeclareStmt,
							String sPhysTbl,
							BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexColName; 
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sPhysTblID = sTrimmedDS.substring(
											sPhysTbl.length(),
											iIndexAS).
											trim().replaceAll("\"", "");
		int iIndexHaving = sDeclareStmt.indexOf(" HAVING");
		if (iIndexHaving != -1)
		sPhysTblName = sTrimmedDS.substring(
											iIndexAS+4, 
											iIndexHaving).
											trim().replaceAll("\"", "");
		bIsPhysicalAlias = false;

		try {
			line = brUDML.readLine();

			vPhysColID = new Vector<String>();
			vPhysColName = new Vector<String>();
			vPhysColDataType = new Vector<String>();
			vPhysColSize = new Vector<String>();
			vPhysColScale = new Vector<String>();
			vPhysColNullable = new Vector<String>();

			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");

				//This is a marker used in Opaque Views.
				if (line.indexOf("TABLE TYPE SELECT DATABASE MAP") != -1)
					do {
						line = brUDML.readLine().trim().replaceAll("\"", "");
					} while (!(line.indexOf("PRIVILEGES (") != -1 && line.indexOf(";") != -1));

				if (line.indexOf(" AS ") != -1 &&
					line.indexOf(" TYPE ") != -1) {
					//FQPHYSCOLNAME
					vPhysColID.add(line.substring(0, line.indexOf(" AS ")).
												trim().replaceAll("\"", ""));
					//PHYSCOLNAME
					iIndexColName = line.indexOf(" TYPE ");
					if (line.indexOf(" EXTERNAL ") != -1 &&
						line.indexOf(" EXTERNAL ") < iIndexColName){
						iIndexColName = line.indexOf(" EXTERNAL ");
					}
					
					if (specialCaseTYPE(line))
					{
						vPhysColName.add("TYPE");
					}
					else
					{
						String str = line.substring(line.indexOf(" AS ")+4, 
								iIndexColName).trim().
								replaceAll("\"", "");
						vPhysColName.add(str);
					}
					
					
					
					//DATA TYPE
					vPhysColDataType.add(line.substring(
												line.indexOf(" TYPE ")+6, 
												line.indexOf(" PRECISION ")).
												trim().replaceAll("\"", ""));
					//SIZE
					int iIndexSCALE = line.indexOf(" SCALE ");
					int iIndexPRECISION = line.indexOf(" PRECISION ")+11;
					
					if (specialCaseSCALE(line))
					{
						vPhysColSize.add("SCALE");
					}
					else
					vPhysColSize.add(line.substring(
												iIndexPRECISION, 
												iIndexSCALE).
												trim().replaceAll("\"", ""));
					//SCALE & NULLABLE
					if (line.indexOf(" NOT NULLABLE") != -1) {
						vPhysColScale.add(line.substring(
												line.indexOf(" SCALE ")+7, 
												line.indexOf(" NOT NULLABLE")).
												trim().replaceAll("\"", ""));
						vPhysColNullable.add("NOT NULLABLE");
					}
					else {
						vPhysColScale.add(line.substring(
												line.indexOf(" SCALE ")+7, 
												line.indexOf(" NULLABLE")).
												trim().replaceAll("\"", ""));
						vPhysColNullable.add("NULLABLE");
					}
				}

				if (line.indexOf(")") == 0 && 
					line.indexOf(" SOURCE ") != -1) {
					sPhysTblSrc = line.substring(line.indexOf(" SOURCE ")+8);
					bIsPhysicalAlias = true;
				}
			} while (!( line.indexOf("PRIVILEGES (") != -1 && 
						line.indexOf(";") != -1));

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Physical Table XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sPhysTblID == null) {
			sPhysTblID = "";
		}
		Node nPhysicalTableID = xmldoc.createTextNode(sPhysTblID);
		if (sPhysTblName == null) {
			sPhysTblName = "";
		}
		Node nPhysicalTableName = xmldoc.createTextNode(sPhysTblName);

		Element ePhysicalTable = xmldoc.createElement("PhysicalTable");
		Element ePhysicalTableID = xmldoc.createElement("PhysicalTableID");
		Element ePhysicalTableName = xmldoc.createElement("PhysicalTableName");

		ePhysicalTable.setAttribute("isAlias", "false");
		
		if (bIsPhysicalAlias) {
			ePhysicalTable.setAttribute("isAlias", "true");
			ePhysicalTable.setAttribute("reference", sPhysTblSrc);
		}
		else
			ePhysicalTable.setAttribute("reference", "");

		ePhysicalTableID.appendChild(nPhysicalTableID);
		ePhysicalTableName.appendChild(nPhysicalTableName);

		ePhysicalTable.appendChild(ePhysicalTableID);
		ePhysicalTable.appendChild(ePhysicalTableName);

		Element ePhysicalColumnList = xmldoc.createElement("PhysicalColumnList");
		Element ePhysicalColumn = null;

		Element	ePhColID		= null;
		Node	nPhColID		= null;
		Element	ePhColName		= null;
		Node	nPhColName		= null;
		Element	ePhColDatatype	= null;
		Node	nPhColDatatype	= null;
		Element	ePhColSize		= null;
		Node	nPhColSize		= null;
		Element	ePhColScale		= null;
		Node	nPhColScale		= null;
		Element	ePhColNullable	= null;
		Node	nPhColNullable	= null;

		if(vPhysColID != null)
			for (int i=0; i< vPhysColID.size(); i++) {
				ePhysicalColumn = xmldoc.createElement("PhysicalColumn");

				ePhColID		= xmldoc.createElement("PhysicalColumnID");
				ePhColName		= xmldoc.createElement("PhysicalColumnName");
				ePhColDatatype	= xmldoc.createElement("PhysicalColumnDatatype");
				ePhColSize		= xmldoc.createElement("PhysicalColumnSize");
				ePhColScale		= xmldoc.createElement("PhysicalColumnScale");
				ePhColNullable	= xmldoc.createElement("PhysicalColumnNullable");

				if (vPhysColID.get(i) == null) {
					nPhColID = xmldoc.createTextNode("");
				} else {
					nPhColID = xmldoc.createTextNode(vPhysColID.get(i));
				}

				if (vPhysColName.get(i) == null) {
					nPhColName = xmldoc.createTextNode("");
				} else {
					nPhColName = xmldoc.createTextNode(vPhysColName.get(i));
				}

				if (vPhysColDataType.get(i) == null) {
					nPhColDatatype = xmldoc.createTextNode("");
				} else {
					nPhColDatatype = xmldoc.createTextNode(vPhysColDataType.get(i));
				}

				if (vPhysColSize.get(i) == null) {
					nPhColSize = xmldoc.createTextNode("");
				} else {
					nPhColSize = xmldoc.createTextNode(vPhysColSize.get(i));
				}

				if (vPhysColScale.get(i) == null) {
					nPhColScale = xmldoc.createTextNode("");
				} else {
					nPhColScale = xmldoc.createTextNode(vPhysColScale.get(i));
				}

				if (vPhysColNullable.get(i) == null) {
					nPhColNullable = xmldoc.createTextNode("");
				} else {
					nPhColNullable = xmldoc.createTextNode(vPhysColNullable.get(i));
				}

				ePhColID.appendChild(nPhColID);
				ePhColName.appendChild(nPhColName);
				ePhColDatatype.appendChild(nPhColDatatype);
				ePhColSize.appendChild(nPhColSize);
				ePhColScale.appendChild(nPhColScale);
				ePhColNullable.appendChild(nPhColNullable);

				ePhysicalColumn.appendChild(ePhColID);
				ePhysicalColumn.appendChild(ePhColName);
				ePhysicalColumn.appendChild(ePhColDatatype);
				ePhysicalColumn.appendChild(ePhColSize);
				ePhysicalColumn.appendChild(ePhColScale);
				ePhysicalColumn.appendChild(ePhColNullable);

				ePhysicalColumnList.appendChild(ePhysicalColumn);
			}

		ePhysicalTable.appendChild(ePhysicalColumnList);
		return ePhysicalTable;
	}
	
private boolean specialCaseTYPE(String inputStr)
{
	int first = inputStr.indexOf(" TYPE ");
	String last = inputStr.substring(first+5, inputStr.length());

	if (last.indexOf(" TYPE ") != -1) return true;

	return false;
}

private boolean specialCaseSCALE(String inputStr)
{
	int first = inputStr.indexOf(" SCALE ");
	String last = inputStr.substring(first+6, inputStr.length());

	if (last.indexOf(" SCALE ") != -1) return true;

	return false;
}
}
/*
 * DECLARE TABLE <FQ table name> AS <table name> HAVING
 * (
 * <FQ phys col name1> AS <phys col name1> TYPE <datatype> PRECISION <int> SCALE <int>  <[NOT] NULLABLE> COUNT <distinct count> LAST UPDATED <date>
 * PRIVILEGES ( READ),
 * <FQ phys col name2> AS <phys col name2>  TYPE <datatype> PRECISION <int> SCALE <int>  <[NOT] NULLABLE> COUNT <distinct count> LAST UPDATED <date>
 * PRIVILEGES ( READ)
 * ) DIAGRAM POSITION (<int>, <int>) ROW COUNT <records#> LAST UPDATED <date>
 * PRIVILEGES ( READ);
 */
