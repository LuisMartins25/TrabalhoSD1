package sd2526.trab.api.rest;

import java.util.List;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Users;
import sd2526.trab.api.java.Result;

@Path("/users")
public class RestUsersBridge  implements  RestUsers{
    private final Users users;

    public RestUsersBridge(Users users) {
        this.users = users;
    }

    @Override
    public String postUser(User user) {
        return resultOrThrow(users.postUser(user));
    }

    @Override
    public User getUser(String name, String pwd) {
        return resultOrThrow(users.getUser(name, pwd));
    }

    @Override
    public User updateUser(String name, String pwd, User info) {
        return resultOrThrow(users.updateUser(name, pwd, info));
    }

    @Override
    public User deleteUser(String name, String pwd) {
        return resultOrThrow(users.deleteUser(name, pwd));
    }

    @Override
    public List<User> searchUsers(String name, String pwd, String pattern) {
        return resultOrThrow(users.searchUsers(name, pwd, pattern));
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
            case FORBIDDEN:
                return Status.FORBIDDEN;
            case CONFLICT:
                return Status.CONFLICT;
            case NOT_FOUND:
                return Status.NOT_FOUND;
            case BAD_REQUEST:
                return Status.BAD_REQUEST;
            case NOT_IMPLEMENTED:
                return Status.NOT_IMPLEMENTED;
            default:
                return Status.INTERNAL_SERVER_ERROR;
        }
    }
}