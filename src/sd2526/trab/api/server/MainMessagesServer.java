package sd2526.trab.api.server;

import java.net.URI;
import java.util.logging.Logger;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Users;
import sd2526.trab.api.rest.RestMessagesBridge;

public class MainMessagesServer {
    private static Logger Log = Logger.getLogger(MainMessagesServer.class.getName());

    public static void main(String[] args) {
        try {

            String domain = (args.length > 0) ? args[0] : "fct";
            String serverPort = (args.length > 1) ? args[1] : "8081";

            String serverURI = String.format("http://0.0.0.0:%s/rest", serverPort);

            Users usersLogic = new UsersService(domain);

            Messages messagesLogic = new MessagesService(domain, usersLogic);

            ResourceConfig config = new ResourceConfig();

            config.register(JacksonFeature.class);

            config.register(new RestMessagesBridge(messagesLogic));

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("Messages Server (%s) ready at: %s", domain, serverURI));

        } catch (Exception e) {
            Log.severe("Error starting Messages Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}