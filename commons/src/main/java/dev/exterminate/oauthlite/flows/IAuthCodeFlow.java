package dev.exterminate.oauthlite.flows;

import com.sun.net.httpserver.HttpServer;
import dev.exterminate.oauthlite.data.AuthCodeResponse;
import dev.exterminate.oauthlite.data.AuthFlowResponse;
import dev.exterminate.oauthlite.util.OAuthException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

//TODO: PKCE support
public interface IAuthCodeFlow extends IFlow {

    //TODO: Return URL Type?
    /**
     * Returns the URL to which the user should be redirected to authorize the application.
     * This method generates a random state string to prevent CSRF attacks.
     *
     * @return An array containing the authorization URL and a random state string.
     */
    default String[] buildAuthorizationUrl() {
        String state = createState();
        return new String[]{
            buildAuthorizationUrl(state), state
        };
    }

    /**
     * Returns the URL to which the user should be redirected to authorize the application.
     *
     * @param state The state parameter to prevent CSRF attacks.
     * @return The authorization URL.
     */
    String buildAuthorizationUrl(String state);

    /**
     * Returns the URL to which the user should be redirected after authorization.
     *
     * @return The redirect URL.
     */
    default String getRedirectUrl() {
        //Random port to avoid conflicts
        return "http://localhost:7866/callback"; // Default redirect URL, can be overridden
    }

    /**
     * Creates a random state parameter to prevent CSRF attacks.
     *
     * @return A random state string.
     */
    default String createState() {
        return java.util.UUID.randomUUID().toString();
    }

    //TODO: Consider alternative http servers (such as NanoHttpd)
    //TODO: Permanent server instance to avoid creating a new one each time?
    /**
     * Creates a HTTP Server to listen for the authorization code response.
     *
     * @param state The state parameter to match the response with the request.
     * @return A CompletableFuture that will complete with the AuthCodeResponse when the response is received.
     */
    default CompletableFuture<AuthCodeResponse> listenForResponse(String state) {
        return CompletableFuture.supplyAsync(() -> {
            HttpServer server;
            try {
                server = HttpServer.create(new InetSocketAddress(7866), 0);
            } catch (IOException e) {
                //TODO: Handle IOException properly
                throw new RuntimeException(e);
                //throw new OAuthException(e);
            }

            AuthCodeResponse response = new AuthCodeResponse();
            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    if (query.contains("error")) {
                        // Send a response back to the client
                        String responseBody = "There was an error during the OAuth flow. Please try again.";
                        exchange.sendResponseHeaders(200, responseBody.length());
                        exchange.getResponseBody().write(responseBody.getBytes());
                        exchange.getResponseBody().close();
                        response.setError(true);
                        return;
                    }
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            if ("code".equals(keyValue[0])) {
                                response.setCode(keyValue[1]);
                            } else if ("state".equals(keyValue[0])) {
                                response.setState(keyValue[1]);
                            }
                        }
                    }
                }

                // Send a response back to the client
                String responseBody = "Authorization code received. You can close this window.";
                exchange.sendResponseHeaders(200, responseBody.length());
                exchange.getResponseBody().write(responseBody.getBytes());
                exchange.getResponseBody().close();
            });
            server.setExecutor(null);
            server.start();

            // Wait for the response
            while (!response.isError() && response.getCode() == null || !response.getState().equals(state)) {
                try {
                    Thread.sleep(100); // Polling interval
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    //throw new OAuthException(e);
                }
            }
            server.stop(1); // Stop the server after receiving the response
            return response;
        });
    }

    /**
     * Completes the OAuth flow using the provided authorization code response.
     *
     * @Implementation Use this function to call @{@link #completeFlow(String, String, String)} with the appropriate parameters.
     * @param code The authorization code received from the authorization server.
     * @return A FlowResponse object containing the access token and other information.
     * @throws OAuthException If there is an error during the OAuth flow.
     */
    AuthFlowResponse completeFlow(String code) throws OAuthException;

    /**
     * Completes the OAuth flow using the provided authorization code response.
     *
     * @param code The authorization code received from the authorization server.
     * @param url The URL to which the request should be sent.
     * @param params The parameters to be included in the request.
     * @return A FlowResponse object containing the access token and other information.
     * @throws OAuthException If there is an error during the OAuth flow.
     */
    default AuthFlowResponse completeFlow(String code, String url, String params) throws OAuthException {
        //TODO: append "&code=" + code to params?
        String request = stringUrlToResponse(url, "POST", params);
        if (request == null) {
            throw new OAuthException("Failed to complete the OAuth flow. The request returned null.");
        }
        return AuthFlowResponse.fromJson(request);
    }


}
