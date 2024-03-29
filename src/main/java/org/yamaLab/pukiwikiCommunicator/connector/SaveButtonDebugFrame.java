package org.yamaLab.pukiwikiCommunicator.connector;
import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.yamaLab.pukiwikiCommunicator.language.HtmlTokenizer;
import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.Util;

//import android.widget.EditText;

import org.yamaLab.pukiwikiCommunicator.connector.AuthDialogListener;


/**

*/
public class SaveButtonDebugFrame extends JFrame 
implements AuthDialogListener, InterpreterInterface
{
	private JButton saveButton;
	private String baseUrl;
	private String pageName;
	private String plugInName;
	private String pageCharSet;
	private String charset;

	private static final int AUTH_MODE_PREEMPTIVE = 0;
    private static final int AUTH_MODE_NOT_PREEMPTIVE = 1;
    private static final int AUTH_MODE_CONSOLE = 2;
    private Properties setting;
    private static final long timeOutTime=20000;

	private void initGUI() {
		try {
			{
				getContentPane().setLayout(null);
			}
			{
				saveButton = new JButton();
				getContentPane().add(saveButton);
				saveButton.setText("save");
				saveButton.setBounds(5, 0, 75, 31);
				saveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						saveButtonActionPerformed(evt);
					}
				});
			}
			{
				clearButton = new JButton();
				getContentPane().add(clearButton);
				clearButton.setText("clear message");
				clearButton.setBounds(4, 34, 132, 31);
				clearButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						clearButtonActionPerformed(evt);
					}
				});
			}
			{
				messagePane = new JScrollPane();
				getContentPane().add(messagePane);
				messagePane.setBounds(5, 140, 527, 469);
				{
					messageTextArea = new JTextArea();
					messagePane.setViewportView(messageTextArea);
//					messageTextArea.setBounds(236, 172, 61, 12);
//					messageTextArea.setPreferredSize(new java.awt.Dimension(521, 501));
					messagePane.setAutoscrolls(true);
				}
			}
			{
				inputField = new JTextField();
				getContentPane().add(inputField);
				inputField.setBounds(5, 101, 455, 39);
			}
			{
				sendButton = new JButton();
				getContentPane().add(sendButton);
				sendButton.setText("send");
				sendButton.setBounds(459, 101, 73, 38);
				sendButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						sendButtonActionPerformed(evt);
					}
				});
			}
			{
				urlField = new JTextField();
				getContentPane().add(urlField);
				urlField.setBounds(5, 65, 454, 34);
			}
			{
				connectButton = new JButton();
				getContentPane().add(connectButton);
				connectButton.setText("con");
				connectButton.setBounds(459, 65, 74, 36);
				connectButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						(new Thread(){
							public void run(){
						       connectButtonActionPerformed(null);
							}
						}).start();
					}
				});
			}
			{
				editPageButton = new JButton();
				getContentPane().add(editPageButton);
				editPageButton.setText("edit");
				editPageButton.setBounds(158, 0, 75, 31);
				editPageButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						editPageButtonActionPerformed(evt);
					}
				});
			}
			{
				updateButton = new JButton();
				getContentPane().add(updateButton);
				updateButton.setText("update");
				updateButton.setBounds(233, 0, 84, 31);
				updateButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						updateButtonActionPerformed(evt);
					}
				});
			}
			{
				paramButton = new JButton();
				getContentPane().add(paramButton);
				paramButton.setText("params");
				paramButton.setBounds(317, 0, 91, 31);
				paramButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
					}
				});
			}
			{
				readFromPukiWikiButton = new JButton();
				getContentPane().add(readFromPukiWikiButton);
				readFromPukiWikiButton.setText("read");
				readFromPukiWikiButton.setBounds(80, 1, 78, 30);
				readFromPukiWikiButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
					}
				});
			}
			{
				showMessagesCheckBox = new JCheckBox();
				getContentPane().add(showMessagesCheckBox);
				showMessagesCheckBox.setText("Show messages");
				showMessagesCheckBox.setBounds(147, 38, 154, 23);
				showMessagesCheckBox.setSelected(false);
			}
			{
				this.setSize(551, 650);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveButtonActionPerformed(ActionEvent evt) {
//		System.out.println("saveButton.actionPerformed, event="+evt);
		//TODO add your code for saveButton.actionPerformed
		this.editPageButtonActionPerformed(null);
		this.updateButtonActionPerformed(null);
	}
	public void saveText(String x){
		try{
		this.editPageButtonActionPerformed(null);
		this.replaceTextWith(x);
		}
		catch(Exception e){
			System.out.println("SaveButtonDebugFrame.saveText("+x+") error:"+e);
			Thread.dumpStack();
		}
	}
	public void saveText(String url,String x){
		try{
		   String rtn=this.setPageOfTheURL(url);
		   if(rtn==null) {
			   System.out.println("SaveButtonDebugFrame.saveText( url="+url+", x="+x+") error..");
			   return;
		   }
		   this.editPageButtonActionPerformed(null);
		   this.replaceTextWith(x);
		}
		catch(Exception e){
			System.out.println("SaveButtonDebugFrame.saveText( url="+url+", x="+x+") error:"+e);
		}
	}
	
	private JButton clearButton;
	private JTextArea messageTextArea;
	private JCheckBox showMessagesCheckBox;
	private JButton readFromPukiWikiButton;
	private JButton paramButton;
	private JButton updateButton;
	private JScrollPane messagePane;
	private JButton editPageButton;
	private JButton connectButton;
	private JTextField urlField;
	private JButton sendButton;
	private JTextField inputField;
	private boolean authInputFlag;
	private boolean loginButtonPressed;
	private PukiwikiJavaApplication application;

	public SaveButtonDebugFrame(PukiwikiJavaApplication a){
		this.application=a;
//		this.setting=p;
//		this.messageTextArea.append("documentBase="+this.applet.getDocumentBase().toString()+"\n");
		this.println("y2");
		this.initGUI();
		this.println("saveButtonDebugFrame");
		this.setName("saveButtonFrame");
		charset="UTF-8";
	}
	public void setSetting(Properties x) {
		this.setting=x;
	}
		
	private void clearButtonActionPerformed(ActionEvent evt) {
		this.println("clearButton.actionPerformed, event="+evt);
		//TODO add your code for clearButton.actionPerformed
		this.messageTextArea.setText("");
	}
	private String getUrlWithoutParameters(String url){
		int i=url.indexOf("?");
		if(i<0) return url;
		String rtn=url.substring(0,i);
		return rtn;
	}
	
	HttpClient client=null;
	
	AuthDialog authDialog=null;
	private void connectButtonActionPerformed(ActionEvent evt){
		String url=this.urlField.getText();
		this.println("url="+url);
		connectToURL(url);
	}
	
    static int status=0;
    static HttpMethod method=null;
	private String connectToURL(String url) {
		this.println("connectButton.actionPerformed, url="+url);
		if(url==null){
			System.out.println("connectToURL(null)... return without do anything.");
			return null;
		}
		//TODO add your code for connectButton.actionPerformed
		String pageText=null;
		client=new HttpClient();
//		this.messageTextArea.append(url+"\n");
		String urlWithoutParameters="";
		if(authDialog!=null){
			this.println("authDialog is not null");
			urlWithoutParameters=getUrlWithoutParameters(url);
			String registeredUrl=authDialog.getProperty("auth-url");
			this.println("urlWithoutParamaters="+urlWithoutParameters);
			this.println("registeredUrl="+registeredUrl);
			if(registeredUrl==null){
				this.authDialog=null;
				pageText=this.connectToURL(url);
				return pageText;
			}
			if(registeredUrl.equals(urlWithoutParameters)){
				this.println("registeredUrl == urlWithoutParameters");
				client.getParams().setAuthenticationPreemptive(true);
			    // �ｿｽ�ｽｿ�ｽｽF�ｿｽ�ｽｿ�ｽｽ�ｾ俶得�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ(�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ[�ｿｽ�ｽｿ�ｽｽU�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｾ�繝托ｿｽ�ｽｿ�ｽｽX�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ[�ｿｽ�ｽｿ�ｽｽh)�ｿｽ�ｽｿ�ｽｽ�ｾ御ｽ懈��.
				String uname=authDialog.getID();
				char[] pwd=authDialog.getPassword();
				String pwdx=new String(pwd);
				String idPass=uname+":"+pwdx;
      	        String authUrl="basicAuth-"+urlWithoutParameters;
    	        this.setting.setProperty(authUrl,idPass);
//			    Credentials defaultcreds1 = new UsernamePasswordCredentials(uname, pwdx);
				Credentials defaultcreds1 = new UsernamePasswordCredentials(idPass);
			    // �ｿｽ�ｽｿ�ｽｽF�ｿｽ�ｽｿ�ｽｽ�ｾ倥�ｮ繧ｹ�ｿｽ�ｽｿ�ｽｽR�ｿｽ�ｽｿ�ｽｽ[�ｿｽ�ｽｿ�ｽｽv.
			    AuthScope scope1 = new AuthScope(null, -1, null);
			    // �ｿｽ�ｽｿ�ｽｽX�ｿｽ�ｽｿ�ｽｽR�ｿｽ�ｽｿ�ｽｽ[�ｿｽ�ｽｿ�ｽｽv�ｿｽ�ｽｿ�ｽｽ�ｾ�隱搾ｿｽ�ｽｿ�ｽｽ�ｾ俶得�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｾ檎ｵ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽ�ｿｽ�ｽｿ�ｽｽZ�ｿｽ�ｽｿ�ｽｽb�ｿｽ�ｽｿ�ｽｽg.
			    client.getState().setCredentials(scope1, defaultcreds1);				
			}
			else{
				this.authDialog=null;
				pageText=this.connectToURL(url);
				return pageText;
			}
		}
		try{
			this.println("new getMethod("+url+")");
    		method = new GetMethod(url);
//    		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//					new DefaultHttpMethodRetryHandler(3, false));
    		if(this.authDialog!=null){
        	   method.setDoAuthentication(true);
    		}

//	    	method.getParams().setContentCharset("UTF-8");
    		status=0;
   			try{
			   Thread thread = new Thread(new Runnable(){
//				   @Override
				   public void run(){   		
    		          try{
    				     status=client.executeMethod(method);
    		          }
    		          catch(Exception e){
    			        System.out.println("connect error."+e);
    		         }
				  } //run
			   }); //  time schedule
			   thread.start();
			   long endTimeMillis = System.currentTimeMillis()+timeOutTime;
			   while(thread.isAlive()){
				   if(System.currentTimeMillis()>endTimeMillis){
					   this.println("getMethod connection time out.");
					   method.abort();
					   return null; // return by the time out
				   }
				   try{
					   Thread.sleep(100);
				   }
				   catch(InterruptedException e){}
			   }
   			}
    		catch(Exception e){
    			this.println(e.toString()+"\n");
    			return null;
    		}
		    if (status != HttpStatus.SC_OK) {
		        this.println("Method failed: " + method.getStatusLine());
		        if(method==null) return null;
		        String statusLine=(method.getStatusLine()).toString();
		        if(statusLine==null) {
		        	System.out.println("connectToUrl, statusLine==null");
		        	return null;
		        }
		        if(statusLine.indexOf("401")>=0){
		        	if(this.setting!=null){
			   	        this.authDialog=new AuthDialog(this);
			            this.authInputFlag=false;
			            System.out.println("authInputFlag=false");
			        	urlWithoutParameters=getUrlWithoutParameters(url);
			        	this.authDialog.setProperty("auth-url", urlWithoutParameters);
			        	this.println("before waitUntilMessageIsReturned");		        		
			            urlWithoutParameters=getUrlWithoutParameters(url);
		        	    String authUrl="basicAuth-"+urlWithoutParameters;
		        	    String idPass=this.setting.getProperty(authUrl);
		        	    if(idPass==null){
		        	    	idPass=this.getIdPassFromIdList(urlWithoutParameters);
		        	    }
		        	    if(idPass==null) return null;
		        	    String id="";
		        	    String pas="";
		        	    StringTokenizer st1 =new StringTokenizer(idPass,":");		        	    
		        	    if(idPass!=null || idPass!="") {
			        	    if(st1!=null){
			        	       id=st1.nextToken();
			        	       pas=st1.nextToken();
			        	       if(id!=null)
			        	           this.authDialog.setID(id);
			        	       if(pas!=null)
			        	           this.authDialog.setPassword(pas);
				        	   pageText=this.connectToURL(url);
				        	   if(pageText!=null) return pageText;			        	       
			        	    } // if(this.setting!=null)		  
		        	    }		        	    		        	    

			        	this.authDialog.setVisible(true);
		        	    
		        	    id="";
		        	    pas="";
		        	    st1 =new StringTokenizer(idPass,":");		        	    		        	    
		        	    if(st1!=null){
		        	       id=st1.nextToken();
		        	       pas=st1.nextToken();
		        	       if(id!=null)
		        	           this.authDialog.setID(id);
		        	       if(pas!=null)
		        	           this.authDialog.setPassword(pas);
		        	    }
		        	} // if(this.setting!=null)

		        	this.waitUntilMessageIsReturned(); //!!
//		        	this.authDialog.stop(); //!!
		        	this.authDialog.setVisible(false);
		        	this.println("after waitUntilMessageIsReturned");
		        	if(this.loginButtonPressed){
		        		  this.println("loginButtonPressed");
		        	      pageText=this.connectToURL(url);
		        	}
		        	return pageText;
		        } // if((method.getStatusLine()).toString().indexOf("401")>=0){
		    }// if (status != HttpStatus.SC_OK) {
		    else{
		    	pageText=this.getText(method);
		    }
		   
		}
		catch(Exception e){
			this.println(e.toString()+"\n");
			e.printStackTrace();
			return null;
		}

		return pageText;
	}
	private String getIdPassFromIdList(String url){ 
    	for(Object keyo:setting.keySet()){
	    	String key=(String)keyo;
		    if(key.startsWith("basicAuthList-")){
			    String xlabel=key.substring("basicAuthList-".length());
			    String[] rest=new String[1];
			    int[] no=new int[1];
			    Util.parseInt(xlabel, no, rest);
			    int nox=no[0];
			    String label2=rest[0];
			    String urlWithoutParametersx=label2.substring(1);
			    if(urlWithoutParametersx.equals(url)){
			       String idPassx=(String)(setting.get(key));
			       return idPassx;
			    }
		    }
	    }
    	return null;
	}
	private String getText(HttpMethod method){
		String pageText="";
	    try{
		   InputStream is=method.getResponseBodyAsStream();
 		   InputStreamReader isr=new InputStreamReader(is,this.charset);
//     	    InputStreamReader isr=new InputStreamReader(is,"UTF-8");
// 		    InputStreamReader isr=new InputStreamReader(is);
	       BufferedReader br=new BufferedReader(isr);
	       String line="";
	       pageText="";
	       while(true){
	    	   line=br.readLine();
	    	   pageText=pageText+line+"\n";
	        	if(line==null) break;
	    	    this.messageTextArea.append(line+"\n");
//	    	    System.out.println(line);
	       }
	       method.releaseConnection();		
	    }
	    catch(Exception e){}
	    return pageText;
	}
	
	private void waitUntilMessageIsReturned(){
		while(!this.authInputFlag){
			try{
				Thread.sleep(100);
			}
			catch(InterruptedException e){
				
			}
		}
		System.out.println("authInputFlag changed false to true");
	}
	private void sendButtonActionPerformed(ActionEvent evt) {
		this.println("sendButton.actionPerformed, event="+evt);
		//TODO add your code for sendButton.actionPerformed
	}
	String updateText;
	String actionUrl;
	String editCmd;
	String editPage;
	String editDigest;
	String editWriteValue;
	String editEncodeHint;
    public void setBaseUrl(String url){
    	if(url==null) {
    		System.out.println("baseUrl became null");
    		Thread.dumpStack();
    	}
    	this.baseUrl=url;
    }
	private void editPageButtonActionPerformed(ActionEvent evt) {
		this.println("editPageButton.actionPerformed, event="+evt);
		if(baseUrl==null) {
//			Thread.dumpStack();
			System.out.println("something wrong... baseUrl==null");
			return;
		}
		//TODO add your code for editPageButton.actionPerformed
		   String editUrl=baseUrl+"?cmd=edit&page="+pageName;
		this.println("editUrl="+editUrl);
//		this.messageTextArea.append(baseUrl+"\n");
		this.println("baseUrl="+baseUrl+"\n");
//		this.messageTextArea.append(editUrl+"\n");
		this.println(editUrl+"\n");
		this.urlField.setText(editUrl);
		this.messageTextArea.setText("");
		String x=this.connectToURL(editUrl);
		if(x==null) return;
//		String x=this.messageTextArea.getText();
		
		/* get the first form from the url*/
		String form=this.getBetween(x,"<form", "</form>");
		if(form==null) return;
		this.println("form="+form);
		/* get the head part in the text area from the form*/
		int i=form.indexOf("<textarea ");
		if(i<0) {
			this.println("Could not find out textarea");
			return;
		}
//		this.messageTextArea.setText("");
//		this.messageTextArea.append("i="+i+"\n");
		String y="";
		String z="";
		try{
		    y=form.substring(i);
		    z=y.substring(y.indexOf(">")+1);		
		}
		catch(Exception e){
			
		}
		this.println("z="+z);
//		this.println("plugInName="+this.plugInName);
//		int j=z.indexOf("#"+this.plugInName);
		int j=z.indexOf("result:");
		if(j<0){
			this.println("Could not find out result:");
//			z=z+"\n"+"result:\n";
			this.application.error("format-error ... could not find out result:");
			return;
		}
		int k=j+("result:").length();

		//ring head=x.substring(0,k-1);
		this.println("j="+j+" k="+k);
		// has the command #netpaint argument? 
		String dataStart=z.substring(j);
		this.println("dataStart="+dataStart);
		/*
		if(dataStart.startsWith("result:")){
			String theCommand="result:" ;
			this.println("theCommand="+theCommand);
			k=j+theCommand.length();
		}
		*/
		this.println("j="+j+" k="+k);
		
		String head=z.substring(0,k);
		String w=z.substring(head.length());
		int l=w.indexOf("</textarea");
		String tail=w.substring(l);
		String body=w.substring(0,l-1);
		this.println("head="+head);
		this.println("body="+body);
		this.println("tail="+tail);
		
        head=head.replaceAll("&quot;", "\""); //2016 9/22 yamanoue
        head=head.replaceAll("&lt;", "<");    // 2016 9/22 yamanoue
        head=head.replaceAll("&gt;", ">");	 // 2016 9/22 yamanoue
        head=head.replaceAll("&amp;", "&");	 // 2016 9/26 yamanoue
				
		this.updateText=head;
		String actionwork1=form.substring(0,form.indexOf(">"));
		this.println("actionwork1="+actionwork1);
		//System.out.println("actionwork1="+actionwork1);
		this.actionUrl=this.getTagProperty(actionwork1, "action");
		if(actionUrl.equals("./")){   //2022 12/9 yamanoue
			actionUrl=this.baseUrl;
		}
		this.println("action url="+this.actionUrl);
		//System.out.println("action url="+this.actionUrl);
		
		
		/* 
		 getting input properties
		 */
		HtmlTokenizer htmlt=new HtmlTokenizer(form);
		while(htmlt.hasMoreTokens()){
			String t=htmlt.nextToken();
			if(t.startsWith("<input")){
				String ttype=getTagProperty(t,"type");
				if(ttype.equals("hidden")){
				   String tname=getTagProperty(t,"name");
				   String tvalue=getTagProperty(t,"value");
				   this.println(" "+tname+"="+tvalue);
				   if(tname.equals("cmd")){
					   this.editCmd=tvalue;
				   }
				   if(tname.equals("page")){
					   this.editPage=tvalue;
				   }
				   if(tname.equals("digest")){
				   		this.editDigest=tvalue;
				   }
				   if(tname.equals("encode_hint")){
					   this.editEncodeHint=tvalue;
				   }
				}
				if(ttype.equals("submit")){
				   String tname=getTagProperty(t,"name");
				   String tvalue=getTagProperty(t,"value");
				   this.println(" "+tname+"="+tvalue);
				   if(tname.equals("write")){
						this.editWriteValue=tvalue;
			       }
				}
			}
		}
		
	}
	String insertSpaceAfterNewLine(String x){
		StringTokenizer st=new StringTokenizer(x,"\n",true);
		String rtn="";
		while(st.hasMoreTokens()){
			String t=st.nextToken();
			if(t.equals("\n")){
				rtn=rtn+t+" ";
			}
			else{
				rtn=rtn+t;
			}
		}
		return rtn;
	}
	
	private String getTagProperty(String tag, String key){
		//System.out.println("tag="+tag);
		//System.out.println("key="+key);
		StringTokenizer st=new StringTokenizer(tag," =",true);
		String t=st.nextToken();
//		System.out.println(" first token="+t);
		while(st.hasMoreTokens()){
			t=st.nextToken();
			if(t.equals(" ")){        // skip space
				while(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			}
			String keyx=t;
			//System.out.println(" key?="+keyx);
			if(!st.hasMoreTokens()) return "";
			t=st.nextToken();
			if(t.equals(" ")){        // skip space
				while(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			}
			if(t.equals("=")){
//				System.out.println("...=");
				if(!st.hasMoreTokens()) return "";
				t=st.nextToken();
				if(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			    if(keyx.equals(key)){
			    	//System.out.println(" keyx="+key+" t="+t);
			    	if(t.startsWith("\"")){
			    			t=t.substring(1);
			    	}
			    	if(t.endsWith("\"")){
			    		t=t.substring(0,t.length()-1);
			    	}
			    	return t;
			    }
			}
			
		}
		return "";
	}
	
	private void updateButtonActionPerformed(ActionEvent evt) {
		String output=application.getOutput();
		replaceTextWith(output);
	}
	public String setPageOfTheURL(String url){
		this.println("setPageOfTheURL editUrl="+url);
		this.urlField.setText(url);
		String x=connectToURL(url);
		if(x==null) return null;
//		String x=this.messageTextArea.getText();
		
		// extract charSet from <?xml= ...?> which contains charSet;
		String firstXmltag=getBetween(x,"<?xml","?>");
		if(firstXmltag==null) {
			firstXmltag=getBetween(x,"<meta","/>");
			if(firstXmltag==null) return null;
		}
		this.pageCharSet=this.getTagProperty(firstXmltag,"encoding");
		if(this.pageCharSet==null)
			this.pageCharSet="UTF-8";
		else
		if(this.pageCharSet.equals(""))
			this.pageCharSet="UTF-8";		
		this.println("pageCharSet="+this.pageCharSet);
		x=getBetween(x,"<body>","</body>");
		
		// exclude until <applet
//		int i=x.indexOf("<applet");
//		if(i<0) return;
//		x=x.substring(i);
		String headerPart=getBetween(x,"<div id=\"header\">","</div>");
		String pageNamePart=getBetween(headerPart,"<span class=\"small\">","</span>");
		String xurl=null;
		if(pageNamePart.indexOf("?")<0){
			xurl=pageNamePart;
			pageName="";
		}
		else{
		    StringTokenizer st=new StringTokenizer(pageNamePart,"?");
		    xurl=st.nextToken();
		    this.pageName=st.nextToken();
		}
//		this.baseUrl=st.nextToken();
		if(xurl==null) {
//			Thread.dumpStack();
		}
		this.baseUrl=xurl;
		System.out.println("setPageOfTheURL("+url+")baseUrl="+baseUrl);
		return x;
	}
	public String setPageOfTheURL2(String url){
		this.println("editUrl="+url+"\n");
		this.urlField.setText(url);
		String x="";
		try{
	    x=connectToURL(url);
		}
		catch(Exception e){
			return null;
		}
		if(x==null) return null;
//		String x=this.messageTextArea.getText();
		
		
		return x;
	}
	private void replaceTextWith(String x){
		// System.out.println("updateButton.actionPerformed, event="+evt);
		//TODO add your code for updateButton.actionPerformed
		
		/* make the body (fig) */
		/*
		try{
			fig=new String(fig.getBytes("UTF-8"),  "UTF-8");
		}
		catch(Exception e){
			return;
		}
		*/
		if(this.updateText==null) this.updateText="";
		this.updateText=this.updateText+"\n "+insertSpaceAfterNewLine(x);
		
		this.urlField.setText(this.actionUrl);
		String url=this.urlField.getText();
		this.println("url="+url);
//		this.messageTextArea.append(this.updateText);
		   BufferedReader br = null;
//		System.out.println("updateText="+this.updateText);
		this.println("editWriteValue="+this.editWriteValue);
		this.println("editCmd="+this.editCmd);
		this.println("editPage="+this.editPage);
		this.println("digest="+this.editDigest);
		try{
    		PostMethod method = new PostMethod(url);
    		if(this.client==null) return;
    		method.getParams().setContentCharset(this.pageCharSet);
    		method.setParameter("msg",this.updateText);
//    		method.setParameter("encode_hint",this.editEncodeHint);
    		if(this.editWriteValue!=null)
    		    method.addParameter("write",this.editWriteValue);
    		else
    			method.addParameter("write","");
    		method.addParameter("cmd",this.editCmd);
    		method.addParameter("page",this.editPage);
    		method.addParameter("digest",this.editDigest);
			int status=client.executeMethod(method);
		    if (status != HttpStatus.SC_OK) {
		          this.println("Method failed: " + method.getStatusLine());
		          method.getResponseBodyAsString();
		    }
		    else {
		          br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
		          String readLine;
		          while(((readLine = br.readLine()) != null)) {
		            this.println(readLine);
		          }
		   }
		    method.releaseConnection();
		}
		catch(Exception e){
//			this.messageTextArea.append(e.toString()+"\n");
			System.out.println("SaveButtonDebuFram.replaceTextWith("+x+") error:"+e);
			System.out.println("msg.updateText="+updateText);
		    System.out.println("write.editWriteValue="+this.editWriteValue);
		    System.out.println("cmd.editCmd="+this.editCmd);
		    System.out.println("page.editPage="+this.editPage);
		    System.out.println("digest.editDigest="+this.editDigest);
			e.printStackTrace();
		}
		
	}
	
	boolean isInStringConst(String x, int p){
		
		int px=0;
		int py=0;
		boolean isIn=false;
		while(px<x.length()){
			if(px>p) return false;
			char cx=x.charAt(px);
			char cy=0;
			py=px+1;
			if(cx=='"'){
				isIn=true;
				while(py<x.length()){
					cy=x.charAt(py);
					if(cy=='"'){
						if(px<p && p<py)
							return true;
						else{
							isIn=false;
							px=py;
							break;
						}
					}
					if(cy=='\\'){
						py=py+1;
					}
					py=py+1;
				}
				if(isIn)
					return true;
			}
			if(cx=='\\'){
				px=px+1;
			}
			px=px+1;
		}
		return false;
	}
	public String readFromPukiwikiPageAndSetData(String url) {
		this.println("editUrl="+url+"\n");
		String x=this.setPageOfTheURL(url);
		if(x==null) {
			return "ERROR";
		}
		String inw=getBetween(x,"<pre>","</pre>");
		/*
		String fig="";
		try{
			fig=new String(figw.getBytes(this.pageCharSet),"UTF-8");
		}
		catch(Exception e){
			System.out.println("decoding error..."+e.toString());
			fig=figw;
		}
		*/
		String input=inw;
		if(input==null) return "ERROR";
        input=input.replaceAll("&quot;", "\"");
        input=input.replaceAll("&lt;", "<");
        input=input.replaceAll("&gt;", ">");
        input=input.replaceAll("&amp;", "&");	 // 2016 9/26 yamanoue
        
        application.setInput(input+"\n");
		return "ok";
	}

	/*
	 *  get the first string which is in from l to r in the x
	 */
	String getBetween(String x, String l, String r){
//		System.out.println("x="+x);
		this.println("l="+l);
		this.println("r="+r);
		if(x==null) return null;
		int i=0;
		while(i<=0){
			i=x.indexOf(l,i);
    		if(i<0) return null;
    		if(i==0) break;
	    	if(isInStringConst(x,i)){
		    	i=i+l.length();
		    }
		}
		
		i=i+l.length();
		int j=i;
		while(j<=i){
		    j=x.indexOf(r,j);
		    if(j<0) return null;
		    if(isInStringConst(x,j)){
		    	j=j+r.length();
		    }
		}
		String rtn="";
		try{
			rtn=x.substring(i,j);
		}
		catch(Exception e){
			return null;
		}
//		System.out.println("rtn="+rtn);
		return rtn;
	}
    public void loadPage(String url){
    	this.readFromPukiwikiPageAndSetData(url) ;   	
    }

	public void println(String x){
//		System.out.println(x);
		if(this.showMessagesCheckBox==null) return;
		if(this.showMessagesCheckBox.isSelected()){
			/*
			if(this.messageTextArea==null) return;
			this.messageTextArea.append(x+"\n");
			this.messageTextArea.setCaretPosition((this.messageTextArea.getText()).length());
			*/
			System.out.println(x);
		}
	}

	public void whenLoginButtonClicked(AuthDialog x) {
		// TODO Auto-generated method stub
		this.loginButtonPressed=true;
		this.authInputFlag=true;
		System.out.println("authInputFlag=true");
		x.setVisible(false);
		
	}

	public void whenCancelButtonClicked(AuthDialog x) {
		// TODO Auto-generated method stub
		this.loginButtonPressed=false;
		this.authInputFlag=true;
		System.out.println("authInputFlag=ture");
		x.setVisible(false);
		
	}

//	@Override
	public String getOutputText() {
		// TODO Auto-generated method stub
//		return this.getOutputText();
		return application.getOutput();
	}

//	@Override
	public boolean isTracing() {
		// TODO Auto-generated method stub
//		return this.isTracing();
		return false;
	}

//	@Override
	public String parseCommand(String x) {
		// TODO Auto-generated method stub
		String rest[]=new String[1];
		x=Util.skipSpace(x);
		if(Util.parseKeyWord(x, "getpage ", rest)||Util.parseKeyWord(x, "getPage ", rest)){
			String url=rest[0];
			String y=this.setPageOfTheURL(url);
			String inw=getBetween(y,"<pre>","</pre>");
			String input=inw;
			if(input==null) return "ERROR";
	        input=input.replaceAll("&quot;", "\"");
	        input=input.replaceAll("&lt;", "<");
	        input=input.replaceAll("&gt;", ">");	 			
			return input;
		}
		if(Util.parseKeyWord(x, "getRawPage ", rest)){
			String url=rest[0];
			String y=this.setPageOfTheURL2(url);
			if(y==null) return "ERROR";
			return y;
		}
		
		return "ERROR";
	}

//	@Override
	public InterpreterInterface lookUp(String x) {
		// TODO Auto-generated method stub
		return null;
	}

	public String parseCommandWithReturn(String x, String y) {
		// TODO Auto-generated method stub
		return null;
	}

	public void putApplicationTable(String key, InterpreterInterface obj) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	public String getOutputText() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isTracing() {
		// TODO Auto-generated method stub
		return false;
	}

	public String parseCommand(String x) {
		// TODO Auto-generated method stub
		return null;
	}

	public InterpreterInterface lookUp(String x) {
		// TODO Auto-generated method stub
		return null;
	}
	*/
}
