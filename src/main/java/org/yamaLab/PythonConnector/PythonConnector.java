package org.yamaLab.PythonConnector;

import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.Util;

import com.pi4j.io.serial.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import com.pi4j.jni.I2C;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
 
public class PythonConnector implements InterpreterInterface, Runnable{
    Thread me;
    int counter;
    int frequency;
    int pie;
    boolean pythonUse=true;
    
    boolean debug=false;  /* */
    Serial serial=null;
    InterpreterInterface main;
    String readingLine="";
    Socket socket=null;
    BufferedReader in;
    PrintWriter out;
    BufferedReader keyIn;
    JTabbedPane tabbedPane;
    Properties setting;   
    PythonConnectorGui gui;
	HashMap<String, InterpreterInterface> applications;
    
    public PythonConnector(InterpreterInterface ctlr){
		main=ctlr;
		applications=new HashMap();
	    gui=new PythonConnectorGui(this);	
  }
 
  public void connect(String addr, int port){
        System.out.println("PythonConnector start connect addr:"+addr+" port:"+port);
        try {
          socket = new Socket(addr, port);
          String m=""+socket.getRemoteSocketAddress();
          System.out.println("接続しました" + m);
          gui.addText("connected to "+m+'\n');
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          out = new PrintWriter(socket.getOutputStream(), true);
          keyIn = new BufferedReader(new InputStreamReader(System.in));
          this.start();

        } catch (IOException e) {
        	String m="error: when connect, e="+e;
        	System.out.println(m);
        	gui.addText(m);
            e.printStackTrace();
            try {
              if (socket != null) {
                socket.close();
              }
            } catch (IOException e2) {}
            System.out.println("切断されました "
                               + socket.getRemoteSocketAddress());         
        } finally {
        }
    }
    public void close(){
    	System.out.println("PythonConnector close.");
    	try{
    		socket.close();
     		this.stop();
    	}
    	catch(Exception e){
    		System.out.println("PyCon close error:"+e);
    	}
    }
    public void setGui(JTabbedPane g){
    	this.tabbedPane=g;
    	tabbedPane.add("PythonConnector",gui.getPanel());
    }
    public void setSetting(Properties s){
    	System.out.println("PythonConnector setSetting.");
    	setting=s;
		String w=this.setting.getProperty("pythonCheckBox");
		if(w!=null){
			if(w.equals("true")){
		       this.gui.setPythonUse(true);
		       this.pythonUse=true;
			}
			else
			if(w.equals("false")){
				this.gui.setPythonUse(false);
				this.pythonUse=false;
			}
		}
		else{
	    	this.gui.setPythonUse(true);
	    	if(gui.getPythonUse()){
	    	   setting.put("pythonCheckBox", "true");
	    	}
	    	else{
	    		setting.put("pythonCheckBox", "false");
	    	}
		}
		w=this.setting.getProperty("pyConPort");
		if(w!=null){
			this.gui.setPort(w);
		}
		else{
			setting.put("pyConPort", this.gui.getPort());
		}
		w=this.setting.getProperty("pyConAddr");
		if(w!=null){
			this.gui.setAddr(w);
		}
		else{
			setting.put("pyConAddr", this.gui.getAddr());
		}
    }
    public void saveProperties(){
    	System.out.println("PythonConnector saveProperties.");
	    if(gui.getPythonUse()){
	   	   setting.setProperty("pythonCheckBox", "true");
	    }
	    else{
	    	setting.put("pythonCheckBox", "false");
	    }
		setting.setProperty("pyConPort", this.gui.getPort());
		setting.setProperty("pyConAddr", gui.getAddr());
    }    
	public String getOutputText() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public boolean isTracing() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public String parseCommand(String x) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("PythonConnector..parseCommand:"+x);
		if(x.equals("connect")){
			String host="localhost";
			int port=9998;
			if(gui.getPythonUse()){
			   this.connect(host, port);
			}
		}
		else
		if(x.startsWith("pythonCheckBox ")){
			String rest=x.substring("pythonCheckBox ".length());
			System.out.println("PythonConnector, parseCommand :"+x+". rest="+rest);
			if(rest.equals("true")){
				pythonUse=true;
			}
			else
			if(rest.equals("false")){
				pythonUse=false;
			}
			this.saveProperties();
			this.main.parseCommand("saveProperties");
		}
		else
		if(x.equals("saveProperties")){
			this.saveProperties();
			this.main.parseCommand("saveProperties");
		}
		else{
		  this.send(x);
		}
		return "OK";
	}

	public InterpreterInterface lookUp(String x) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void start(){
		if(me == null){
			me = new Thread(this,"PythohnConnector");
			me.start();
		}
	}

    public void send(String x)
    {
    	System.out.println("send:"+x);
    	try{
    		out.println(x);
    	}
    	catch(Exception e){
    		System.out.println("PythonConnector send error:"+e);
    	}
        gui.setCommand("");
    }	
	
	public void stop(){
		me = null;
	}
	
	public void startScript(String x){
		String[] lines = x.split("\n", 0);
		for(int i=0;i<lines.length;i++){
			String li=lines[i];
			System.out.println(li);
			Process p=null;
	        try {
	            Runtime runtime = Runtime.getRuntime();
	            p = runtime.exec(li);
	            InputStream is = p.getInputStream();
	            p.waitFor(); // プロセス終了を待つ
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	        /*
	        InputStream is = p.getInputStream(); // プロセスの結果を変数に格納する
	        BufferedReader br = new BufferedReader(new InputStreamReader(is)); // テキスト読み込みを行えるようにする

	        try{
	        while (true) {
	            String line = br.readLine();
	            if (line == null) {
	                break; // 全ての行を読み切ったら抜ける
	            } else {
	                System.out.println("line : " + line); // 実行結果を表示
	            }
	        }	
	        }
	        catch(Exception e){
	        	System.out.println("error.."+e);
	        }
	        
	        try{
	        	p.destroy();
	        }
	        catch (Exception e){
	        	System.out.println(e);
	        }
	        */
		}
	}

	public void run() {
		// TODO 自動生成されたメソッド・スタブ
		 System.out.println("start PythonConnector thread");
	     String str="";
	     while(me!=null){
	          try{
	        	  str=in.readLine(); 
	        	  main.parseCommand("putSendBuffer "+str);
	        	  if(this.gui!=null){
	        		  gui.addText(str);
	        	  }
	          } // サーバから 1  line入力する。
	          catch(IOException e){
	        	  System.out.println("PythonConnector error:"+e);
	        	  System.out.println("close(),stop()");
	        	  try{
		                socket.close();
		          }
		          catch(Exception ex){		        		  
		          }    
	        	  stop();
		          return;
	          }
	      }
	}
	public void putApplicationTable(String key, InterpreterInterface obj) {
		// TODO Auto-generated method stub
		applications.put(key, obj);
	}	

}
