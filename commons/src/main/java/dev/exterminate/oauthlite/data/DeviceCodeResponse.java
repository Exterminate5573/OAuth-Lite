package dev.exterminate.oauthlite.data;

import lombok.Data;

@Data
public class DeviceCodeResponse {
    private String deviceCode;
    private String userCode;
    private String verificationUri;
    private int expiresIn;
    private int interval;

    //TODO: Dependency-less JSON parsing (This could be improved)
    public static DeviceCodeResponse fromJson(String json) {
        DeviceCodeResponse response = new DeviceCodeResponse();

        json = json.trim().replaceAll("[{}\"]", ""); // remove braces and quotes
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.trim().split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "device_code":
                    response.setDeviceCode(value);
                    break;
                case "user_code":
                    response.setUserCode(value);
                    break;
                case "verification_uri":
                    response.setVerificationUri(value);
                    break;
                case "expires_in":
                    response.setExpiresIn(Integer.parseInt(value));
                    break;
                case "interval":
                    response.setInterval(Integer.parseInt(value));
                    break;
                case "message":
                    // Ignored in your POJO
                    break;
                default:
                    // Unknown field
                    break;
            }
        }

        return response;
    }
}
