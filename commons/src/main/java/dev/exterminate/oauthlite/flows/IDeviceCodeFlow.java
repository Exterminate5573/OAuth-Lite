package dev.exterminate.oauthlite.flows;

import dev.exterminate.oauthlite.util.OAuthException;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

public interface IDeviceCodeFlow extends IFlow {

    /**
     * Starts the Device Code Flow by sending a request to the OAuth server.
     *
     * @Implementation Use this function to call @{@link #startDeviceCodeFlow(String, String)} with the appropriate URL and parameters.
     * @return A DeviceCodeResponse object containing the device code and other details.
     * @throws OAuthException If an error occurs during the OAuth process.
     */
    DeviceCodeResponse startDeviceCodeFlow() throws OAuthException;

    /**
     * Starts the Device Code Flow by sending a request to the OAuth server with the specified URL and parameters.
     *
     * @param url The URL to send the request to.
     * @param params The parameters to include in the request.
     * @return A DeviceCodeResponse object containing the device code and other details.
     * @throws OAuthException If an error occurs during the OAuth process.
     */
    default DeviceCodeResponse startDeviceCodeFlow(String url, String params) throws OAuthException {
        String response = stringUrlToResponse(
                url,
                "POST",
                params
        );

        // Parse the response into a DeviceCodeResponse object
        return DeviceCodeResponse.fromJson(response);
    }

    /**
     * Listens for the device code response by polling the verification URL.
     *
     * @Implementation Use this function to call @{@link #listenForDeviceCodeResponse(DeviceCodeResponse, String, String)} with the appropriate parameters.
     * @param deviceCodeResponse The DeviceCodeResponse object containing the device code and other details.
     * @return A CompletableFuture that will complete with the FlowResponse when the device code is verified.
     * @throws OAuthException If an error occurs during the OAuth process.
     */
    CompletableFuture<AuthFlowResponse> listenForDeviceCodeResponse(DeviceCodeResponse deviceCodeResponse) throws OAuthException;

    /**
     * Listens for the device code response by polling the verification URL with specified parameters.
     *
     * @param deviceCodeResponse The DeviceCodeResponse object containing the device code and other details.
     * @param verifUrl The verification URL to poll.
     * @param params The parameters to include in the request.
     * @return A CompletableFuture that will complete with the FlowResponse when the device code is verified.
     * @throws OAuthException If an error occurs during the OAuth process.
     */
    default CompletableFuture<AuthFlowResponse> listenForDeviceCodeResponse(DeviceCodeResponse deviceCodeResponse, String verifUrl, String params) throws OAuthException {
        return CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < deviceCodeResponse.getExpiresIn(); i += deviceCodeResponse.getInterval()) {
                try {
                    Thread.sleep(deviceCodeResponse.getInterval() * 1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    String response = stringUrlToResponse(verifUrl, "POST", params);

                    if (response.contains("error")) {

                        if (response.contains("authorization_pending")) {
                            continue;
                        } else {
                            throw new OAuthException("Error during device code flow: " + response);
                        }
                    }

                    // If we reach here, the response is successful
                    return AuthFlowResponse.fromJson(response);
                } catch (OAuthException e) {
                    //TODO: Log the error
                    e.printStackTrace();
                    continue;
                }
            }
            return null;
        });
    }

    @Data
    class DeviceCodeResponse {
        private String deviceCode;
        private String userCode;
        private String verificationUri;
        private int expiresIn;
        private int interval;

        //TODO: Dependency-less JSON parsing (This could be improved)
        protected static DeviceCodeResponse fromJson(String json) {
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
}
