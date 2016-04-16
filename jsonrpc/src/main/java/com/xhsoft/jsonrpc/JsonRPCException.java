package com.xhsoft.jsonrpc;

/**
 * A standardized exception class that conforms with JSON-RPC specifications.
 * 
 * @author Wes Widner
 * @see <a
 *      href="http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal#error-object">JSON-RPC
 *      Error Specification</a>
 */

@SuppressWarnings("serial")
public class JsonRPCException extends RuntimeException {
	private int code;

	public JsonRPCException() {
		super();
		setCode(-32603); // Generic JSON-RPC error code.
	}

	public JsonRPCException(String message) {
		super(message);
		setCode(-32603); // Generic JSON-RPC error code.
	}

	public JsonRPCException(String message, int code) {
		super(message);
		setCode(code);
	}

	public JsonRPCException(JsonRPCError error) {
		super(error.message);
		setCode(error.code);
	}

	public JsonRPCException(Throwable e, int code) {
		super(e.getLocalizedMessage());
		setCode(code);
	}

	/**
	 * Set the JSON-RPC error code for this exception
	 * 
	 * @param code
	 *            The JSON-RPC error code, usually negative in the range of
	 *            -32768 to -32000 inclusive
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Get the JSON-RPC error code of this exception.
	 * 
	 * @return long Error code, usually negative in the range of -32768 to
	 *         -32000 inclusive
	 */
	public int getCode() {
		return code;
	}
}
