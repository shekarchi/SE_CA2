package com.zahra.BankTransactions;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class ClientTerminal {

	private BufferedReader in;
    private PrintWriter out;
    private String id;
    private String type;
    private String serverIP;
    private String serverPort;
    private String outlogPath;
    
    //I used given link for this class: http://www.java-samples.com/showtutorial.php?tutorialid=152
    public class MyXMLParser {
    	private String filename;
    	private Document dom;
    	private String xId;
        private String xType;
        private String xServerIP;
        private String xServerPort;
        private String xOutlogPath;
        private ArrayList<Transaction> transactionsList = null;
    	
        public String getServerIP() {
        	return xServerIP;
        }
        
        public String getServerPort() {
        	return xServerPort;
        }
        
        public String getId() {
        	return xId;
        }
        
        public MyXMLParser(String _filename) {
        	filename = _filename;
        	transactionsList = new ArrayList<Transaction>();
        }
        
        public ArrayList<Transaction> getTransactionsList() {
        	return transactionsList;
        }
        
        public void parseXmlFile(){
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    		try {
    			DocumentBuilder db = dbf.newDocumentBuilder();
    			dom = db.parse(filename);
    			parseDocument();
    		}catch(ParserConfigurationException pce) {
    			pce.printStackTrace();
    		}catch(SAXException se) {
    			se.printStackTrace();
    		}catch(IOException ioe) {
    			ioe.printStackTrace();
    		}
    	}
        
        private void parseDocument(){
    		dom.getDocumentElement().normalize();
    		
    		NodeList terminalInfo = dom.getElementsByTagName("terminal");
    		xId = ((Element)(terminalInfo.item(0))).getAttribute("id");
    		xType = ((Element)(terminalInfo.item(0))).getAttribute("type");
    		
    		NodeList serverInfo = dom.getElementsByTagName("server");
			xServerIP = ((Element)(serverInfo.item(0))).getAttribute("ip");
			xServerPort = ((Element)(serverInfo.item(0))).getAttribute("port");
			
			NodeList outLogPathInfo = dom.getElementsByTagName("outLog");
			xOutlogPath = ((Element)(outLogPathInfo.item(0))).getAttribute("path");
			
    		NodeList nl = dom.getDocumentElement().getElementsByTagName("transaction");
    		for(int i = 0 ; i < nl.getLength();i++) {
    				Element el = (Element)nl.item(i);
    				addTransaction(el);	
    		}
    	}
        
        private void addTransaction(Element transEl) {
        	String id = transEl.getAttribute("id");
        	String type = transEl.getAttribute("type");
        	String amount = transEl.getAttribute("amount");
        	String deposit = transEl.getAttribute("deposit");
    		Transaction e = new Transaction(id, type, new BigDecimal(amount), deposit);
    		transactionsList.add(e);
    	}
        
        public void printParsedFile() {
        	System.err.println("#" +xServerIP + "\t" + xServerPort.toString() + "\t" + xId + "\t" + xOutlogPath + "\t" + xType);
        	ArrayList<Transaction> ts = this.getTransactionsList();
        	for(int i=0; i<ts.size(); i++) {
        		System.err.println(ts.get(i).getId() + " " + ts.get(i).getType() + " " + ts.get(i).getDeposit() + " " + ts.get(i).getAmount());
        	}
        }
    }
    
    
    public void connectToServer(String serverAddress, int portNumber, ArrayList<Transaction> transactions, MyXMLParser xmlParser) throws IOException {
    	System.out.println("Welcome to Bank");
    	Socket socket = new Socket(serverAddress, portNumber);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        // Consume the initial welcoming messages from the server
        for (int i = 0; i < 2; i++) {
            System.out.println(in.readLine());
        }   
        sendRequestToServer(transactions, xmlParser);
    }
    
    public void sendRequestToServer(ArrayList<Transaction> transactions, MyXMLParser xmlParser) {
    	for(int i=0; i<transactions.size(); i++) {	
    	    out.println(xmlParser.xId + "#" + xmlParser.xType + "#" + transactions.get(i).toString());
            String response;	
            try {
                response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
            } catch (IOException ex) {
                response = "Error: " + ex;
            }
            transactions.get(i).setStatus(response);
            System.out.println(response + "\n");
    	}
    }
}
