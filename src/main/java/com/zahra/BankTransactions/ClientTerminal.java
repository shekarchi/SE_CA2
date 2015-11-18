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

    //I used given link for this class: http://stackoverflow.com/questions/7373567/java-how-to-read-and-write-xml-files
    public static void saveToXML(String filename, ArrayList<Transaction> transactions) {
        Document wdom;
        Element e = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            wdom = db.newDocument();
            Element rootEle = wdom.createElement("terminal");

            for (int i=0; i<transactions.size(); i++) {
	            e = wdom.createElement("transaction");
	            e.setAttribute("transactionId", transactions.get(i).getId());
	            e.setAttribute("transactionStatus", transactions.get(i).getStatus());
	            rootEle.appendChild(e);
	        }
            wdom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                tr.transform(new DOMSource(wdom), new StreamResult(new FileOutputStream(filename)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
    
    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
    	
    	MyXMLParser xmlParser = new ClientTerminal(). new MyXMLParser("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/"+ args[0]);
    	xmlParser.parseXmlFile();
    	
    	ArrayList<Transaction> transactions = xmlParser.getTransactionsList();
    	   	
        ClientTerminal client = new ClientTerminal();
        client.connectToServer(xmlParser.xServerIP, new Integer(xmlParser.xServerPort), transactions, xmlParser);
        saveToXML("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/response"+xmlParser.xId+".xml", transactions);
    }
}
