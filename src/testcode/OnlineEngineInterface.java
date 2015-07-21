package testcode;

import mid.Predicate;

public interface OnlineEngineInterface {
	public boolean hasEngine();
	public void executeMethod(Predicate call) throws Exception;
	public boolean executeQuery(Predicate call) throws Exception;
	public void terminate();
}
