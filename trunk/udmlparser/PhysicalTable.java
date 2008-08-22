package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sPhysTblID = sTrimmedDS.substring(
											sPhysTbl.length(),
											iIndexAS).
											trim().replaceAll("\"", "");
		sPhysTblName = sTrimmedDS.substring(
											iIndexAS+4, 
											sDeclareStmt.indexOf(" HAVING")).
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
				if (line.indexOf(" AS ") != -1) {
					//FQPHYSCOLNAME
					vPhysColID.add(line.substring(0, line.indexOf(" AS ")).
												trim().replaceAll("\"", ""));
					//PHYSCOLNAME
					vPhysColName.add(line.substring(line.indexOf(" AS ")+4, 
													line.indexOf(" TYPE ")).
													trim().
													replaceAll("\"", ""));
					//DATA TYPE
					vPhysColDataType.add(line.substring(
												line.indexOf(" TYPE ")+6, 
												line.indexOf(" PRECISION ")).
												trim().replaceAll("\"", ""));
					//SIZE
					vPhysColSize.add(line.substring(
												line.indexOf(" PRECISION ")+11, 
												line.indexOf(" SCALE ")).
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
		Node nPhysicalTableID = xmldoc.createTextNode(sPhysTblID);
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

				nPhColID		= xmldoc.createTextNode(vPhysColID.get(i));
				nPhColName		= xmldoc.createTextNode(vPhysColName.get(i));
				nPhColDatatype	= xmldoc.createTextNode(vPhysColDataType.get(i));
				nPhColSize		= xmldoc.createTextNode(vPhysColSize.get(i));
				nPhColScale		= xmldoc.createTextNode(vPhysColScale.get(i));
				nPhColNullable	= xmldoc.createTextNode(vPhysColNullable.get(i));

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
}
/*
 * DECLARE TABLE <FQ table name> AS <table name> HAVING
 * (
 * <FQ phys col name1> TYPE <datatype> PRECISION <int> SCALE <int>  <[NOT] NULLABLE> COUNT <distinct count> LAST UPDATED <date>
 * PRIVILEGES ( READ),
 * <FQ phys col name2> TYPE <datatype> PRECISION <int> SCALE <int>  <[NOT] NULLABLE> COUNT <distinct count> LAST UPDATED <date>
 * PRIVILEGES ( READ)
 * ) DIAGRAM POSITION (<int>, <int>) ROW COUNT <records#> LAST UPDATED <date>
 * PRIVILEGES ( READ);
 */
