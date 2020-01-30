package com.trmsys.clock.clockapplication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import io.swagger.model.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWireMock(port = 8082, httpsPort = 8081)
public class ClockApplicationTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClockApplicationTest.class);
    private static final String PRIVATE_KEY_CONTENT = "{\n" +
            "    \"keys\": [\n" +
            "        {\n" +
            "            \"p\": \"0iqBrKDDywMNncHvM2skKqJV-Qx_bYLpZN3KgNPGliwimS5aHi5mjpjjrL6BPbiUhjN9Hw7-5qDZpXZQyrWG7zMeb1fgTM7VFDoA0Es35NnnJ0ncSBO-xYcwwLz5YSopVd6S1N9v1zI0Grh2ZlVhcyWjvOafNQXSVut7oiqceT8\",\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"q\": \"x3ubnuNBVHb_EQPgPb9TP0ZmNT9OpE46GUDzm1Zgops9MFvbY6R_n8JMjMoXD2CVckZLMOwpi4fKYwjQuoKNfxbmRb-S33He-aaMcnK6n7Y_CWFj6MqiO920wG_SJza39w9EZxrQZPsNOHt0ywl4QBz0CCNIRKE2CoC_tuqJFrU\",\n" +
            "            \"d\": \"BjjfPJislpPcuyZ-iGGXwjr7xDOnJZB5eg1-y3dFDzPuvFuhjGntnhN9IJmsIimvnQRUB-mOe79AGAFwhjYFOZD-hyRhtUgg1oNcrC9Z57idWgfmO5U5NGtaBryJaFcgxDBGYfqr3y3X13nUMJhVFS68ygmuIky6jqnR2zwmkI_AOX4AYMWA_fXIZfJW2UKxARa3bp2T2DePgeh2z6kZajjphY0ufstVfDP29ZtwEJIlbBlCQaUR0ztQ3nGt2gRDRFR_yJKoHrLx1D32Lgwd9O2Oi7cev1GBeb0M0Ih4H3cXKvBRJ8sQwRAMfnUdrxHUy6cOxUINLzniLJT8LfaUcQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"test\",\n" +
            "            \"qi\": \"DE3viPnXax9GzpLkAC5VjngPBFBvgzbtP34DluTav1zxcn5N9oDwkNebkfggisqd5MAfCgatuXYzzU6cGVFsVhMF_8NjRinom7sW91Tae8UlLJtRId-00L808z-1AQogd507Dx3qGHnXbzStPuAEiMdM02IHxBEw8oeCc3ZHYN8\",\n" +
            "            \"dp\": \"HtfiJAWL9nVCQE6_3hnxOtiMKXquENJPkmACYhmyYUOk6DonO5qvrmm9sBJdQUOfeHqB-FbpDcu5ZVER1k4BuYUVCF4rKtqH5uoESI1hbJKWG-v5ChLrwm1uuDqDlsDrRBDgF6Ga9kgF4Y4Ewkvxe0ag2w41e0CZd-wjPs9mG10\",\n" +
            "            \"alg\": \"RS256\",\n" +
            "            \"dq\": \"jlzNs_f1YMVHxSnh9886Jvcj7C5Q_nuBhRJQiHR9pK5rXb7AdH0qQ6aToQp8qJHCNfqqoLB5-cUgD8tzwxd2ydNa5T4SIHsQqVRN-UOdomua0yP4_GLYoybXX1quPSQU6DGyHG8LeHWeesfIkzof4omHMuOTKL9wZNB7gZwh190\",\n" +
            "            \"n\": \"o8SE-6w6UeJRvHnmzGbTXJs-oY9AfCg0E6JSwlHkSiBGw5Wu_UXqNFeDZuS6RuvTc8JrxVrtAtnxh6mxapGF0ViiC2caskIEYk7gFPnXMP3ZNCavIxsEfKoYemc8v7H6LBw6oOe4ojTzLm_TAyNVfTSi7samJBKYGR_sx6kAR2PDT_tamwmfUZ8fbQ6mYYaoMKybablZWjpJ1lxOwrdEopnzvXs3zBiWWoPTtNg3TZ6Oz5jZoqvIwb2_EI_4VhzJpX8Qf4pGGE4ep5KmeEWW-I4uVQ-BhIxEjhM1qy3jQcAOAveG1eDh6_MklBwlQgNtgEO2rXVmdI81DmGsC8Qjiw\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    @Autowired
    private TestRestTemplate restTemplate;

    private BlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final long TIMEOUT = 30000L;
    private final AtomicInteger counter = new AtomicInteger();

    private SignatureService signatureService;

    @Before
    public void init() throws ParseException, JOSEException {
        this.signatureService = new SignatureService(PRIVATE_KEY_CONTENT);
        stubFor(get(urlPathEqualTo("/jwks.json"))
                .willReturn(aResponse().withBody(signatureService.getPublicKey().toString())));
        restTemplate.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            try {
                final String signature = signatureService.getSignature(body);
                request.getHeaders().add("Signature", signature);
                request.getHeaders().add("ff-trace-id", UUID.randomUUID().toString());
            } catch (JOSEException e) {
                LOGGER.error("Unexpected error", e);
            }

            return execution.execute(request, body);
        });
    }

    @Test(timeout = TIMEOUT)
    public void testSendEventAndReceiveOnWebSocket() throws InterruptedException, ExecutionException {
        openWebSocket();
        final Event expected = new Event().tenant("test").eventTime("" + counter.get());
        sendEvent(expected);
        final Event received = events.take();
        Assert.assertEquals(expected, received);
    }

    private void sendEvent(Event test) {
        restTemplate.postForEntity("http://localhost:8080/sample/clock-service/v1/datetime-published", test, Void.class);
    }

    private void openWebSocket() throws ExecutionException, InterruptedException {
        WebSocketClient client = new SockJsClient(Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient())));

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        stompClient.connect("ws://localhost:8080/socket", sessionHandler).get();
    }

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            LOGGER.info("New session established : " + session.getSessionId());
            session.subscribe("/clock/event", this);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            LOGGER.error("Got an exception", exception);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Event.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Event event = (Event) payload;
            LOGGER.info("Received : {}", event);
            events.add(event);
        }
    }

    private static class SignatureService {
        public static final String KEY_ID = "test";
        private final JWKSet publicKey;
        private final JWSSigner jwsSigner;
        private final JWSHeader jwsHeader;

        public SignatureService(String privateKeyContent) throws ParseException, JOSEException {
            final JWKSet privateKeySet = JWKSet.parse(privateKeyContent);
            final JWK privateKey = privateKeySet.getKeyByKeyId(KEY_ID);
            this.publicKey = privateKeySet.toPublicJWKSet();
            this.jwsSigner = new RSASSASigner((RSAKey) privateKey);
            this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(privateKey.getAlgorithm().getName()))
                    .keyID(KEY_ID).build();
        }

        private String signData(byte[] data) throws JOSEException {
            final Base64URL signatureBaseURL = jwsSigner.sign(jwsHeader, data);
            return Base64.getEncoder().encodeToString(signatureBaseURL.decode());
        }

        String getSignature(byte[] data) throws JOSEException {
            return "keyId=\"" + jwsHeader.getKeyID() +
                    "\",algorithm=\"" + jwsHeader.getAlgorithm().toString() +
                    "\",signature=\"" + signData(data) + "\"";
        }

        JWKSet getPublicKey() {
            return this.publicKey;
        }
    }
}
