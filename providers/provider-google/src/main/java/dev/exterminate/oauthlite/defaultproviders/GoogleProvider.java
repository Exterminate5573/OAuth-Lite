package dev.exterminate.oauthlite.defaultproviders;

import dev.exterminate.oauthlite.data.AuthFlowResponse;
import dev.exterminate.oauthlite.data.BasicUser;
import dev.exterminate.oauthlite.data.DeviceCodeResponse;
import dev.exterminate.oauthlite.flows.IAuthCodeFlow;
import dev.exterminate.oauthlite.flows.IDeviceCodeFlow;
import dev.exterminate.oauthlite.providers.AbstractProvider;
import dev.exterminate.oauthlite.util.OAuthException;

import java.util.concurrent.CompletableFuture;

public class GoogleProvider extends AbstractProvider implements IAuthCodeFlow, IDeviceCodeFlow {

    // Google OAuth endpoints
    private final String DEVICECODE_ENDPOINT = "https://oauth2.googleapis.com/device/code";
    private final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private final String USER_INFO_URL = "https://www.googleapis.com/userinfo/v2/me";

    private final String redirectUri;

    //TODO: Overloads
    /**
     * Constructs a GoogleProvider with the specified client ID, client secret, and scopes.
     *
     * @param clientId     The client ID of the application.
     * @param clientSecret The client secret of the application.
     * @param scopes       The scopes to request during authentication. Defaults to email and profile if null.
     */
    public GoogleProvider(String clientId, String clientSecret, String scopes, String redirectUri) {
        super(clientId, clientSecret, scopes != null ? scopes : "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");
        this.redirectUri = redirectUri != null ? redirectUri : defaultRedirectUrl;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return AUTHORIZATION_ENDPOINT +
                "?client_id=" + getClientId() +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + getScopes() +
                "&access_type=offline" +
                "&state=" + state;
    }

    @Override
    public AuthFlowResponse completeFlow(String code) throws OAuthException {
        return this.completeFlow(code, TOKEN_ENDPOINT,
                "client_id=" + getClientId() +
                "&client_secret=" + getClientSecret() +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code" +
                "&code=" + code);
    }

    @Override
    public DeviceCodeResponse startDeviceCodeFlow() throws OAuthException {
        return this.startDeviceCodeFlow(
                DEVICECODE_ENDPOINT,
                "client_id=" + clientId +
                "&scope=" + scopes
        );
    }

    @Override
    public CompletableFuture<AuthFlowResponse> listenForDeviceCodeResponse(DeviceCodeResponse deviceCodeResponse) throws OAuthException {
        return this.listenForDeviceCodeResponse(deviceCodeResponse, TOKEN_ENDPOINT,
                "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=urn:ietf:params:oauth:grant-type:device_code" +
                "&device_code=" + deviceCodeResponse.getDeviceCode()
        );
    }

    @Override
    public BasicUser getUser(String accessToken) throws OAuthException {
        String resp = this.stringUrlToResponse(USER_INFO_URL, "GET", "", "Authorization: Bearer " + accessToken);
        return BasicUser.fromJson(resp, "id", "email", "name");
    }
}
