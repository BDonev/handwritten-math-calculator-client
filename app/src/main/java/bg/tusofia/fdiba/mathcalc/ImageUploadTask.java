package bg.tusofia.fdiba.mathcalc;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploadTask extends AsyncTask<Void, Integer, String> {
    private String urlString;
    private String filePath;

    public ImageUploadTask(String filepath, String urlString) {
        this.filePath = filepath;
        this.urlString = urlString;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            System.out.println("params = [" + params + "]");
            return uploadFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uploadFile() throws IOException {
        URL serverUrl =
                new URL(this.urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

        String boundaryString = "----SomeRandomText";
        File imageFile = new File(this.filePath);

// Indicate that we want to write to the HTTP request body
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

        OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
        BufferedWriter httpRequestBodyWriter =
                new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

// Include value from the myFileDescription text area in the post data
        httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
        httpRequestBodyWriter.write("\n\n");
        httpRequestBodyWriter.write("Log file for 20150208");

// Include the section to describe the file
        httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data;"
                + "name=\"pic\";"
                + "filename=\""+ imageFile.getName() +"\""
                + "\nContent-Type: image/jpeg\n\n");
        httpRequestBodyWriter.flush();

        // Write the actual file contents
        FileInputStream inputStreamToImageFile = new FileInputStream(imageFile);

        int bytesRead;
        byte[] dataBuffer = new byte[1024];
        while((bytesRead = inputStreamToImageFile.read(dataBuffer)) != -1) {
            outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
        }

        outputStreamToRequestBody.flush();

// Mark the end of the multipart http request
        httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
        httpRequestBodyWriter.flush();

// Close the streams
        outputStreamToRequestBody.close();
        httpRequestBodyWriter.close();

        BufferedReader httpResponseReader =
                new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String lineRead;
        while((lineRead = httpResponseReader.readLine()) != null) {
            stringBuilder.append(lineRead);
        }
        urlConnection.disconnect();
        return stringBuilder.toString();
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

