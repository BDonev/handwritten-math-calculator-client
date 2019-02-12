package bg.tusofia.fdiba.mathcalc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetOutputTask extends AsyncTask<Void, Integer, String> {

    private static final String REQUEST_METHOD_GET = "GET";
    private String urlString;

    public GetOutputTask(String urlString) {
        this.urlString = urlString;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return getOutput();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getOutput() throws IOException {
        // Maintain http url connection.
        HttpURLConnection httpConn = null;

        InputStream inputStream = null;

        // Read text input stream.
        InputStreamReader isReader = null;

        // Read text into buffer.
        BufferedReader bufReader = null;

        // Save server response text.
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // Create a URL object use page url.
            URL url = new URL(urlString);

            // Open http connection to web server.
            httpConn = (HttpURLConnection) url.openConnection();

            // Set http request method to get.
            httpConn.setRequestMethod(REQUEST_METHOD_GET);

            // Set connection timeout and read timeout value.
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);

            // Get input stream from web url connection.
            inputStream = httpConn.getInputStream();

            // Create input stream reader based on url connection input stream.
            isReader = new InputStreamReader(inputStream);

            // Create buffered reader.
            bufReader = new BufferedReader(isReader);

            // Read line of text from server response.
            String line = bufReader.readLine();

            // Loop while return line is not null.
            while (line != null) {
                // Append the text to string buffer.
                stringBuilder.append(line);

                // Continue to read text line.
                line = bufReader.readLine();
            }

            return stringBuilder.toString();
        } finally {
            if (bufReader != null) {
                bufReader.close();
            }

            if (isReader != null) {
                isReader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }
}
