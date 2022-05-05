package no.hvl.dat110.aciotdevice.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;

public class RestClient {

	
	
	public RestClient() {
		// TODO Auto-generated constructor stub
	}

	private static String logpath = "/accessdevice/log";

	public void doPostAccessEntry(String message) {
		send("POST", logpath, AccessMessage.class, new AccessMessage(message));
	}
	
	private static String codepath = "/accessdevice/code";
	
	public AccessCode doGetAccessCode() {
		
		AccessCode code = send("GET",codepath,AccessCode.class);
		
		return code;
	}
	
	private static <T> T send(String method, String endpoint, Class<T> clazz) {
		return send(method, endpoint, clazz, null);
	}
	
	private static <T> T send(String method, String endpoint,Class<T> clazz, Object body){
		try(Socket s = new Socket(Configuration.host,Configuration.port)){
			String request = 
					method + " "
				   +endpoint+" HTTP/1.1\r\n"
			   	  + "Accept: application/json\r\n"
			   	  + "Host: localhost\r\n"
			   	  + "Connection: close\r\n";
			if (body != null) {
				
				Gson gson = new Gson();
				String requestBody = gson.toJson(body);
				request += "Content-type: application/json\r\n"
						+  "Content-length: " + requestBody.length() + "\r\n"
						+ "\r\n"
						+ requestBody;
			}
			request += "\r\n";
			
			OutputStream stream = s.getOutputStream();
			
			PrintWriter pw = new PrintWriter(stream);
			pw.print(request);
			
			if (body != null) {
				
			}
			
			pw.flush();
			
			InputStream in = s.getInputStream();
			
			Scanner sc = new Scanner(in);
			
			StringBuilder sb = new StringBuilder();
			
			//We retrieve the status line to check the status code
			String status = sc.nextLine();
			
			//General syntax is [HTTP VERSION][STATUS CODE][REASON PHRASE]
			String[] tokens = status.split(" ");
			
			boolean header = true;
			while(sc.hasNext()) {
				String line = sc.nextLine();
				if (header) {
					System.out.println(line);
				}
				else {
					sb.append(line);
				}
				
				if (line.isEmpty()) {
					header = false;
				}
			}
			
			sc.close();
			
			if (Integer.parseInt(tokens[1]) / 100 != 2) {
				throw new RuntimeException("Recieved " + tokens[1] + " " + tokens[2] + ": " + sb.toString());
			}
			
			Gson gson = new Gson();
			T respBody = gson.fromJson(sb.toString(), clazz);
			
			return respBody;
			
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
