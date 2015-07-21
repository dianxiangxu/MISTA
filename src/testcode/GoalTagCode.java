package testcode;


public class GoalTagCode implements java.io.Serializable  {

	private static final long serialVersionUID = 1L;

	private TargetLanguage targetLanguage;
	private String testFramework;
	private String tagCode;
	
	public GoalTagCode(TargetLanguage targetLanguage, String testFramework, String tagCode){
		this.targetLanguage = targetLanguage;
		this.testFramework = testFramework;
		this.tagCode = tagCode;
	}
	
	public boolean isForTestFramework(TargetLanguage targetLanguage, String testFramework){
		return this.targetLanguage.equals(targetLanguage) && this.testFramework.equalsIgnoreCase(testFramework);
	}
	
	public String getTitle(){
		return "["+targetLanguage.getName() + (testFramework.equalsIgnoreCase("No")?"":", "+testFramework)+"]";
	}
	
	public void setTagCode(String code) {
		this.tagCode = code;
	}
	
	public String getTagCode(){
		return tagCode;
	}
	
	private static final String JAVAGOALTAG = "System.out.println(\"GOAL [NAME]\");";
	
	public static final GoalTagCode JAVA_NOFRAMEWORK_TAG = new GoalTagCode(TargetLanguage.JAVA, TargetLanguage.NO_TEST_ENGINE, JAVAGOALTAG);
	public static final GoalTagCode JAVA_JUNIT_TAG = new GoalTagCode(TargetLanguage.JAVA, TargetLanguage.JUNIT3, JAVAGOALTAG);
	public static final GoalTagCode JAVA_WINDOWTESTER_TAG = new GoalTagCode(TargetLanguage.JAVA, TargetLanguage.WINDOW_TESTER, JAVAGOALTAG);

	public static final GoalTagCode KBT_ROBOTFRAMEWORK_TAG = new GoalTagCode(TargetLanguage.KBT, TargetLanguage.ROBOT_FRAMEWORK, "[Tags]  [NAME]");
	
	public static final GoalTagCode[] DEFAULT_GOAL_TAGS = {JAVA_NOFRAMEWORK_TAG, JAVA_JUNIT_TAG, JAVA_WINDOWTESTER_TAG, KBT_ROBOTFRAMEWORK_TAG};
	

}
