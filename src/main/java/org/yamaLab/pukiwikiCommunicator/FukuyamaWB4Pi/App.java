package org.yamaLab.pukiwikiCommunicator.FukuyamaWB4Pi;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.yamaLab.pukiwikiCommunicator.MainController;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
/*
 * 
 
 app - new -> MainController
 
 MainController -> new gui, TwitterController, set properties
 
 on the gui,
 [connect twitter button] --connect twitter command--> TwitterController 
 
 [connect wiki button] --- pukiwiki Java Application
 
 +---------------+
 |twitter        |
 +---------------+ 
       |
 +-----------------+               +---+
 |TwitterController|-------+       |GUI|
 +-----------------+       |       +---+
                           |         |
                           |       +-----+
                           +-------|App  |
                                   +-----+
                                     |
                +--------------------+
                |
 +--------------------+   +-----------------------+      +-------------+
 |PukiwikiCommunicator|-> |PukiwikiJavaApplication|------|Pukiwiki Page|
 +--------------------+   +-----------------------+      +-------------+
 
 
 
 
 
 
 */

public class App
{
	   static WikiBotV1Gui frame;
	       static boolean guiOn;
	
    public static void main( String[] args ) throws TwitterException
    {
    	/*
        Twitter twitter = new TwitterFactory().getInstance();
        User user = twitter.verifyCredentials();
        
        //ユーザ情報取得
        System.out.println("なまえ　　　：" + user.getName());
        System.out.println("ひょうじ名　：" + user.getScreenName());
        System.err.println("ふぉろー数　：" + user.getFriendsCount());
        System.out.println("ふぉろわー数：" + user.getFollowersCount());
      
        //ついーとしてみる
        Status status = twitter.updateStatus("初めてTwitter4J使ってみました(^^)/");
        */
//        App app=new App();
  	  guiOn=true;
    	if(args.length>0){
    	String a0=args[0];
    	  if(a0!=null){
    	   System.out.println("a0="+a0);
    	   if(a0.equals("-nw")){
    		              guiOn=false;
    	   }
    	  }
    	}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new WikiBotV1Gui();
					if(guiOn){
					   frame.setVisible(true);
					}
					else{
						 frame.startAppli();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
//          frame.setVisible(true);
    }
}
