package de.harry_r.gl2gh_im;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Migrate {
	static String lab_URL, lab_token, lab_final_url, hub_url, hub_name,
			hub_repo, hub_token, hub_final_url;
	static String charset = "UTF-8";

	static int lab_project_id;
	static int status = 0;

	static JSONObject input_json, output_json = new JSONObject();

	public static void main(String[] args) throws IOException, JSONException {
		welcomeDialog();
		performHttpGet();
		input_json = readJson();
		System.out.println(input_json);
		createOutputJson();
		performHttpPost();
	}

	public static void welcomeDialog() {
		// send welcome message
		System.out.println("Welcome at GitLab to GitHub issue migrating!");
		// read user inputs
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter your GitLab URL:");
		lab_URL = reader.next();
		System.out.println("Please enter your GitLab project id:");
		lab_project_id = reader.nextInt();
		System.out.println("Please enter your GitLab private token:");
		lab_token = reader.next();
		System.out.println("Please enter your GitHub name:");
		hub_name = reader.next();
		System.out.println("Please enter your GitHub repo:");
		hub_repo = reader.next();
		System.out.println("Please enter your GitHub API Autentication token:");
		hub_token = reader.next();
		reader.close();
	}

	// authenticate with private token
	public static void performHttpGet() {
		try {
			// open connection
			lab_final_url = lab_URL + "/api/v3/projects/" + lab_project_id
					+ "/issues" + "?" + "private_token=" + lab_token;
			URLConnection connection = new URL(lab_final_url).openConnection();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONObject readJson() throws IOException, JSONException {
		// open input stream
		InputStream is = new URL(lab_final_url).openStream();
		try {
			// create a buffered reader and String builder
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					charset));
			StringBuilder sb = new StringBuilder();
			// read from input stream and append to string builder
			int i, j = 0;
			while ((i = rd.read()) >= 0) {
				sb.append((char) i);
				j++;
			}
			// delete [] around the string
			sb.deleteCharAt(0);
			sb.deleteCharAt(sb.length() - 1);
			// build json object from String and return
			JSONObject json = new JSONObject(sb.toString());
			return json;
		} finally {
			// close stream in case of exception
			is.close();
			System.out.println("Connection closed!");
		}
	}

	private static void createOutputJson() throws JSONException {
		// add relevant parameters to output_json
		output_json.put("milestone", input_json.get("milestone"));
		output_json.put("title", input_json.get("title"));
		output_json.put("body", input_json.get("description"));
		output_json.put("labels", input_json.get("labels"));
		System.out.print(output_json);
	}

	private static void performHttpPost() throws MalformedURLException,
			IOException {
		// content type
		String type = "application/json";
		// create URL
		hub_final_url = "https://api.github.com/repos/" + hub_name + "/" + hub_repo
				+ "/issues" + "?access_token=" + hub_token;
		//create and open connection
		HttpURLConnection connection = (HttpURLConnection) new URL(
				hub_final_url).openConnection();
		// print URL for debugging
		System.out.println(connection);
		// set connection parameters
		connection.setRequestProperty("Accept-Charset", charset);
		connection.setRequestProperty("Content-Type", type);
		connection.setRequestProperty("Content-Length",
				String.valueOf(output_json.toString().length()));
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		// get and write output stream
		OutputStream os = connection.getOutputStream();
		os.write(output_json.toString().getBytes());
		// get and print http response code
		status = connection.getResponseCode();
		System.out.println(status);
	}

}
