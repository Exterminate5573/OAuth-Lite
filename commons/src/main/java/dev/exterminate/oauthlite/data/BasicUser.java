package dev.exterminate.oauthlite.data;

import dev.exterminate.oauthlite.util.OAuthException;
import lombok.Data;

import java.io.IOException;

@Data
public class BasicUser {
    private String id;
    private String email;
    private String username;

    public static BasicUser fromJson(String json, String idKey, String emailKey, String usernameKey) {
        BasicUser user = new BasicUser();

        json = json.trim().replaceAll("[{}\"]", ""); // remove braces and quotes
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals(idKey)) {
                user.setId(value);
            } else if (key.equals(emailKey)) {
                user.setEmail(value);
            } else if (key.equals(usernameKey)) {
                user.setUsername(value);
            }
        }

        return user;
    }
}