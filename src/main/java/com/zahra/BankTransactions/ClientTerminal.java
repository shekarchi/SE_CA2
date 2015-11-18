package com.zahra.BankTransactions;

import java.io.BufferedReader;
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

public class ClientTerminal {

	private BufferedReader in;
    private PrintWriter out;
    private String id;
    private String type;
    private String serverIP;
    private String serverPort;
    private String outlogPath;
    
    //I used given link for this class: http://www.java-samples.com/showtutorial.php?tutorialid=152
    private class MyXMLParser {
    	private String filename;
    	private Document dom;
    	private String xId;
        private String xType;
        private String xServerIP;
        private String xServerPort;
        private String xOutlogPath;
        private ArrayList<Transaction> transactionsList = null;
    	
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
    
    
    public void connectToServer(String serverAddress, int portNumber) throws IOException {
    	System.out.println("Welcome to Bank");
    	Socket socket = new Socket(serverAddress, portNumber);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        // Consume the initial welcoming messages from the server
        for (int i = 0; i < 3; i++) {
            System.out.println(in.readLine() + "\n");
        }
        /*while(true) {
        	String input = "";
        	try{
        		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        		input = br.readLine();
        			
        	}catch(IOException io){
        		io.printStackTrace();
        	}	
    	    out.println(input);
            String response;	
            try {
                response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
            } catch (IOException ex) {
                response = "Error: " + ex;
            }
            System.out.println(response + "\n");
        }*/
    }
    
    public void sendRequestToServer(ArrayList<Transaction> transactions) {
    	for(int i=0; i<transactions.size(); i++) {	
    	    out.println(transactions.get(i).toString());
            String response;	
            try {
                response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
            } catch (IOException ex) {
                response = "Error: " + ex;
            }
            System.out.println(response + "\n");
    	}
    }

    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
    	
    	MyXMLParser xmlParser = new ClientTerminal(). new MyXMLParser("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/terminal.xml");
    	xmlParser.parseXmlFile();
    	
    	ArrayList<Transaction> transactions = xmlParser.getTransactionsList();
    	   	
        ClientTerminal client = new ClientTerminal();
        client.connectToServer(xmlParser.xServerIP, new Integer(xmlParser.xServerPort));
        client.sendRequestToServer(transactions);
    }
}
