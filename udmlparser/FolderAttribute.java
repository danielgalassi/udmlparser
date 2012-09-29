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
	private String		sPressDispayName;
	private String		sPressDescription;
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
				
				//DISPLAY NAME
				if (line.indexOf("DISPLAY NAME ") != -1){
					sPressDispayName = line.trim().substring(
										line.indexOf("DISPLAY NAME ")+13,
										line.lastIndexOf(" ON")).
										trim().replaceAll("\"", "");
				}
				
				//DESCRIPTION
				if (line.indexOf("DESCRIPTION") != -1){
					sPressDescription = line.trim().substring(
							line.indexOf("{")+1,
							line.length()).
							trim().replaceAll("}", "");
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = brUDML.readLine().trim();
						sPressDescription += "\n";
						sPressDescription += line.trim().replaceAll("}", "");
					}
				}
					
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
		if (sPresColumnID == null) {
			sPresColumnID = "";
		}
		Node nPresentationColumnID = xmldoc.createTextNode(sPresColumnID);
		if (sPresColumnName == null) {
			sPresColumnName = "";
		}
		Node nPresentationColumnName = xmldoc.createTextNode(sPresColumnName);
		if (sPresColumnMappingID == null) {
			sPresColumnMappingID = "";
		}
		Node nPresentationColumnMappingID = xmldoc.createTextNode(sPresColumnMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (sPressDispayName == null) {
			sPressDispayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(sPressDispayName);
		if (sPressDescription == null) {
			sPressDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(sPressDescription);

		Element ePresentationColumn = xmldoc.createElement("PresentationColumn");
		Element ePresentationColumnID = xmldoc.createElement("PresentationColumnID");
		Element ePresentationColumnName = xmldoc.createElement("PresentationColumnName");
		Element ePresentationColumnMappingID = xmldoc.createElement("PresentationColumnMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");
		
		ePresentationColumnID.appendChild(nPresentationColumnID);
		ePresentationColumnName.appendChild(nPresentationColumnName);
		ePresentationColumnMappingID.appendChild(nPresentationColumnMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationColumn.appendChild(ePresentationColumnID);
		ePresentationColumn.appendChild(ePresentationColumnName);
		ePresentationColumn.appendChild(ePresentationColumnMappingID);
		ePresentationColumn.appendChild(ePresentationDisplayName);
		ePresentationColumn.appendChild(ePresentationDescription);

		Element ePresentationColumnAliasList = xmldoc.createElement("PresentationColumnAliasList");
		Element ePresentationColumnAlias = null;
		Node nCatalogFolderAlias = null;

		if(saPresColAliases != null)
			for (int i=0; i< saPresColAliases.length; i++) {
				ePresentationColumnAlias = xmldoc.createElement("PresentationColumnAlias");
				if (saPresColAliases[i] == null) {
					nCatalogFolderAlias = xmldoc.createTextNode("");
				} else {
					nCatalogFolderAlias = xmldoc.createTextNode(saPresColAliases[i]);
				}
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
