package testcode;

import java.util.ArrayList;

import kernel.ProgressDialog;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import edit.GeneralEditor;

import parser.MIDParser;

import locales.LocaleBundle;
import mid.Predicate;

public class OnlineEngineSelenium implements OnlineEngineInterface {

	private WebDriver driver=null;
	private WebDriverCommandProcessor webDriverCommandProcessor;

	public OnlineEngineSelenium(String url, GeneralEditor editor){
		int browser = editor.getKernel().getSystemOptions().getTestFrameworkIndex();
		ProgressDialog progressDialog = new ProgressDialog(null, LocaleBundle.bundleString("ONLINE_TESTING"), 
				LocaleBundle.bundleString("STARTING_SELENIUM_DRIVER"), 
				ProgressDialog.CANCELLATION_NOT_ALLOWED);
		Thread codeGenerationThread = new Thread(new OpenBrowserThread(progressDialog, browser));
		codeGenerationThread.start();
		progressDialog.setVisible(true);
		if (driver==null)
			return;
		webDriverCommandProcessor = new WebDriverCommandProcessor("", driver);
		String[] arg = new String[1];
		arg[0]=url;
		webDriverCommandProcessor.doCommand("open", arg);
	}
	
	class OpenBrowserThread implements Runnable {
		private ProgressDialog progressDialog;
		private int browser;
		
		OpenBrowserThread(ProgressDialog progressDialog, int browser) {
			this.progressDialog = progressDialog;
			this.browser = browser;
		}
		
		public void run () {
			try {
				switch (browser){
					case TargetLanguage.FIREBOXBROWSER: 
						driver = new FirefoxDriver(); 
						break;	
					case TargetLanguage.INTERNETEXPLORER: 
						driver = new InternetExplorerDriver(); 
						break;	
				}
				progressDialog.dispose();
			} 
			catch (Exception e){
				progressDialog.finishDialog(LocaleBundle.bundleString("Fail to launch web browser"));
			}
		}
	}

	public void executeMethod(Predicate cmd) throws Exception{
		String[] args = transformArguments(cmd.getArguments());
		webDriverCommandProcessor.doCommand(cmd.getName(), args);
	}
	
	public boolean executeQuery(Predicate cmd){
		String[] args = transformArguments(cmd.getArguments());
		try {
			String result = webDriverCommandProcessor.doCommand(cmd.getName(),args);
			return result!=null && result.equalsIgnoreCase("TRUE")? true: false;
		}
		catch (Exception e){
			return false;
		}
	}

	private String[] transformArguments(ArrayList<String> args){
		String[] arguments = new String[args.size()];
		for (int index=0; index<args.size(); index++)
			arguments[index]= MIDParser.removeQuotesFromString(args.get(index));
		return arguments;
	}
	
	public boolean hasEngine(){
		return driver!=null;
	}
	
	public void terminate(){
		if (driver!=null)
			driver.close();
	}
	
}
