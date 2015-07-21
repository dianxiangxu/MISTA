package testcode;

import java.net.URL;

import javax.swing.JOptionPane;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import locales.LocaleBundle;
import mid.Predicate;

public class OnlineEngineRPCApache implements OnlineEngineInterface {

	private XmlRpcClient xmlrpc;
	
	public OnlineEngineRPCApache(URL url){
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(url);
			xmlrpc = new XmlRpcClient();
			xmlrpc.setConfig(config);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Fail to connect"));
		}
	}
	
	public void executeMethod(Predicate rpc) throws Exception{
		if (xmlrpc!=null)
			xmlrpc.execute (rpc.getName(), rpc.getArguments());
		else
		  throw new Exception(LocaleBundle.bundleString("Network exception"));
	}
	
	public boolean executeQuery(Predicate rpc) throws Exception{
		if (xmlrpc==null)
			throw new Exception(LocaleBundle.bundleString("Network exception"));
		String result = (String)xmlrpc.execute (rpc.getName(), rpc.getArguments());
		if (result!=null) {
			if (result.equals("false"))
				return false;
			else
			if (result.equals("true"))
				return true;
		}
		throw new Exception(LocaleBundle.bundleString("RETURN_VALUE_IS_NOT_BOOLEAN"));
	}

	public boolean hasEngine(){
		return xmlrpc!=null;
	}
	
	public void terminate(){
	}

}
