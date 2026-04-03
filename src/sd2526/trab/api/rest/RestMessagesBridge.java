package sd2526.trab.api.rest;

import java.util.List;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;

@Path(RestUsers.PATH)
public class RestMessagesBridge implements RestMessages {

    private final Messages messages;

    public RestMessagesBridge(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String postMessage(String pwd, Message msg) {
        return resultOrThrow(messages.postMessage(pwd, msg));
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        return resultOrThrow(messages.getInboxMessage(name, mid, pwd));
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        // se a query for vazia, listamos tudo Caso contrário pesquisamos.
        if (query == null || query.isEmpty()) {
            return resultOrThrow(messages.getAllInboxMessages(name, pwd));
        } else {
            return resultOrThrow(messages.searchInbox(name, pwd, query));
        }
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
        resultOrThrow(messages.removeInboxMessage(name, mid, pwd));
    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        resultOrThrow(messages.deleteMessage(name, mid, pwd));
    }


    private <T> T resultOrThrow(Result<T> result) {
        if (result.isOK()) {
            return result.value();
        } else {
            throw new WebApplicationException(toStatus(result.error()));
        }
    }

    private Status toStatus(Result.ErrorCode error) {
        switch (error) {
            case FORBIDDEN: return Status.FORBIDDEN;
            case NOT_FOUND: return Status.NOT_FOUND;
            case BAD_REQUEST: return Status.BAD_REQUEST;
            case CONFLICT: return Status.CONFLICT;
            default: return Status.INTERNAL_SERVER_ERROR;
        }
    }
}