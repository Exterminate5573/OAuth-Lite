package dev.exterminate.oauthlite.flows;

import dev.exterminate.oauthlite.util.OAuthException;

public interface ICredentialsFlow extends IFlow {

    /**
     * Executes the credentials flow with the provided username and password.
     *
     * @Implementation Use this function to call @{@link #credentialsLogin_(String, String)} with the appropriate URL and parameters.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A FlowResponse containing the result of the flow execution.
     */
    AuthFlowResponse credentialsLogin(String username, String password) throws OAuthException;

    /**
     * Executes the credentials flow with the provided username, password, and additional parameters.
     *
     * @param url The URL to which the request should be sent.
     * @param params Additional parameters to include in the request.
     * @return A FlowResponse containing the result of the flow execution.
     */
    default AuthFlowResponse credentialsLogin_(String url, String params) throws OAuthException {
        String response = stringUrlToResponse(
                url,
                "POST",
                params
        );

        return AuthFlowResponse.fromJson(response);
    }

}
