package dev.exterminate.oauthlite.defaultproviders;

import dev.exterminate.oauthlite.flows.IAuthCodeFlow;
import dev.exterminate.oauthlite.flows.ICredentialsFlow;
import dev.exterminate.oauthlite.flows.IDeviceCodeFlow;
import dev.exterminate.oauthlite.providers.AbstractProvider;
import dev.exterminate.oauthlite.util.OAuthException;

import java.util.concurrent.CompletableFuture;

public class MicrosoftProvider extends AbstractProvider implements IAuthCodeFlow, ICredentialsFlow, IDeviceCodeFlow {

    // Microsoft OAuth endpoints
    private final String OAUTH_URL = "https://login.microsoftonline.com/";
    private final String DEVICECODE_ENDPOINT = "/oauth2/v2.0/devicecode";
    private final String AUTHORIZATION_ENDPOINT = "/oauth2/v2.0/authorize";
    private final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";

    private final String tenant;

    //TODO: Overloads
    /**
     * Constructs a MicrosoftProvider with the specified client ID, client secret, tenant, and scopes.
     *
     * @param clientId     The client ID of the application.
     * @param clientSecret The client secret of the application.
     * @param tenant       The tenant ID or "common" for multi-tenant applications.
     * @param scopes       The scopes to request during authentication.
     */
    public MicrosoftProvider(String clientId, String clientSecret, String tenant, String scopes) {
        super(clientId, clientSecret, scopes != null ? scopes : "email openid profile offline_access");
        this.tenant = tenant != null ? tenant : "common"; // Default to common if tenant is not provided
    }

    //TODO: Make this async?
    @Override
    public DeviceCodeResponse startDeviceCodeFlow() throws OAuthException {
        //TODO: Custom scope
        return this.startDeviceCodeFlow(
                OAUTH_URL + tenant + DEVICECODE_ENDPOINT,
                "client_id=" + clientId
                        + "&scope=email openid profile offline_access");
    }


    @Override
    public CompletableFuture<AuthFlowResponse> listenForDeviceCodeResponse(DeviceCodeResponse deviceCodeResponse) throws OAuthException {
        String params = "client_id=" + clientId + "&grant_type=urn:ietf:params:oauth:grant-type:device_code&device_code=" + deviceCodeResponse.getDeviceCode();
        return this.listenForDeviceCodeResponse(deviceCodeResponse, OAUTH_URL + tenant + TOKEN_ENDPOINT, params);
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return OAUTH_URL + tenant + AUTHORIZATION_ENDPOINT +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&scope=" + scopes +
                "&redirect_uri=" + getRedirectUrl() +
                "&response_mode=query" +
                "&state=" + state;
    }

    @Override
    public AuthFlowResponse completeFlow(String code) throws OAuthException {
        return this.completeFlow(code, OAUTH_URL + tenant + TOKEN_ENDPOINT,
                "client_id=" + clientId +
                "&grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + getRedirectUrl() +
                "&scope=" + scopes +
                //TODO: Optional client secret
                "&client_secret=" + clientSecret);
    }

    @Override
    public AuthFlowResponse credentialsLogin(String username, String password) throws OAuthException {
        String params = "client_id=" + clientId +
                "&grant_type=password" +
                "&username=" + username +
                "&password=" + password +
                "&scope=" + scopes +
                //TODO: Optional client secret
                "&client_secret=" + clientSecret;

        return this.credentialsLogin_(OAUTH_URL + tenant + TOKEN_ENDPOINT, params);
    }
}
