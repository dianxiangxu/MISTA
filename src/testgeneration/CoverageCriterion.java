/* 	
	Author Dianxiang Xu
*/
package testgeneration;

public class CoverageCriterion implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name; 
	
	public CoverageCriterion(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	public String getName(){
		return name;
	}

	public String getID(){
		return id;
	}

	public boolean isCoverage(CoverageCriterion coverage) {
		return name.equals(coverage.name);
	}

}
