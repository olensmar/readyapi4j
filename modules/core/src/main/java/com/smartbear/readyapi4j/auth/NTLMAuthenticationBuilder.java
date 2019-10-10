package com.smartbear.readyapi4j.auth;

import com.smartbear.readyapi4j.client.model.Authentication;

import static com.smartbear.readyapi4j.support.Validations.validateNotEmpty;

/**
 * Builds an authentication object for NTLM authentication
 */

public class NTLMAuthenticationBuilder extends BasicAuthenticationBuilder {

    public NTLMAuthenticationBuilder(String username, String password) {
        super(username, password);
    }

    public NTLMAuthenticationBuilder setDomain(String domain) {
        authentication.setDomain(domain);
        return this;
    }

    @Override
    public Authentication build() {
        validateNotEmpty(authentication.getUsername(), "Missing username, it's a required parameter for NTLM Auth.");
        validateNotEmpty(authentication.getPassword(), "Missing password, it's a required parameter for NTLM Auth.");
        authentication.setType("NTLM");
        return authentication;
    }
}
