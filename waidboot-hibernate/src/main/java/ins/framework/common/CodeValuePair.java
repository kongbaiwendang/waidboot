package ins.framework.common;

import java.io.Serializable;

public class CodeValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private String value;

	public CodeValuePair() {
	}

	public CodeValuePair(String code, String value) {
		this.code = code;
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "CodeValuePair [code=" + code + ", value=" + value + "]";
	}

}
