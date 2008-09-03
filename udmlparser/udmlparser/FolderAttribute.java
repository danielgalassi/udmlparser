package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Folder Attribute Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class FolderAttribute {

	private String		sPresColumnID;
	private String		sPresColumnName;
	private String		sPresColumnMappingID;
	private String[] 	saPresColAliases = null;

	public FolderAttribute (String sDeclareStmt, 
							String sPresColumn,
							BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		int iIndexLA = sTrimmedDS.indexOf(" LOGICAL ATTRIBUTE ");
		sPresColumnID = sTrimmedDS.substring(sPresColumn.length(), iIndexAS).
												trim().replaceAll("\"", "");
		sPresColumnName = sTrimmedDS.substring(iIndexAS+4, 
						sTrimmedDS.indexOf( " LOGICAL ATTRIBUTE ",iIndexAS)).
						trim().replaceAll("\"", "");

		if (sTrimmedDS.indexOf(" OVERRIDE LOGICAL NAME") == -1)
			sPresColumnMappingID = sTrimmedDS.substring(iIndexLA+19).
												trim().replaceAll("\"", "");
		else
			sPresColumnMappingID = sTrimmedDS.substring(iIndexLA+19, 
								sTrimmedDS.indexOf(" OVERRIDE LOGICAL NAME")).
								trim().replace("\"", "");

		try {
			//ALIASES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
				if(line.indexOf("ALIASES (") != -1)
					saPresColAliases = line.substring(
										line.indexOf("ALIASES (")+9, 
										line.lastIndexOf(")")).
										trim().replaceAll("\"", "").split(",");
			} while (line.indexOf("PRIVILEGES") == -1 &&
					 line.indexOf(";") == -1);

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
		Node nPresentationColumnID = xmldoc.createTextNode(sPresColumnID);
		Node nPresentationColumnName = xmldoc.createTextNode(sPresColumnName);
		Node nPresentationColumnMappingID = xmldoc.createTextNode(sPresColumnMappingID);

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

		if(saPresColAliases != null)
			for (int i=0; i< saPresColAliases.length; i++) {
				ePresentationColumnAlias = xmldoc.createElement("PresentationColumnAlias");
				nCatalogFolderAlias = xmldoc.createTextNode(saPresColAliases[i]);
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
