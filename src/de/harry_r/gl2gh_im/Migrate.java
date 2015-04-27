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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Migrate {
	static String lab_URL, lab_token, lab_final_url, hub_url, hub_name,
			hub_repo, hub_token, hub_final_url, author_name;
	static String charset = "UTF-8";

	static int lab_project_id;
	static int status = 0;

	static boolean time_and_name;

	static JSONArray input_json, output_json = new JSONArray(),
			edit_json = new JSONArray();

	public static void main(String[] args) throws IOException, JSONException, ParseException {
		welcomeDialog();
		performHttpGet();
		input_json = readJson();
		System.out.println(input_json);
		createOutputJson();
		if (time_and_name) {
			addTimeName();
		}
		createIssues();
		createStateArray();
		editIssues();
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
		System.out.println("Please enter your GitHub API autentication token:");
		hub_token = reader.next();
		System.out
				.println("Do you want to write originally creator's name and timestamp into the description? If not, they will be lost! (y/n)");
		String input = "42";
		// read input until user does a correct input
		while (!input.equals("y") && !input.equals("n")) {
			input = reader.next();
		}
		switch (input) {
		case ("y"): {
			time_and_name = true;
			break;
		}
		case ("n"): {
			time_and_name = false;
			break;
		}
		}
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
			handleHttpStatusCode(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONArray readJson() throws IOException, JSONException {
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
			// build json object from String and return
			JSONArray json = new JSONArray(sb.toString());
			return json;
		} finally {
			// close stream in case of exception
			is.close();
			System.out.println("Connection closed!");
		}
	}

	private static void handleHttpStatusCode(int status) {
		switch (status) {
		case (0): {
			System.out.println("Connection failed!");
			break;
		}
		case (200): {
			System.out.println("OK");
			break;
		}
		case (201): {
			System.out.println("Succesfully created");
			break;
		}
		case (400): {
			System.out.println("Bad request!");
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
	}

	private static void createOutputJson() throws JSONException {
		// add relevant parameters output_json
		for (int i = 0; i < input_json.length(); i++) {
			JSONObject in_object = input_json.getJSONObject(i);
			JSONObject out_object = new JSONObject();
			out_object.put("milestone", in_object.get("milestone"));
			out_object.put("title", in_object.get("title"));
			out_object.put("body", in_object.get("description"));
			out_object.put("labels", in_object.get("labels"));
			output_json.put(out_object);
		}
	}

	private static void addTimeName() throws JSONException, ParseException {
		for (int i = 0; i < input_json.length(); i++) {
			// get author name
			String author_name = (String) input_json.getJSONObject(i)
					.getJSONObject("author").get("name");
			// get and reformat date
			String json_date_string = input_json.getJSONObject(i).getString("created_at")
					.replace('T', ' ').replace('Z', ' ');
			// Format example: 2015-04-26T22:42:04.897Z  -> 2015-04-26 22:42:04.897
			DateFormat json_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.ENGLISH);
			Date date = json_format.parse(json_date_string);
			DateFormat out_format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
			String out_date = out_format.format(date);
			// get description, add data
			String description_string = output_json.getJSONObject(i)
					.get("body")
					+ "\n" + "\n"
					+ "_This issue was migrated from GitLab. Original author is " + author_name
					+ "._\n"
					+ "_It was originally created " + out_date + "._";
			output_json.getJSONObject(i).put("body", description_string);
		}
	}

	private static void createIssues() throws MalformedURLException,
			IOException, JSONException {
		// send each json object alone
		for (int i = 0; i < output_json.length(); i++) {
			// reverse to bring the issues in right order
			JSONObject out_object = output_json.getJSONObject(output_json
					.length() - (i + 1));
			System.out.println(out_object);
			// content type
			String type = "application/json";
			// create URL
			hub_final_url = "https://api.github.com/repos/" + hub_name + "/"
					+ hub_repo + "/issues" + "?access_token=" + hub_token;
			// create and open connection
			HttpURLConnection connection = (HttpURLConnection) new URL(
					hub_final_url).openConnection();
			// print URL for debugging
			System.out.println(connection);
			// set connection parameters
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", type);
			connection.setRequestProperty("Content-Length",
					String.valueOf(out_object.toString().length()));
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			// get and write output stream
			OutputStream os = connection.getOutputStream();
			os.write(out_object.toString().getBytes());
			// get and print http status codes
			status = connection.getResponseCode();
			System.out.println(status);
			handleHttpStatusCode(status);
		}
	}

	private static void createStateArray() throws JSONException {
		// create an array with number and state for editing the issues
		for (int i = 0; i < input_json.length(); i++) {
			JSONObject in_object = input_json.getJSONObject(i);
			JSONObject state_object = new JSONObject();
			state_object.put("state", in_object.get("state"));
			edit_json.put(state_object);
		}
	}

	private static void editIssues() throws MalformedURLException, IOException,
			JSONException {
		// send each json object alone
		for (int i = 0; i < edit_json.length(); i++) {
			// reverse to bring the issues in right order
			JSONObject edit_object = edit_json.getJSONObject(edit_json.length()
					- (i + 1));
			System.out.println(edit_object);
			// content type
			String type = "application/json";
			// create URL, i+1 is the issue number
			hub_final_url = "https://api.github.com/repos/" + hub_name + "/"
					+ hub_repo + "/issues/" + (i + 1) + "?access_token="
					+ hub_token;
			// create and open connection
			HttpURLConnection connection = (HttpURLConnection) new URL(
					hub_final_url).openConnection();
			// print URL for debugging
			System.out.println(connection);
			// set connection parameters
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", type);
			connection.setRequestProperty("Content-Length",
					String.valueOf(edit_object.toString().length()));
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			// get and write output stream
			OutputStream os = connection.getOutputStream();
			os.write(edit_object.toString().getBytes());
			// get and print http status codes
			status = connection.getResponseCode();
			System.out.println(status);
			handleHttpStatusCode(status);
		}
	}
}
