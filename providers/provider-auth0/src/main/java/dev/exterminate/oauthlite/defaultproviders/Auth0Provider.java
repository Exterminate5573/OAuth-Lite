package dev.exterminate.oauthlite.defaultproviders;

import dev.exterminate.oauthlite.data.AuthFlowResponse;
import dev.exterminate.oauthlite.data.BasicUser;
import dev.exterminate.oauthlite.data.DeviceCodeResponse;
import dev.exterminate.oauthlite.flows.IAuthCodeFlow;
import dev.exterminate.oauthlite.flows.IDeviceCodeFlow;
import dev.exterminate.oauthlite.providers.AbstractProvider;
import dev.exterminate.oauthlite.util.OAuthException;

import java.util.concurrent.CompletableFuture;

//TODO: audience thing
public class Auth0Provider extends AbstractProvider implements IAuthCodeFlow, IDeviceCodeFlow {

    private final String baseUrl;
    private final String redirectUri;

    /**
     * Constructs an Auth0Provider with the specified client ID, client secret, and scopes.
     *
     * @param clientId     The client ID for the OAuth provider.
     * @param clientSecret The client secret for the OAuth provider.
     * @param scopes       The scopes requested by the OAuth provider.
     */
    public Auth0Provider(String clientId, String clientSecret, String scopes, String baseUrl, String redirectUri) {
        super(clientId, clientSecret, scopes != null ? scopes : "email openid profile");
        this.baseUrl = baseUrl;
        this.redirectUri = redirectUri != null ? redirectUri : defaultRedirectUrl;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return baseUrl + "/authorize?client_id=" + getClientId() +
                "&response_type=code" +
                "&scope=" + getScopes() +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;
    }

    @Override
    public AuthFlowResponse completeFlow(String code) throws OAuthException {
        return this.completeFlow(
                code,
                baseUrl + "/oauth/token",
                "grant_type=authorization_code" +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&code=" + code +
                        "&redirect_uri=" + redirectUri
        );
    }

    @Override
    public DeviceCodeResponse startDeviceCodeFlow() throws OAuthException {
        return this.startDeviceCodeFlow(
                baseUrl + "/oauth/device/code",
                "client_id=" + getClientId() +
                        "&scope=" + getScopes()
        );
    }

    @Override
    public CompletableFuture<AuthFlowResponse> listenForDeviceCodeResponse(DeviceCodeResponse deviceCodeResponse) throws OAuthException {
        return this.listenForDeviceCodeResponse(
                deviceCodeResponse,
                baseUrl + "/oauth/token",
                "client_id=" + clientId +
                        "&grant_type=urn:ietf:params:oauth:grant-type:device_code" +
                        "&device_code=" + deviceCodeResponse.getDeviceCode()
        );
    }

    @Override
    public BasicUser getUser(String accessToken) throws OAuthException {
        String resp = this.stringUrlToResponse(baseUrl + "/userinfo", "GET", "", "Authorization: Bearer " + accessToken);
        //TODO: I don't know the proper id field for Auth0, so I'm using "sub" for now.
        //TODO: preferred_username or nickname? (I don't know enough about Auth0)
        return  BasicUser.fromJson(resp, "sub", "email", "preferred_username");
    }
}
