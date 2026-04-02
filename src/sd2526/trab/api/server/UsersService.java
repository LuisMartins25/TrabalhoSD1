package sd2526.trab.api.server;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UsersService implements Users {

    private final String  domain;
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UsersService(String domain) {
        this.domain = domain;
    }

    public Result<String> postUser(User user) {
        if (user == null || user.getName() == null || user.getPwd() == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        if (!domain.equals(user.getDomain())) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        var existingUser = users.get(user.getName());
        if (existingUser != null) {
            if (!existingUser.getPwd().equals(user.getPwd()) ||
                    !existingUser.getDisplayName().equals(user.getDisplayName())) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
            return Result.ok(user.getName() + "@" + domain);
        }

        users.put(user.getName(), user);
        return Result.ok(user.getName() + "@" + domain);
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        var user = users.get(name);
        if (user == null || !user.getPwd().equals(pwd)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        var res = getUser(name, pwd);
        if(!res.isOK()) return Result.error(res);

        String q = query.toLowerCase();
        var hits = users.values().stream()
                .filter(u -> u.getName().toLowerCase().contains(q))
                .map(u -> {
            User copy = new User(u.getName(), "", u.getDisplayName(), u.getDomain());
            return copy;
        })
                .collect(Collectors.toList());

        return Result.ok(hits);
    }

    @Override public Result<User> updateUser(String name, String pwd, User info) { return null; }
    @Override public Result<User> deleteUser(String name, String pwd) { return null; }
}
