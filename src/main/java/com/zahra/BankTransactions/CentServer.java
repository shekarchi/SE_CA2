package com.zahra.BankTransactions;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CentServer {
	
	static String port = "";
    static String outLog = "";
    static ArrayList<Deposit> deposits = null;
    
    //I used given link for these functions: http://www.mkyong.com/java/json-simple-example-read-and-write-json/
    private static void parseJSONFile(String filename) {
    	JSONParser parser = new JSONParser();
        try {

    		Object obj = parser.parse(new FileReader(filename));
    		JSONObject jsonObject = (JSONObject) obj;

    		port = (String) jsonObject.get("port").toString();
    		
    		JSONArray transactions = (JSONArray) jsonObject.get("deposits");
    		Iterator<JSONObject> iterator = transactions.iterator();
    		while (iterator.hasNext()) {
    			addDeposit((JSONObject)(iterator.next()));
    		}
    		
    		outLog = (String) jsonObject.get("outLog");

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    }
    
    private static void addDeposit(JSONObject o) {
    	String customerName = o.get(new String("customer")).toString();
		String id = o.get("id").toString();
		BigDecimal initialBalance = new BigDecimal(o.get("initialBalance").toString());
		BigDecimal upperBound = new BigDecimal (o.get(new String("upperBound")).toString());
		Deposit d = new Deposit(customerName, id, initialBalance, upperBound);
		deposits.add(d);
    }
    
    private static void printParsedFile () {
    	System.err.println(port + "\t" + outLog);
    	for(int i=0; i<deposits.size(); i++)
    		System.err.println(deposits.get(i).getCustomer() + " " + deposits.get(i).getId() + " " + deposits.get(i).getInitialBalance() + " " + deposits.get(i).getUpperBound());
    }
    
	/**
     * Application method to run the server runs in an infinite loop
     * listening on given port.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        
        deposits = new ArrayList<Deposit>();
        
        parseJSONFile("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/core.json");
        printParsedFile ();
        
        int clientNumber = 0;
        int portNumber = new Integer(port);
        ServerSocket listener = new ServerSocket(portNumber);
        try {
            while (true) {
                new Service(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A private thread to handle service requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Service extends Thread {
        private Socket socket;
        private int clientNumber;

        public Service(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            System.out.println("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the service.
         */
        public void run() {
            try {
                // Ensure output is flushed after every newline. 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit");

                while (true) {
                    String input = in.readLine();
                    
                    System.err.println(input);
                    
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    String result = performRequest(input);
                    
                    out.println(result);
                }
            } catch (IOException e) {
                System.out.println("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close a socket, what's going on?");
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }
        }
        
        private String performRequest(String request) {
        	String result = "failed";
        	
        	//terminalId#treminalTye#1#requestType#amount#deposit
        	//0         #1          #2#3          #4     #5
         	String[] words = request.split("#");
         	
         	System.err.println(words[0] + " " + words[1] + " " + words[2] + " " + words[3] + " " + words[4]);
         	
        	Deposit d = findDepositById(words[5]);
        	if(d == null)
        		return "Deposit id is invalid.";
        	try {
        		d.applyRequestOnDepo(words[3], new BigDecimal(words[4]));
        		result = "success";
        		saveToLogFile(outLog, "terminalId: " + words[0]
        							+ " terminalType: " + words[1]
        							+ " transactionId: " + words[2]
        							+ " requestType: " + words[3]
        							+ " amount: " + words[4]
        							+ " depositId: " + words[5]
        							+ " result: " + result);
        	} catch(Exception e) {
        		saveToLogFile(outLog, "terminalId: " + words[0]
						+ " terminalType: " + words[1]
						+ " transactionId: " + words[2]
						+ " requestType: " + words[3]
						+ " amount: " + words[4]
						+ " depositId: " + words[5]
						+ " result: " + result);
        		System.out.println(e.toString() + " happened.");
        	}
        	return result;
        }
        
        private Deposit findDepositById(String depoId) {
        	for(int i=0; i<deposits.size(); i++) {
        		if(deposits.get(i).getId().equals(depoId))
        			return deposits.get(i);
        	}
        	System.out.println("Invalid deposit id #" + depoId);
        	return null;
        }
        
        private synchronized void saveToLogFile(String filename, String line) {
        	PrintWriter out = null;
        	try{
        		out = new PrintWriter(new BufferedWriter(new FileWriter("/home/zahra/eclipseWorkspace/BankTransactions/src/main/java/"+filename, true)));
        	    out.println(line);
        	}catch (IOException e) {
        	    //exception handling left 
        	} finally {
        		out.close();
        	}
        }
    }
}
