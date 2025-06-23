package dev.exterminate.oauthlite.providers;

import dev.exterminate.oauthlite.flows.IFlow;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractProvider {

    protected String clientId;
    protected String clientSecret;
    //TODO: Make scopes an enum/some other data type for easier management?
    protected String scopes;

    /**
     * Constructs an AbstractProvider with the specified client ID, client secret, and scopes.
     *
     * @param clientId     The client ID for the OAuth provider.
     * @param clientSecret The client secret for the OAuth provider.
     * @param scopes       The scopes requested by the OAuth provider.
     */
    public AbstractProvider(String clientId, String clientSecret, String scopes) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
    }

    /**
     * Returns the Implemented Flows for this provider.
     *
     * @return List of implemented flows.
     */
    public List<IFlow> getFlows() {
        return Arrays.stream(this.getClass().getInterfaces())
                .filter(IFlow.class::isAssignableFrom)
                .map(IFlow.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
