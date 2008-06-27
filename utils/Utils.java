package utils;

import java.util.Vector;

public class Utils {

	/**
	 * UDML calculation parser (identifies both, Logical and Physical columns).
	 * @param sPrefixString where to get the Subject Area prefix from
	 * @param sCalculation contains the expression to parse
	 * @param isDerived points out the type of expression to evaluate (derived column or mapping calculation)
	 * @return Vector containing (logical or physical) column IDs
	 */
	public static Vector CalculationParser(String sPrefixString, String sCalculation, boolean isDerived) {
		String sSAPrefix = "\"" + sPrefixString.substring(0, sPrefixString.indexOf(".")) + "\"";
		String sExpression = sCalculation.toString();
		String sRemainingExpr = sCalculation.toString();

		Vector vColumnMappingID = null;

		int iExpBegins	= 0;
		int iStPos		= 0;
		int iRelPos		= 0;
		int iFinalPos	= 0;
		int iLoop		= 8;

		boolean bDuplicated;

		if(sExpression.indexOf(sSAPrefix) != -1)
			vColumnMappingID = new Vector();

		if (isDerived)
			iLoop = 4;

		while (sExpression.indexOf(sSAPrefix) != -1) {
			iStPos = sExpression.indexOf(sSAPrefix);
			//gets the first ocurrence of ."
			iRelPos = sExpression.indexOf(".\"");
			iFinalPos = sExpression.indexOf(sSAPrefix) + sSAPrefix.length();
			//4 = derived calculation; 8 = LTS calculation
			for (int i=0; i<iLoop; i++) {
				sExpression = sExpression.substring(iRelPos);
				iRelPos = sExpression.indexOf("\"") + 1;
				iFinalPos += iRelPos;
			}

			//prevents repeatedly added columns --begin
			bDuplicated = false;
			for(int j=0; j<vColumnMappingID.size(); j++)
				if (((String)vColumnMappingID.get(j)).equals(sRemainingExpr.substring(iStPos, iFinalPos).replaceAll("\"", ""))) {
					bDuplicated = true;
					break;
				}
			if (!bDuplicated)
				vColumnMappingID.add(sRemainingExpr.substring(iStPos, iFinalPos).replaceAll("\"", ""));
			//prevents repeatedly added columns --end

			iExpBegins += iFinalPos;
			sExpression = sCalculation.substring(iExpBegins);
			sRemainingExpr = sExpression.toString();
		}

		return vColumnMappingID;
	}
}
