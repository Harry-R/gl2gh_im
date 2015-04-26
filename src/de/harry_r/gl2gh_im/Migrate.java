package de.harry_r.gl2gh_im;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class Migrate {
	static String lab_URL, lab_token;
	static String charset = "UTF-8";
	
	static int lab_project_id; 
	static int status = 0;
	
	 public static void main( String[] args ) {
		 welcomeDialog();
		 getIssues();
	  }
	 
	 public static void welcomeDialog () {
		// send welcome message
		System.out.println( "Welcome at GitLab to GitHub issue migrating!" );
		// read user inputs (GitLab URL and private token)
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter your GitLab URL:");
		lab_URL = reader.next();
		System.out.println("Please enter your GitLab project id:");
		lab_project_id = reader.nextInt();
		System.out.println("Please enter your GitLab private token:");
		lab_token = reader.next(); 
		reader.close();
	 }
	 
	// authenticate with private token
		public static void getIssues() {
			try {
				// open connection
				URLConnection connection = new URL(lab_URL + "/api/v3/projects/"
						+ lab_project_id + "/issues" + "?" + "private_token="
						+ lab_token).openConnection();
				// print URL for debugging
				System.out.println(connection);
				connection.setRequestProperty("Accept-Charset", charset);
				// handle HTTP status code
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				status = httpConnection.getResponseCode();
				System.out.println(status);
				switch (status) {
					case (0): {
						System.out.println("Connection failed!");
						break;
					}
					case (200): {
						System.out.println("OK");
						break;
					}
					case (401): {
						System.out.println("Authentication failed!");
						break;
					}
					case (404): {
						System.out.println("Requested recource not found!");
						break;
					}
					default: {
						System.out.println("There was an unknown error!");
						break;
					}
				}
				//TODO: handle HTTP content
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}