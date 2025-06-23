package dev.exterminate.oauthlite;

import dev.exterminate.oauthlite.providers.AbstractProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class OAuthLite {

    /**
     * Creates a new OAuthLite instance with the specified provider.
     *
     * @param provider The OAuth provider to use.
     */
    @Getter
    private final AbstractProvider provider;

}
