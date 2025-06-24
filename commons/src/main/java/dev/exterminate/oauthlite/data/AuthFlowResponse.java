package dev.exterminate.oauthlite.data;

import lombok.Data;

@Data
public class AuthFlowResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String scope;

    //TODO: Dependency-less JSON parsing (This could be improved)
    public static AuthFlowResponse fromJson(String json) {
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