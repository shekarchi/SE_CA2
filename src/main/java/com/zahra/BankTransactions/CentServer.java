package com.zahra.BankTransactions;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
    
    private static void addDeposit(JSONObject o) {
    	String customerName = o.get(new String("customer")).toString();
		String id = o.get("id").toString();
		BigDecimal initialBalance = new BigDecimal(o.get("initialBalance").toString());
		BigDecimal upperBound = new BigDecimal (o.get(new String("upperBound")).toString());
		Deposit d = new Deposit(customerName, id, initialBalance, upperBound);
		deposits.add(d);
    }
    
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

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    out.println(input.toUpperCase());
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
    }
}
