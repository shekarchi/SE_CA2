package com.zahra.BankTransactions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientMain {
public static void main(String[] args) throws Exception {
    	
    	ClientTerminal.MyXMLParser xmlParser = new ClientTerminal(). new MyXMLParser("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/"+ args[0]);
    	xmlParser.parseXmlFile();
    	
    	ArrayList<Transaction> transactions = xmlParser.getTransactionsList();
    	   	
        ClientTerminal client = new ClientTerminal();
        client.connectToServer(xmlParser.getServerIP(), new Integer(xmlParser.getServerPort()), transactions, xmlParser);
        saveToXML("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/response"+xmlParser.getId()+".xml", transactions);
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
	            e.setAttribute("type", transactions.get(i).getType());
	            e.setAttribute("depositId", transactions.get(i).getDeposit());
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
}
