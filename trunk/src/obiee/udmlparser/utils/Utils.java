package obiee.udmlparser.utils;

import java.util.Vector;

/**
 * General Utilities class
 * @author danielgalassi@gmail.com
 *
 */
public class Utils {

	/**
	 * UDML calculation parser -identifies both, Logical and Physical columns
	 * @param sPrefixStr where to get the Subject Area prefix from
	 * @param sCalculation contains the expression to parse
	 * @param isDerived points out the type of expression to evaluate 
	 * 			(derived column or mapping calculation)
	 * @return Vector containing (logical or physical) column IDs
	 */
	public static Vector<String> CalculationParser (String sPrefixStr,
													String sCalculation, 
													boolean isDerived) {
		String sSAPrefix		= "\"" + sPrefixStr.substring(0,
										sPrefixStr.indexOf(".")) + "\"";
		String sExpr			= sCalculation.toString();
		String sRemainingExpr	= sCalculation.toString();
		
		Vector <String>	vColMapID = null;

		int iExpBegins	= 0;
		int iStPos		= 0;
		int iRelPos		= 0;
		int iEndPos		= 0;
		int iLoop		= 8;

		boolean bDuplicated;

		if(sExpr.indexOf(sSAPrefix) != -1)
			vColMapID = new Vector<String>();

		if (isDerived)
			iLoop = 4;
		while (sExpr.indexOf(sSAPrefix) != -1) {
			iStPos = sExpr.indexOf(sSAPrefix);
			//gets the first ocurrence of ."
			iRelPos = sExpr.indexOf(".\"");
			iEndPos = sExpr.indexOf(sSAPrefix) + sSAPrefix.length();
			//4 = derived calculation; 8 = LTS calculation
			for (int i=0; i<iLoop; i++) {
				sExpr = sExpr.substring(iRelPos);
				iRelPos = sExpr.indexOf("\"") + 1;
				iEndPos += iRelPos;
			}

			if (iEndPos > sRemainingExpr.length())
				iEndPos = sRemainingExpr.length();
			
			//prevents repeatedly added columns --begin
			bDuplicated = false;
			for(int j=0; j<vColMapID.size(); j++)
				if ((vColMapID.get(j)).equals(
						sRemainingExpr.substring(iStPos, iEndPos).
						replaceAll("\"", ""))) {
					bDuplicated = true;
					break;
				}
			if (!bDuplicated)
				vColMapID.add(sRemainingExpr.
						substring(iStPos, iEndPos).replaceAll("\"", ""));
			//prevents repeatedly added columns --end

			iExpBegins += iEndPos;
			sExpr = sCalculation.substring(iExpBegins);
			sRemainingExpr = sExpr.toString();
		}

		return vColMapID;
	}
}
