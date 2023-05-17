package ch.uzh.ifi.hase.soprafs23.utility.memeapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImgflipClient implements IMemeApi {
    private final Logger log = LoggerFactory.getLogger(ImgflipClient.class);

    private static final String ENDPOINT = "https://api.imgflip.com";

    public ApiResponse getTemplates() {

        Scanner scanner = null;
        try {
            URL url = new URL(ENDPOINT + "/get_memes");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed with HTTP error code: " + responseCode);
            }

            scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\A").next();

            ObjectMapper mapper = new ObjectMapper();
            ApiResponse apiResponse = mapper.readValue(response, ApiResponse.class);

            scanner.close();

            return apiResponse;

        } catch (IOException e) {
            log.error(e.toString());
        } finally {
            if (scanner != null)
                scanner.close();
        }

        return null;
    }

    public static class ApiResponse {
        @JsonProperty("success")
        public boolean success;

        @JsonProperty("data")
        public Data data;
    }

    public static class Data {
        @JsonProperty("memes")
        public List<Meme> memes;
    }

    public static class Meme {
        @JsonProperty("id")
        public String id;

        @JsonProperty("name")
        public String name;

        @JsonProperty("url")
        public String url;

        @JsonProperty("width")
        public int width;

        @JsonProperty("height")
        public int height;

        @JsonProperty("box_count")
        public int boxCount;

        @JsonProperty("captions")
        public int captions;

    }
}