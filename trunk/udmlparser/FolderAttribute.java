package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Folder Attribute Parser class
 * @author dgalassi
 *
 */
public class FolderAttribute {

	private String		sPresentationColumnID;
	private String		sPresentationColumnName;
	private String		sPresentationColumnMappingID;
	private String[] 	saPresentationColumnAliases = null;

	public FolderAttribute (String sDeclareStmt, String sPresentationColumn, BufferedReader brUDML) {
		String line;
		sPresentationColumnID = sDeclareStmt.trim().substring(sPresentationColumn.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		sPresentationColumnName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4,sDeclareStmt.trim().indexOf(" LOGICAL ATTRIBUTE ", sDeclareStmt.trim().indexOf(" AS "))).trim().replaceAll("\"", "");
		if (sDeclareStmt.indexOf(" OVERRIDE LOGICAL NAME") == -1)
			sPresentationColumnMappingID = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" LOGICAL ATTRIBUTE ")+19).trim().replaceAll("\"", "");
		else
			sPresentationColumnMappingID = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" LOGICAL ATTRIBUTE ")+19, sDeclareStmt.indexOf(" OVERRIDE LOGICAL NAME")).trim().replace("\"", "");

		try {
			//ALIASES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
				if(line.indexOf("ALIASES (") != -1)
					saPresentationColumnAliases = line.substring(line.indexOf("ALIASES (")+9, line.lastIndexOf(")")).trim().replaceAll("\"", "").split(",");
			} while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1);
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Folder Attribute XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nPresentationColumnID = xmldoc.createTextNode(sPresentationColumnID);
		Node nPresentationColumnName = xmldoc.createTextNode(sPresentationColumnName);
		Node nPresentationColumnMappingID = xmldoc.createTextNode(sPresentationColumnMappingID);

		Element ePresentationColumn = xmldoc.createElement("PresentationColumn");
		Element ePresentationColumnID = xmldoc.createElement("PresentationColumnID");
		Element ePresentationColumnName = xmldoc.createElement("PresentationColumnName");
		Element ePresentationColumnMappingID = xmldoc.createElement("PresentationColumnMappingID");

		ePresentationColumnID.appendChild(nPresentationColumnID);
		ePresentationColumnName.appendChild(nPresentationColumnName);
		ePresentationColumnMappingID.appendChild(nPresentationColumnMappingID);

		ePresentationColumn.appendChild(ePresentationColumnID);
		ePresentationColumn.appendChild(ePresentationColumnName);
		ePresentationColumn.appendChild(ePresentationColumnMappingID);

		Element ePresentationColumnAliasList = xmldoc.createElement("PresentationColumnAliasList");
		Element ePresentationColumnAlias = null;
		Node nCatalogFolderAlias = null;

		if(saPresentationColumnAliases != null)
			for (int i=0; i< saPresentationColumnAliases.length; i++) {
				ePresentationColumnAlias = xmldoc.createElement("PresentationColumnAlias");
				nCatalogFolderAlias = xmldoc.createTextNode(saPresentationColumnAliases[i]);
				ePresentationColumnAlias.appendChild(nCatalogFolderAlias);
				ePresentationColumnAliasList.appendChild(ePresentationColumnAlias);
			}

		ePresentationColumn.appendChild(ePresentationColumnAliasList);
		return ePresentationColumn;
	}
}
/*
 * DECLARE FOLDER ATTRIBUTE <FQ Folder Attribute Name> AS <Folder Attribute Name> LOGICAL ATTRIBUTE  <FQ Logical Column Name>
 *  ALIASES (<Folder Attribute Alias>)
 *  PRIVILEGES ( <...>);
 */
