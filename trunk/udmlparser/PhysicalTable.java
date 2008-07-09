package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Physical Table Parser class
 * @author dgalassi
 *
 */
public class PhysicalTable {

	private String			sPhysicalTableID;
	private String			sPhysicalTableName;
	private String			sPhysicalTableSource;
	private boolean			bIsPhysicalAlias;
	private Vector <String>	vPhysicalColumnID;
	private Vector <String>	vPhysicalColumnName;
	private Vector <String>	vPhysicalColumnDataType;
	private Vector <String>	vPhysicalColumnSize;
	private Vector <String>	vPhysicalColumnScale;
	private Vector <String>	vPhysicalColumnNullable;

	public PhysicalTable (String sDeclareStmt, String sPhysicalTable, BufferedReader brUDML) {
		String line;
		sPhysicalTableID = sDeclareStmt.trim().substring(sPhysicalTable.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		sPhysicalTableName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4, sDeclareStmt.indexOf(" HAVING")).trim().replaceAll("\"", "");
		bIsPhysicalAlias = false;

		try {
			line = brUDML.readLine();

			vPhysicalColumnID = new Vector<String>();
			vPhysicalColumnName = new Vector<String>();
			vPhysicalColumnDataType = new Vector<String>();
			vPhysicalColumnSize = new Vector<String>();
			vPhysicalColumnScale = new Vector<String>();
			vPhysicalColumnNullable = new Vector<String>();

			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
				if (line.indexOf(" AS ") != -1) {
					//FQPHYSCOLNAME
					vPhysicalColumnID.add(line.substring(0, line.indexOf(" AS ")).trim().replaceAll("\"", ""));
					//PHYSCOLNAME
					vPhysicalColumnName.add(line.substring(line.indexOf(" AS ")+4, line.indexOf(" TYPE ")).trim().replaceAll("\"", ""));
					//DATA TYPE
					vPhysicalColumnDataType.add(line.substring(line.indexOf(" TYPE ")+6, line.indexOf(" PRECISION ")).trim().replaceAll("\"", ""));
					//SIZE
					vPhysicalColumnSize.add(line.substring(line.indexOf(" PRECISION ")+11, line.indexOf(" SCALE ")).trim().replaceAll("\"", ""));
					//SCALE & NULLABLE
					if(line.indexOf(" NOT NULLABLE") != -1) {
						vPhysicalColumnScale.add(line.substring(line.indexOf(" SCALE ")+7, line.indexOf(" NOT NULLABLE")).trim().replaceAll("\"", ""));
						vPhysicalColumnNullable.add("NOT NULLABLE");
					}
					else {
						vPhysicalColumnScale.add(line.substring(line.indexOf(" SCALE ")+7, line.indexOf(" NULLABLE")).trim().replaceAll("\"", ""));
						vPhysicalColumnNullable.add("NULLABLE");
					}
				}

				if (line.indexOf(")") == 0 && line.indexOf(" SOURCE ") != -1) {
					sPhysicalTableSource = line.substring(line.indexOf(" SOURCE ")+8);
					bIsPhysicalAlias = true;
				}
			} while (!(line.indexOf("PRIVILEGES (") != -1 && line.indexOf(";") != -1));

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Physical Table XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nPhysicalTableID = xmldoc.createTextNode(sPhysicalTableID);
		Node nPhysicalTableName = xmldoc.createTextNode(sPhysicalTableName);

		Element ePhysicalTable = xmldoc.createElement("PhysicalTable");
		Element ePhysicalTableID = xmldoc.createElement("PhysicalTableID");
		Element ePhysicalTableName = xmldoc.createElement("PhysicalTableName");

		ePhysicalTable.setAttribute("isAlias", "false");
		
		if (bIsPhysicalAlias) {
			ePhysicalTable.setAttribute("isAlias", "true");
			ePhysicalTable.setAttribute("reference", sPhysicalTableSource);
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

		if(vPhysicalColumnID != null)
			for (int i=0; i< vPhysicalColumnID.size(); i++) {
				ePhysicalColumn = xmldoc.createElement("PhysicalColumn");

				ePhColID		= xmldoc.createElement("PhysicalColumnID");
				ePhColName		= xmldoc.createElement("PhysicalColumnName");
				ePhColDatatype	= xmldoc.createElement("PhysicalColumnDatatype");
				ePhColSize		= xmldoc.createElement("PhysicalColumnSize");
				ePhColScale		= xmldoc.createElement("PhysicalColumnScale");
				ePhColNullable	= xmldoc.createElement("PhysicalColumnNullable");

				nPhColID		= xmldoc.createTextNode(vPhysicalColumnID.get(i));
				nPhColName		= xmldoc.createTextNode(vPhysicalColumnName.get(i));
				nPhColDatatype	= xmldoc.createTextNode(vPhysicalColumnDataType.get(i));
				nPhColSize		= xmldoc.createTextNode(vPhysicalColumnSize.get(i));
				nPhColScale		= xmldoc.createTextNode(vPhysicalColumnScale.get(i));
				nPhColNullable	= xmldoc.createTextNode(vPhysicalColumnNullable.get(i));

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
