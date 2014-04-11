package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
//import java.util.regex.*;

//import com.sun.org.apache.xalan.internal.xsltc.dom.MatchingIterator;

/**
 * Physical Table Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class PhysicalTable {

	private String			physicalTableID;
	private String			physicalTableName;
	private String			physicalTableSource;
	private boolean			isPhysicalAlias;
	private Vector <String>	physicalumnColumnIDs;
	private Vector <String>	physicalumnColumnNames;
	private Vector <String>	physicalColumnDataTypes;
	private Vector <String>	physicalColumnSizes;
	private Vector <String>	physicalColumnScales;
	private Vector <String>	physicalColumnNullables;

	public PhysicalTable (	String declare,
							String physicalTable,
							BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexColName; 
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		physicalTableID = trimmedDeclareStatement.substring(
											physicalTable.length(),
											iIndexAS).
											trim().replaceAll("\"", "");
		int iIndexHaving = declare.indexOf(" HAVING");
		if (iIndexHaving != -1)
		physicalTableName = trimmedDeclareStatement.substring(
											iIndexAS+4, 
											iIndexHaving).
											trim().replaceAll("\"", "");
		isPhysicalAlias = false;

		try {
			line = udml.readLine();

			physicalumnColumnIDs = new Vector<String>();
			physicalumnColumnNames = new Vector<String>();
			physicalColumnDataTypes = new Vector<String>();
			physicalColumnSizes = new Vector<String>();
			physicalColumnScales = new Vector<String>();
			physicalColumnNullables = new Vector<String>();

			do {
				line = udml.readLine().trim().replaceAll("\"", "");

				//This is a marker used in Opaque Views.
				if (line.indexOf("TABLE TYPE SELECT DATABASE MAP") != -1)
					do {
						line = udml.readLine().trim().replaceAll("\"", "");
					} while (!(line.indexOf("PRIVILEGES (") != -1 && line.indexOf(";") != -1));

				if (line.indexOf(" AS ") != -1 &&
					line.indexOf(" TYPE ") != -1) {
					//FQPHYSCOLNAME
					physicalumnColumnIDs.add(line.substring(0, line.indexOf(" AS ")).
												trim().replaceAll("\"", ""));
					//PHYSCOLNAME
					iIndexColName = line.indexOf(" TYPE ");
					if (line.indexOf(" EXTERNAL ") != -1 &&
						line.indexOf(" EXTERNAL ") < iIndexColName) {
						iIndexColName = line.indexOf(" EXTERNAL ");
					}
					
					if (specialCaseTYPE(line)) {
						physicalumnColumnNames.add("TYPE");
					}
					else {
						String str = line.substring(line.indexOf(" AS ")+4, 
								iIndexColName).trim().
								replaceAll("\"", "");
						physicalumnColumnNames.add(str);
					}
					
					
					
					//DATA TYPE
					physicalColumnDataTypes.add(line.substring(
												line.indexOf(" TYPE ")+6, 
												line.indexOf(" PRECISION ")).
												trim().replaceAll("\"", ""));
					//SIZE
					int iIndexSCALE = line.indexOf(" SCALE ");
					int iIndexPRECISION = line.indexOf(" PRECISION ")+11;
					
					if (specialCaseSCALE(line)) {
						physicalColumnSizes.add("SCALE");
					}
					else
					physicalColumnSizes.add(line.substring(
												iIndexPRECISION, 
												iIndexSCALE).
												trim().replaceAll("\"", ""));
					//SCALE & NULLABLE
					if (line.indexOf(" NOT NULLABLE") != -1) {
						physicalColumnScales.add(line.substring(
												line.indexOf(" SCALE ")+7, 
												line.indexOf(" NOT NULLABLE")).
												trim().replaceAll("\"", ""));
						physicalColumnNullables.add("NOT NULLABLE");
					}
					else {
						physicalColumnScales.add(line.substring(
												line.indexOf(" SCALE ")+7, 
												line.indexOf(" NULLABLE")).
												trim().replaceAll("\"", ""));
						physicalColumnNullables.add("NULLABLE");
					}
				}

				if (line.indexOf(")") == 0 && 
					line.indexOf(" SOURCE ") != -1) {
					physicalTableSource = line.substring(line.indexOf(" SOURCE ")+8);
					isPhysicalAlias = true;
				}
			} while (!( line.indexOf("PRIVILEGES (") != -1 && 
						line.indexOf(";") != -1));

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line		= null;
	}

	/**
	 * Physical Table XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (physicalTableID == null) {
			physicalTableID = "";
		}
		Node nPhysicalTableID = xmldoc.createTextNode(physicalTableID);
		if (physicalTableName == null) {
			physicalTableName = "";
		}
		Node nPhysicalTableName = xmldoc.createTextNode(physicalTableName);

		Element ePhysicalTable = xmldoc.createElement("PhysicalTable");
		Element ePhysicalTableID = xmldoc.createElement("PhysicalTableID");
		Element ePhysicalTableName = xmldoc.createElement("PhysicalTableName");

		ePhysicalTable.setAttribute("isAlias", "false");
		
		if (isPhysicalAlias) {
			ePhysicalTable.setAttribute("isAlias", "true");
			ePhysicalTable.setAttribute("reference", physicalTableSource);
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

		if(physicalumnColumnIDs != null)
			for (int i=0; i< physicalumnColumnIDs.size(); i++) {
				ePhysicalColumn = xmldoc.createElement("PhysicalColumn");

				ePhColID		= xmldoc.createElement("PhysicalColumnID");
				ePhColName		= xmldoc.createElement("PhysicalColumnName");
				ePhColDatatype	= xmldoc.createElement("PhysicalColumnDatatype");
				ePhColSize		= xmldoc.createElement("PhysicalColumnSize");
				ePhColScale		= xmldoc.createElement("PhysicalColumnScale");
				ePhColNullable	= xmldoc.createElement("PhysicalColumnNullable");

				if (physicalumnColumnIDs.get(i) == null) {
					nPhColID = xmldoc.createTextNode("");
				} else {
					nPhColID = xmldoc.createTextNode(physicalumnColumnIDs.get(i));
				}

				if (physicalumnColumnNames.get(i) == null) {
					nPhColName = xmldoc.createTextNode("");
				} else {
					nPhColName = xmldoc.createTextNode(physicalumnColumnNames.get(i));
				}

				if (physicalColumnDataTypes.get(i) == null) {
					nPhColDatatype = xmldoc.createTextNode("");
				} else {
					nPhColDatatype = xmldoc.createTextNode(physicalColumnDataTypes.get(i));
				}

				if (physicalColumnSizes.get(i) == null) {
					nPhColSize = xmldoc.createTextNode("");
				} else {
					nPhColSize = xmldoc.createTextNode(physicalColumnSizes.get(i));
				}

				if (physicalColumnScales.get(i) == null) {
					nPhColScale = xmldoc.createTextNode("");
				} else {
					nPhColScale = xmldoc.createTextNode(physicalColumnScales.get(i));
				}

				if (physicalColumnNullables.get(i) == null) {
					nPhColNullable = xmldoc.createTextNode("");
				} else {
					nPhColNullable = xmldoc.createTextNode(physicalColumnNullables.get(i));
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
