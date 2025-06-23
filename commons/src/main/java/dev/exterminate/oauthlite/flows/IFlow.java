package dev.exterminate.oauthlite.flows;

import dev.exterminate.oauthlite.util.OAuthException;
import lombok.Data;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public interface IFlow {

    default String urlToResponse(URL url, String method, String encodedParams) throws OAuthException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            //TODO: User agent?
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + encodedParams.getBytes(StandardCharsets.UTF_8).length);
            connection.setRequestMethod(method);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            OutputStream stream = connection.getOutputStream();
            stream.write(encodedParams.getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();

            InputStream inputStream = null;
            if (100 <= connection.getResponseCode() && connection.getResponseCode() <= 399) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            //TODO: Grrrrr (Read all bytes is java 9+)
            byte[] bytes;
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] temp = new byte[1024];
                int read;
                while ((read = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, read);
                }
                bytes = buffer.toByteArray();
            }
            inputStream.close();
            return new String(bytes);
        } catch (IOException e) {
            throw new OAuthException(e);
        }
    }

    default String stringUrlToResponse(String url, String method, String params) throws OAuthException {
        try {
            return urlToResponse(new URL(url), method, encode(params));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
    }

    default String encode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    @Data
    class AuthFlowResponse {
        private String accessToken;
        private String refreshToken;
        private int expiresIn;
        private String scope;

        //TODO: Dependency-less JSON parsing (This could be improved)
        protected static AuthFlowResponse fromJson(String json) {
            AuthFlowResponse response = new AuthFlowResponse();

            json = json.trim().replaceAll("[{}\"]", ""); // remove braces and quotes
            String[] pairs = json.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.trim().split(":", 2);
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "access_token":
                        response.setAccessToken(value);
                        break;
                    case "refresh_token":
                        response.setRefreshToken(value);
                        break;
                    case "expires_in":
                        response.setExpiresIn(Integer.parseInt(value));
                        break;
                    case "scope":
                        response.setScope(value);
                        break;
                    default:
                        // Unknown field
                        break;
                }
            }

            return response;
        }
    }
}