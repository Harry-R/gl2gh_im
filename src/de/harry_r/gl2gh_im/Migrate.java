package de.harry_r.gl2gh_im;

import java.util.Scanner;


public class Migrate {
	private static String lab_URL, lab_token;
	private static int lab_project_id; 
	
	 public static void main( String[] args ) {
		 welcomeDialog();
		 
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
}