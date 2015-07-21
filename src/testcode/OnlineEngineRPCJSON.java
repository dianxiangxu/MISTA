package testcode;

import java.net.URL;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;

import locales.LocaleBundle;
import mid.Predicate;

public class OnlineEngineRPCJSON implements OnlineEngineInterface {

	private URL serverURL=null;
	private JSONRPC2Session mySession =null;
	private int requestID = 0;

	public OnlineEngineRPCJSON(URL url){
		this.serverURL = url;
		mySession = new JSONRPC2Session(serverURL);
	}
	
	public void executeMethod(Predicate rpc) throws Exception{
		JSONRPC2Request request = new JSONRPC2Request(rpc.getName(), requestID++);
		if (rpc.getArguments().size()>0)
			request.setParams(rpc.getArguments());
		JSONRPC2Response response = mySession.send(request);
		if (response.indicatesSuccess()) {
//			System.out.println("Success: "+response.getResult());
		}
		else
			throw new Exception(response.getError().getMessage());

	}
	
	public boolean executeQuery(Predicate rpc) throws Exception{
		JSONRPC2Request request = new JSONRPC2Request(rpc.getName(), requestID++);
		if (rpc.getArguments().size()>0)
			request.setParams(rpc.getArguments());
		JSONRPC2Response response = mySession.send(request);
		if (response.indicatesSuccess() && response.getResult()!=null){
			if (response.getResult().toString().equals("false"))
				return false;
			else
			if (response.getResult().toString().equals("true"))
				return true;
			else 
				throw new Exception(LocaleBundle.bundleString("RETURN_VALUE_IS_NOT_BOOLEAN"));
		}
		else
			throw new Exception(response.getError().getMessage());
	}

	public boolean hasEngine(){
		return mySession!=null;
	}

	public void terminate(){
	}

}
