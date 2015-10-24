package de.contentreich.clojure;

import clojure.java.api.Clojure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@ServerEndpoint(value = "/lambdalf/gorilla-repl/repl")
public class NReplConnector {
    static final Logger log = LoggerFactory.getLogger(NReplConnector.class.getName());

    @OnOpen
    public void start(Session session) {
        URI uri = session.getRequestURI();
        String path = uri.getPath();
        log.debug("Establishing session for " + path);
    }

    @OnClose
    public void end(Session wsSession) {
        log.debug("End session session ", wsSession.getId());
    }

    @OnMessage
    public void incoming(String message, Session session) {
        String key = session.getId();
        log.debug("Received " + message + " for session " + key);
        String ns = "contentreich.gorilla-repl";
        String fn = "process-json-message";
        log.info("Preparing to invoke {}/{}", ns, fn);
        try {
            Object sym = Clojure.var("clojure.core", "symbol").invoke(ns);
            Clojure.var("clojure.core", "require").invoke(sym);
            // Not quite sure if the Remote should go in as well
            List<String> retval = (List<String>) Clojure.var(ns, fn).invoke(message, session.getUserProperties());
            log.info("Got {}", retval.size() + " reponses");
            List<String> responses = (List) retval;
            for (String response: responses) {
                log.debug("Sending {}", response);
                session.getBasicRemote().sendText(response);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try (Closeable closeable = session) {
                session.close();
            } catch (IOException e2) { }
        }
    }

}
