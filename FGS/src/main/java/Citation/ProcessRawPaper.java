package Citation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProcessRawPaper {


    public void getPaper(String filePath) throws IOException {

        if (filePath == null || filePath.length() == 0) {
            System.out.println("No Input Node Types File Path!");
            return;
        }





        File file = new File(filePath);
        File file1 = new File("C:\\Users\\Nick\\Downloads\\cord-19_2020-05-26\\2020-05-26\\paperIds");
        BufferedWriter br1 = new BufferedWriter(new FileWriter(file1));
        BufferedReader br = new BufferedReader(new FileReader(file));



        String st;
        int i = 0;

        while ((st = br.readLine()) != null) {


            String[] meta = st.split(",");

            if (isNumeric(meta[meta.length -1])) {
                i++;
                String id = meta[meta.length -1];
                System.out.println(i);
                br1.write(id.trim() + "\n");


            }


        }
    }
    public void printEdgeLabel(String filePath) throws IOException, InterruptedException {

        if (filePath == null || filePath.length() == 0) {
            System.out.println("No Input Node Types File Path!");
            return;
        }

        File file = new File(filePath);
        File file1 = new File("C:\\Users\\Nick\\Downloads\\cord-19_2020-05-26\\2020-05-26\\graphPaper6");
        BufferedWriter br1 = new BufferedWriter(new FileWriter(file1));
        BufferedReader br = new BufferedReader(new FileReader(file));


        String st;
        int i = 0;

        while ((st = br.readLine()) != null) {

              String id = st.trim();

            try {
                URL url = new URL("https://api.semanticscholar.org/v1/paper/MAG:"+id);
//                URL url = new URL("https://api.semanticscholar.org/v1/author/1741101");
                // Open a connection(?) on the URL(?) and cast the response(??)
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Now it's "open", we can set the request method, headers etc.
                connection.setRequestProperty("accept", "application/json");
                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_FORBIDDEN) {
                    connection.disconnect();
                    System.out.println("Wait............");
                    Thread.sleep(305000);

                }
                if (status !=  HttpURLConnection.HTTP_OK) {
                    continue;
                }
                // This line makes the request
                InputStream responseStream = connection.getInputStream();
                i++;
                // Manually converting the response body InputStream to APOD using Jackson

                Scanner s = new Scanner(responseStream).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";
                System.out.println(i);
                System.out.println(st);
                br1.write(result + "\n");

            } catch (java.net.SocketTimeoutException e) {
                System.out.println("Timeout");
                e.printStackTrace();
            } catch (javax.net.ssl.SSLException e) {
                System.out.println("SSL");
                e.printStackTrace();
            }





        }
        br.close();
        br1.close();
    }
    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public void getJason (String filepath) throws IOException {
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        int i = 0;
        int j = 0;

        while ((st = br.readLine()) != null) {

            if (st.trim().length() == 0 || st.equals("[") || st.equals("]")) {
                continue;
            }



            JSONObject obj = new JSONObject(st);

            i++;
        }
    }





    public  static void main (String args[]) throws IOException, InterruptedException {
         ProcessRawPaper p = new ProcessRawPaper();
         p.printEdgeLabel("C:\\Users\\Nick\\Downloads\\cord-19_2020-05-26\\2020-05-26\\paperIds");
        //p.getPaper("C:\\Users\\Nick\\Downloads\\cord-19_2020-05-26\\2020-05-26\\metadata.csv");
    }

}
