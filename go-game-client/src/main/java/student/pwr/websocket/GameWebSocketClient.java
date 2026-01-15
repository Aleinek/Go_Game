package student.pwr.websocket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import student.pwr.dto.websocket.*;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * WebSocket client for real-time communication with the Go Game server.
 * <p>
 * Uses STOMP over SockJS for WebSocket communication. Handles:
 * <ul>
 *   <li>Connection establishment with player authentication</li>
 *   <li>Subscription to player-specific game events</li>
 *   <li>Event parsing and queuing for the JavaFX thread</li>
 * </ul>
 * </p>
 * <p>
 * Events are placed in blocking queues that the game controller
 * polls from. This decouples the WebSocket receiving thread from
 * the JavaFX application thread.
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class GameWebSocketClient {

    private final String serverUrl;
    private final ObjectMapper objectMapper;
    private WebSocketStompClient stompClient;
    private StompSession session;
    private UUID playerId;
    
    // Kolejki do przekazywania zdarzeń do głównego wątku
    private final BlockingQueue<GameStartedPayload> gameStartedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GameEventWrapper> gameEventQueue = new LinkedBlockingQueue<>();
    
    // Wrapper do przechowywania różnych typów zdarzeń
    public record GameEventWrapper(String type, Object payload) {}

    public GameWebSocketClient(String serverUrl) {
        this.serverUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Nawiązuje połączenie WebSocket i subskrybuje zdarzenia gry dla danego gracza.
     */
    public void connect(UUID playerId) throws InterruptedException, ExecutionException, TimeoutException {
        this.playerId = playerId;
        
        // Konfiguracja transportu SockJS
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        
        stompClient = new WebSocketStompClient(sockJsClient);
        
        // Konfiguracja konwertera JSON
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
        
        // Połączenie z serwerem
        String wsUrl = serverUrl + "/ws";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("X-Player-Id", playerId.toString());
        
        session = stompClient.connectAsync(wsUrl, new GameSessionHandler(), connectHeaders)
                .get(10, TimeUnit.SECONDS);
        
        // Subskrypcja zdarzeń gry dla tego gracza
        subscribeToGameEvents();
    }

    /**
     * Subskrybuje kanał zdarzeń gry dla aktualnego gracza.
     */
    private void subscribeToGameEvents() {
        // Subskrybuj kanał /topic/game/{playerId} - serwer wysyła tam zdarzenia
        String destination = "/topic/game/" + playerId.toString();
        
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return JsonNode.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    JsonNode node = (JsonNode) payload;
                    String eventType = node.get("type").asText();
                    JsonNode payloadNode = node.get("payload");
                    
                    switch (eventType) {
                        case GameEvent.GAME_STARTED -> {
                            GameStartedPayload gameStarted = objectMapper.treeToValue(
                                payloadNode, GameStartedPayload.class);
                            gameStartedQueue.offer(gameStarted);
                        }
                        case GameEvent.OPPONENT_MOVED -> {
                            OpponentMovedPayload moved = objectMapper.treeToValue(
                                payloadNode, OpponentMovedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, moved));
                        }
                        case GameEvent.OPPONENT_PASSED -> {
                            OpponentPassedPayload passed = objectMapper.treeToValue(
                                payloadNode, OpponentPassedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, passed));
                        }
                        case GameEvent.GAME_ENDED -> {
                            GameEndedPayload ended = objectMapper.treeToValue(
                                payloadNode, GameEndedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, ended));
                        }
                        // Negotiation phase events
                        case GameEvent.NEGOTIATION_STARTED -> {
                            NegotiationStartedPayload negotiation = objectMapper.treeToValue(
                                payloadNode, NegotiationStartedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, negotiation));
                        }
                        case GameEvent.CHAIN_STATUS_CHANGED -> {
                            ChainStatusChangedPayload changed = objectMapper.treeToValue(
                                payloadNode, ChainStatusChangedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, changed));
                        }
                        case GameEvent.SCORE_ACCEPTED -> {
                            ScoreAcceptedPayload accepted = objectMapper.treeToValue(
                                payloadNode, ScoreAcceptedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, accepted));
                        }
                        case GameEvent.GAME_RESUMED -> {
                            GameResumedPayload resumed = objectMapper.treeToValue(
                                payloadNode, GameResumedPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, resumed));
                        }
                        case GameEvent.NEGOTIATION_ERROR -> {
                            NegotiationErrorPayload error = objectMapper.treeToValue(
                                payloadNode, NegotiationErrorPayload.class);
                            gameEventQueue.offer(new GameEventWrapper(eventType, error));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Błąd przetwarzania zdarzenia WebSocket: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Czeka na zdarzenie GAME_STARTED (matchmaking zakończony).
     * Blokuje do momentu otrzymania zdarzenia lub timeout.
     */
    public GameStartedPayload waitForGameStart(long timeout, TimeUnit unit) throws InterruptedException {
        return gameStartedQueue.poll(timeout, unit);
    }

    /**
     * Czeka na dowolne zdarzenie gry (ruch przeciwnika, pass, koniec gry).
     * Blokuje do momentu otrzymania zdarzenia lub timeout.
     */
    public GameEventWrapper waitForGameEvent(long timeout, TimeUnit unit) throws InterruptedException {
        return gameEventQueue.poll(timeout, unit);
    }

    /**
     * Sprawdza czy jest dostępne zdarzenie gry bez blokowania.
     */
    public GameEventWrapper pollGameEvent() {
        return gameEventQueue.poll();
    }

    /**
     * Sprawdza czy połączenie WebSocket jest aktywne.
     */
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    /**
     * Zamyka połączenie WebSocket.
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }
    
    /**
     * Czyści kolejki zdarzeń.
     */
    public void clearQueues() {
        gameStartedQueue.clear();
        gameEventQueue.clear();
    }

    /**
     * Handler sesji STOMP - obsługuje zdarzenia cyklu życia połączenia.
     */
    private class GameSessionHandler extends StompSessionHandlerAdapter {
        
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Połączono z serwerem WebSocket");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, 
                                   StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("Błąd WebSocket: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("Błąd transportu WebSocket: " + exception.getMessage());
        }
    }
}
