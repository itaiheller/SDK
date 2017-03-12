package com.cx.sdk.infrastructure.providers;

import com.checkmarx.plugin.common.api.CxSamlClient;
import com.checkmarx.plugin.common.api.CxSamlClientImpl;
import com.checkmarx.plugin.common.webbrowsering.SAMLLoginData;
import com.cx.sdk.application.contracts.providers.LoginProvider;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.domain.Session;
import com.cx.sdk.domain.exceptions.SdkException;
import com.cx.sdk.infrastructure.CxRestClient;
import com.cx.sdk.infrastructure.CxSoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ehuds on 2/25/2017.
 */
public class LoginProviderImpl implements LoginProvider {

    SDKConfigurationProvider sdkConfigurationProvider;
    CxRestClient cxRestClient;
    CxSoapClient cxSoapClient;
    Logger logger = LoggerFactory.getLogger(LoginProviderImpl.class);


    @Inject
    public LoginProviderImpl(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        cxRestClient = new CxRestClient(sdkConfigurationProvider);
        cxSoapClient = new CxSoapClient(sdkConfigurationProvider);
    }

    @Override
    public Session login(String userName, String password) throws SdkException {
        return new Session(cxSoapClient.login(userName, password),
                            cxRestClient.login(userName, password));
    }

    @Override
    public Session ssoLogin() throws SdkException {
        return new Session(cxSoapClient.ssoLogin(),
                cxRestClient.ssoLogin());
    }

    @Override
    public Session samlLogin() throws SdkException {
        CxSamlClient cxSamlClient = new CxSamlClientImpl(sdkConfigurationProvider.getCxServerUrl(),
                                                         sdkConfigurationProvider.getCxOriginName());
        SAMLLoginData samlLoginData = null;
        try {
            samlLoginData = cxSamlClient.login();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to preform saml login to server: %s",
                    sdkConfigurationProvider.getCxServerUrl().toString());
            logger.info(errorMessage, e);
            throw new SdkException(errorMessage, e);
        }

        if (samlLoginData.wasCanceled)
            return null;

        return new Session(samlLoginData.getCxWSResponseLoginData().getSessionId(), extractCxCoockies(samlLoginData));
    }

    private Map<String, String> extractCxCoockies(SAMLLoginData samlLoginData) {
        Map coockies = new HashMap<String, String>();
        coockies.put(samlLoginData.getCxCookie().getName(), samlLoginData.getCxCookie().getValue());
        coockies.put(samlLoginData.getCXRFCookie().getName(), samlLoginData.getCXRFCookie().getValue());
        return coockies;
    }
}
