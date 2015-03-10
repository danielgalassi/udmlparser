package obiee.udmlparser.parser;

import java.util.Vector;

import metadata.Repository;

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
public class PhysicalTable implements UDMLObject {

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

	public PhysicalTable (String declare, String physicalTable, Repository udml) {
		String line;
		String header = declare.trim();
		int indexColName; 
		int indexAS = header.indexOf(" AS ");
		physicalTableID = header.substring(physicalTable.length(), indexAS).trim().replaceAll("\"", "");
		int indexHaving = declare.indexOf(" HAVING");
		if (indexHaving != -1) {
			physicalTableName = header.substring(indexAS+4, indexHaving).trim().replaceAll("\"", "");
		}
		isPhysicalAlias = false;

		line = udml.nextLine();

		physicalumnColumnIDs = new Vector<String>();
		physicalumnColumnNames = new Vector<String>();
		physicalColumnDataTypes = new Vector<String>();
		physicalColumnSizes = new Vector<String>();
		physicalColumnScales = new Vector<String>();
		physicalColumnNullables = new Vector<String>();

		do {
			line = udml.nextLine().trim().replaceAll("\"", "");

			//This is a marker used in Opaque Views.
			if (line.indexOf("TABLE TYPE SELECT DATABASE MAP") != -1)
				do {
					line = udml.nextLine().trim().replaceAll("\"", "");
				} while (!(line.indexOf("PRIVILEGES (") != -1 && line.indexOf(";") != -1));

			if (line.contains(" AS ") && line.contains(" TYPE ")) {
				//FQPHYSCOLNAME
				physicalumnColumnIDs.add(line.substring(0, line.indexOf(" AS ")).
						trim().replaceAll("\"", ""));
				//PHYSCOLNAME
				indexColName = line.indexOf(" TYPE ");
				if (line.contains(" EXTERNAL ") && line.indexOf(" EXTERNAL ") < indexColName) {
					indexColName = line.indexOf(" EXTERNAL ");
				}

				if (specialCaseTYPE(line)) {
					physicalumnColumnNames.add("TYPE");
				}
				else {
					String str = line.substring(line.indexOf(" AS ")+4, indexColName).trim().replaceAll("\"", "");
					physicalumnColumnNames.add(str);
				}

				//DATA TYPE
				physicalColumnDataTypes.add(line.substring(line.indexOf(" TYPE ")+6, line.indexOf(" PRECISION ")).trim().replaceAll("\"", ""));
				//SIZE
				int indexSCALE = line.indexOf(" SCALE ");
				int indexPRECISION = line.indexOf(" PRECISION ")+11;

				if (specialCaseSCALE(line)) {
					physicalColumnSizes.add("SCALE");
				}
				else {
					physicalColumnSizes.add(line.substring(indexPRECISION, indexSCALE).trim().replaceAll("\"", ""));
				}
				//SCALE & NULLABLE
				if (line.contains(" NOT NULLABLE")) {
					physicalColumnScales.add(line.substring(line.indexOf(" SCALE ")+7, line.indexOf(" NOT NULLABLE")).trim().replaceAll("\"", ""));
					physicalColumnNullables.add("NOT NULLABLE");
				}
				else {
					physicalColumnScales.add(line.substring(line.indexOf(" SCALE ")+7, line.indexOf(" NULLABLE")).trim().replaceAll("\"", ""));
					physicalColumnNullables.add("NULLABLE");
				}
			}

			if (line.indexOf(")") == 0 && line.contains(" SOURCE ")) {
				physicalTableSource = line.substring(line.indexOf(" SOURCE ")+8);
				isPhysicalAlias = true;
			}
		} while (!( line.indexOf("PRIVILEGES (") != -1 && line.indexOf(";") != -1));
	}

	/**
	 * Physical Table XML serializer
	 * @param doc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document doc) {
		if (physicalTableID == null) {
			physicalTableID = "";
		}
		Node nPhysicalTableID = doc.createTextNode(physicalTableID);
		if (physicalTableName == null) {
			physicalTableName = "";
		}
		Node nPhysicalTableName = doc.createTextNode(physicalTableName);

		Element ePhysicalTable = doc.createElement("PhysicalTable");
		Element ePhysicalTableID = doc.createElement("PhysicalTableID");
		Element ePhysicalTableName = doc.createElement("PhysicalTableName");

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

		Element ePhysicalColumnList = doc.createElement("PhysicalColumnList");
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
				ePhysicalColumn = doc.createElement("PhysicalColumn");

				ePhColID		= doc.createElement("PhysicalColumnID");
				ePhColName		= doc.createElement("PhysicalColumnName");
				ePhColDatatype	= doc.createElement("PhysicalColumnDatatype");
				ePhColSize		= doc.createElement("PhysicalColumnSize");
				ePhColScale		= doc.createElement("PhysicalColumnScale");
				ePhColNullable	= doc.createElement("PhysicalColumnNullable");

				if (physicalumnColumnIDs.get(i) == null) {
					nPhColID = doc.createTextNode("");
				} else {
					nPhColID = doc.createTextNode(physicalumnColumnIDs.get(i));
				}

				if (physicalumnColumnNames.get(i) == null) {
					nPhColName = doc.createTextNode("");
				} else {
					nPhColName = doc.createTextNode(physicalumnColumnNames.get(i));
				}

				if (physicalColumnDataTypes.get(i) == null) {
					nPhColDatatype = doc.createTextNode("");
				} else {
					nPhColDatatype = doc.createTextNode(physicalColumnDataTypes.get(i));
				}

				if (physicalColumnSizes.get(i) == null) {
					nPhColSize = doc.createTextNode("");
				} else {
					nPhColSize = doc.createTextNode(physicalColumnSizes.get(i));
				}

				if (physicalColumnScales.get(i) == null) {
					nPhColScale = doc.createTextNode("");
				} else {
					nPhColScale = doc.createTextNode(physicalColumnScales.get(i));
				}

				if (physicalColumnNullables.get(i) == null) {
					nPhColNullable = doc.createTextNode("");
				} else {
					nPhColNullable = doc.createTextNode(physicalColumnNullables.get(i));
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
