// This sample code will make a request to LinkedIn's API to retrieve and print out some
// basic profile information for the user whose access token you provide.

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

public class ProfileExample {

    public static void main(String[] args) {
        String profileUrl = "https://api.linkedin.com/v2/me";
//        String profileUrl = "GET https://api.linkedin.com/v2/people?q=vanityName&vanityName={chris-t-a36551bb}";

        // Access token for the r_liteprofile permission
        String accessToken = "AQXFLxJ1WUrbGYv0NYa-JASoOwMRGYqwrS0iLTUnVktoiXkST2Jy1sWoX9C5dW2nmOK-ZjPG4S13I52gPLseGli6DmPR3oLd4L8gxDN1M1pZK25ep3xhTZt3MJIEU4ubX7DiL0NoP7KNEv3kTey9dE_2KgeFDkxK2aHnUpWG34wr-FWshhpdFEWMpDvq1GbdZNTF5s_Mj8fkACNg_X2lFV2RDnwa1VrqL-djhOgos-WyFKUl2rvC2Rrqhlr1w4H4IPseSycOUziMWlGvCz2JUK0tEDif3OGzisM35Q0DgaKsKVeriOgSuVc35sbN_D0_lIUiI8eY1--5N2IQ7ffd3U64QeRlzQ";

        try {
            String profileData = ProfileExample.sendGetRequest(profileUrl, accessToken);
            System.out.println(profileData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendGetRequest(String urlString, String accessToken) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        con.setRequestProperty("cache-control", "no-cache");
        con.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            jsonString.append(line);
        }
        br.close();

//        JsonReader jsonReader = Json.createReader(new StringReader(jsonString.toString()));
//        JsonObject jsonObject = jsonReader.readObject();
//
//        return jsonObject;
        return jsonString.toString();
    }
}