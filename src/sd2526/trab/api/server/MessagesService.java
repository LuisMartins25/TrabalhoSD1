package sd2526.trab.api.server;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

import java.util.*;

public class MessagesService implements Messages {

    private final String domain;
    private final Users users;

    private final Map<String, List<String>> inboxes = new HashMap<>();

    private final Map<String, Message> allMessages = new HashMap<>();

    public MessagesService(String domain, Users users) {
        this.domain = domain;
        this.users = users;
    }

    @Override
    public synchronized Result<String> postMessage(String pwd, Message msg) {
        if (msg == null || pwd == null || msg.getSender() == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        String senderName = msg.getSender().split("@")[0];
        var userRes = users.getUser(senderName, pwd);
        if (!userRes.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        String formattedSender = String.format("%s <%s@%s>",
                userRes.value().getDisplayName(), senderName, domain);
        msg.setSender(formattedSender);

        String mid = UUID.randomUUID().toString();
        msg.setId(mid);
        msg.setCreationTime(System.currentTimeMillis());

        for (String dest : msg.getDestination()) {
            if (dest.endsWith("@" + domain)) {
                String destUser = dest.split("@")[0];
                inboxes.computeIfAbsent(destUser, k -> new ArrayList<>()).add(mid);
            }
        }

        allMessages.put(mid, msg);
        return Result.ok(mid);
    }

    @Override
    public synchronized Result<Message> getInboxMessage(String name, String mid, String pwd) {
        var auth = users.getUser(name, pwd);
        if (!auth.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        Message msg = allMessages.get(mid);
        if (msg == null || !inboxes.getOrDefault(name, Collections.emptyList()).contains(mid))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(msg);
    }

    @Override
    public synchronized Result<List<String>> getAllInboxMessages(String name, String pwd) {
        var auth = users.getUser(name, pwd);
        if (!auth.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        return Result.ok(new ArrayList<>(inboxes.getOrDefault(name, Collections.emptyList())));
    }

    @Override
    public synchronized Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        var auth = users.getUser(name, pwd);
        if (!auth.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        List<String> userMbox = inboxes.get(name);
        if (userMbox == null || !userMbox.remove(mid))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public synchronized Result<Void> deleteMessage(String name, String mid, String pwd) {
        var auth = users.getUser(name, pwd);
        if (!auth.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        Message msg = allMessages.get(mid);
        if (msg == null) return Result.ok();

        long now = System.currentTimeMillis();
        if (now - msg.getCreationTime() < 30000) {
            for (List<String> mbox : inboxes.values()) {
                mbox.remove(mid);
            }
            allMessages.remove(mid);
        }

        return Result.ok();
    }

    @Override
    public synchronized Result<List<String>> searchInbox(String name, String pwd, String query) {
        var auth = users.getUser(name, pwd);
        if (!auth.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);

        String q = query.toLowerCase();
        List<String> userMbox = inboxes.getOrDefault(name, Collections.emptyList());

        List<String> hits = userMbox.stream()
                .filter(mid -> {
                    Message m = allMessages.get(mid);
                    return m != null && (m.getSubject().toLowerCase().contains(q)
                            || m.getContents().toLowerCase().contains(q));
                })
                .toList();

        return Result.ok(hits);
    }
}