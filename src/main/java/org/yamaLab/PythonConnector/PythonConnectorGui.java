package org.yamaLab.PythonConnector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class PythonConnectorGui {
	
    private JPanel mainPanel;
	private JScrollPane pConAreaPane;
	private JLabel pConLabel;
	private JTextArea pConCommandArea;
	private JTextField pConCommandField;
	private JTextField pConAddrField;
	private JTextField pConPortField;    
	private PythonConnector controller;
	private JTextArea pythonStartArea;
	
	PythonConnectorGui(PythonConnector p){
		initPythonConnectorGui();
		controller=p;
	}
	public JPanel getPanel(){
		return mainPanel;
	}
	
	public void initPythonConnectorGui(){
		mainPanel= new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(null);
		
        int x1=1;
        int x2=95;
		/* */
        {
        	int h=5;
			pConLabel = new JLabel();
			mainPanel.add(pConLabel);
			pConLabel.setText("python Connector:");
			pConLabel.setBounds(x1, h, 150, 33);
        	
    		JButton savePropertiesButton = new JButton("SaveProperties");
    		savePropertiesButton.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				System.out.println("Save Properties");
//    				connectTwitter();
//    				reflectProperties();
//    				saveProperties();
    			}
    		});
    		savePropertiesButton.setBounds(530, h, 165, 29);
    		mainPanel.add(savePropertiesButton);        	
        }
		{
			int h=35;
			JLabel xLabel = new JLabel();
			mainPanel.add(xLabel);
		    xLabel.setText("addr,port:");
			xLabel.setBounds(x1, h, 70, 33);
			//
			pConAddrField = new JTextField();
		    mainPanel.add(pConAddrField);
		    pConAddrField.setBounds(x2, h, 250, 33);
		    pConAddrField.setText("localhost");
			//
			pConPortField = new JTextField();
		    mainPanel.add(pConPortField);
		    pConPortField.setBounds(x2+250, h, 100, 33);		
		    pConPortField.setText("9998");
		    /* */
			{
				JButton connectButton = new JButton("connect");
				connectButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("pyCon Connect");
						controller.connect(pConAddrField.getText(),
								new Integer(pConPortField.getText()).intValue());
//						connectTwitter();
//						reflectProperties();
//						saveProperties();
					}
				});
				mainPanel.add(connectButton);
				connectButton.setBounds(x2+352, h, 100, 33);				
			}
			{
				JButton connectButton = new JButton("close");
				connectButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
				            	controller.close();
					}
				});
				mainPanel.add(connectButton);
				connectButton.setBounds(x2+454, h, 100, 33);				
			}			

		}
		{
			int h=70;
			/* */
			pConAreaPane = new JScrollPane();
			mainPanel.add(pConAreaPane);
			pConAreaPane.setBounds(x2, h, 550, 200);
			{
				pConCommandArea = new JTextArea();
				pConAreaPane.setViewportView(pConCommandArea);
			}
			/* */
		}		
		{
			int h=270;
			pConCommandField = new JTextField();
		    mainPanel.add(pConCommandField);
		    pConCommandField.setBounds(x2, h, 450, 33);	
		    JLabel inLabel = new JLabel();
			mainPanel.add(inLabel);
			inLabel.setText("input:");
			inLabel.setBounds(x1, h, 100, 33);
			{
				JButton sendButton = new JButton("send");
				sendButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("pyCon Send");
						String sending=pConCommandField.getText();
//						connectTwitter();
//						reflectProperties();
//						saveProperties();
						controller.send(sending);
					}
				});
				mainPanel.add(sendButton);
				sendButton.setBounds(x2+452,h, 100, 33);				
			}
		}
	/*	*/
		{
			int h=310;
			JScrollPane psPane = new JScrollPane();
			mainPanel.add(psPane);
			psPane.setBounds(x2, h, 450, 100);
			{
				pythonStartArea = new JTextArea();
				psPane.setViewportView(pythonStartArea);
			}
			
		    JLabel inLabel = new JLabel();
			mainPanel.add(inLabel);
			inLabel.setText("script:");
			inLabel.setBounds(x1, h, 100, 33);
			{
				JButton startButton = new JButton("exec");
				startButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("python Program Start");
						String script=pythonStartArea.getText();
//						connectTwitter();
//						reflectProperties();
//						saveProperties();
						controller.startScript(script);
					}
				});
				mainPanel.add(startButton);
				startButton.setBounds(x2+452,h, 100, 33);				
			}
			pythonStartArea.setText("python3 /home/pi/python3/tcp_server_ex1.py");
		}
/* */		
		mainPanel.setVisible(false);
//		this.setSize(804, 700);		
	}

	private Vector<String> putResultQueue=new Vector();	
		
	public void addText(String x){
		putResultQueue.add(x);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	String x=putResultQueue.remove(0);
        		String w=pConCommandArea.getText();
        		if(w.length()>10000)
        			w=w.substring(5000);
        		w=w+"\n"+x;
        		pConCommandArea.setText(w);
        		JScrollBar sb=pConAreaPane.getVerticalScrollBar();
        		sb.setValue(sb.getMaximum());           	
            }
       });		
	}
	public void setCommand(String x){
		this.pConCommandField.setText(x);
	}

}
