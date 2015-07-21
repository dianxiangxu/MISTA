package testgeneration;

import java.io.Serializable;

public class ParaRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String expression;
	private boolean parameter;

	public ParaRecord() {
		this.expression = "";
//		this.parameter = true;    // expression is a statement
		this.parameter = false;   // expression is a method parameter
	}

	public ParaRecord(String expression, boolean parameter) {
		this.expression = expression;
		this.parameter=parameter;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String exp) {
		this.expression = exp;
	}

	public boolean isParameter() {
		return parameter;
	}

	public void setParameter(boolean parameter) {
		this.parameter = parameter;
	}

	public String toString() {
		return expression+" "+parameter;
	}
	
	public ParaRecord clone(){
		return new ParaRecord(expression, parameter);
	}
}
