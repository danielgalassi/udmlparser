/**
 * 
 */
package obiee.udmlparser.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Requests represent a specific call from a command line interface.
 * @author danielgalassi@gmail.com
 *
 */
public class Request {

	/** A set of (key,value) pairs representing all (no flags) attributes*/
	private Map<String, String> request = new HashMap<String, String>();
	private boolean isBusMatrixInvoked = false;
	private boolean isFullMatrixInvoked = false;
	private boolean isTransformationInvoked = false;

	public void invokeTransformation(boolean invoked) {
		isTransformationInvoked = invoked;
	}

	public boolean isTransformationInvoked() {
		return isTransformationInvoked;
	}

	public void invokeBusMatrix(String value) {
		isBusMatrixInvoked = (value.equals("busmatrix"));
		
		if (!isBusMatrixInvoked) {
			isFullMatrixInvoked = (value.equals("metrics"));
		}
	}

	public boolean isBusMatrixInvoked() {
		return isBusMatrixInvoked;
	}

	public boolean isFullMatrixInvoked() {
		return isFullMatrixInvoked;
	}

	/**
	 * Sets execution argument to the provided value
	 * @param key command line argument representation
	 * @param value value of the argument (not flags)
	 */
	public void setArg(String key, String value) {
		if (request.containsKey(key)) {
			request.remove(key);
		}
		request.put(key, value);
	}

	/**
	 * Retrieves the value of the execution argument. If not available, returns an empty <code>String</code>
	 * @param key command line argument representation
	 * @return the value of the execution argument or an empty String if no value is found
	 */
	public String getArg(String key) {
		String value = "";
		if (request.containsKey(key)) {
			value = request.get(key);
		}
		else {
			System.out.println("Parameter " + key + " not found.");
		}
		return value;
	}
}