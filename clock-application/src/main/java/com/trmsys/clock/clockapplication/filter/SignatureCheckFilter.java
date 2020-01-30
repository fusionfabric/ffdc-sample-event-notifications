package com.trmsys.clock.clockapplication.filter;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.trmsys.clock.clockapplication.configuration.MessagingConfiguration;
import com.trmsys.clock.clockapplication.exceptions.JwksException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

@Component
public class SignatureCheckFilter extends GenericFilterBean {

    private final static Logger LOG = LoggerFactory.getLogger(SignatureCheckFilter.class);

    private final RemoteJWKSet<SecurityContext> remoteJWKSet;

    @Autowired
    public SignatureCheckFilter(MessagingConfiguration messagingConfiguration) {
        try {
            final String jwksUrl = messagingConfiguration.getServerUrl() + "/jwks.json";
            LOG.info("server URL {}", messagingConfiguration.getServerUrl());
            this.remoteJWKSet = new RemoteJWKSet<>(new URL(jwksUrl));
        } catch (IOException e) {
            LOG.error("Failed to load jwks: ", e);
            throw new JwksException(e);
        }
    }

    /**
     * This method filter reject incoming event not coming from FFDC services
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String url = httpRequest.getRequestURL().toString();
        boolean isWebSocket = (httpRequest.getMethod().equals("GET") || httpRequest.getMethod().equals("POST"))
                && url.contains("/socket/");
        boolean isWebHookValidation = url.endsWith("/sample/clock-service/v1");
        if (isWebSocket || isWebHookValidation) {
            chain.doFilter(request, response);
            return;
        }
        byte[] body = MultipleReadHttpRequest.readBody(httpRequest);
        MultipleReadHttpRequest wrappedRequest = new MultipleReadHttpRequest(httpRequest, body);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String signature = wrappedRequest.getHeader("Signature");
        try {
            // check signature
            checkSignature(signature, body);
            chain.doFilter(wrappedRequest, response);
        } catch (JwksException e) {
            LOG.error("signature check failed", e);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * This method checks Signature of dataByte that matches the FFDC public Key
     *
     * @param fullSignature the full signature containing the key id, the algorithm and the signature
     * @param dataByte      the signed data
     */
    private void checkSignature(String fullSignature, byte[] dataByte) {
        try {
            Signature sig = parseSignature(fullSignature);
            final JWKMatcher jwkMatcher = new JWKMatcher.Builder()
                    .keyID(sig.keyId)
                    .algorithm(sig.algorithm)
                    .build();
            final List<JWK> jwkList = remoteJWKSet.get(new JWKSelector(jwkMatcher), null); // security context is ignored for Remote JWK set
            if (jwkList.size() != 1) {
                LOG.info("jwkList{}", jwkList.toString());
                throw new JwksException("Unable to find unique matcher for keyId:"
                        + sig.keyId + ", and algorithm:" + sig.algorithm + " in provided JWKS.");
            }
            final JWK jwk = jwkList.get(0);
            final JWSVerifier verifier = new RSASSAVerifier((RSAKey) jwk);
            LOG.info("Signature {}", sig.signature);
            final Base64URL signatureBaseURL =
                    Base64URL.encode(Base64.getMimeDecoder().decode(sig.signature));
            final JWSHeader jwsHeader = new JWSHeader(new JWSAlgorithm(jwk.getAlgorithm().getName()));
            if (!verifier.verify(jwsHeader, dataByte, signatureBaseURL)) {
                throw new JwksException("Invalid signature.");
            }
        } catch (JOSEException e) {
            throw new JwksException(e);
        }
    }

    private Signature parseSignature(String fullSignature) {
        String[] signaturesVal = fullSignature.split(",");
        if (signaturesVal.length == 3) {
            String[] keyVal = signaturesVal[0].split("=");
            String[] algorithmVal = signaturesVal[1].split("=");
            String[] signVal = signaturesVal[2].split("=");
            final String keyId = keyVal[1].replace("\"", "");
            final String algorithm = algorithmVal[1].replace("\"", "");
            final String signature = signVal[1].replace("\"", "");
            return new Signature(keyId, algorithm, signature);
        }
        throw new JwksException("Unable to parse header " + fullSignature);
    }

    private static class Signature {
        private final String keyId;
        private final Algorithm algorithm;
        private final String signature;

        private Signature(String keyId, String algorithm, String signature) {
            this.keyId = keyId;
            this.algorithm = new Algorithm(algorithm);
            this.signature = signature;
        }
    }

}
