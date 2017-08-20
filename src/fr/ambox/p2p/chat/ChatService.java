package fr.ambox.p2p.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.ambox.p2p.UserService;
import fr.ambox.p2p.configuration.IdentityService;
import fr.ambox.p2p.connexion.ConnexionEmitter;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;
import fr.ambox.p2p.peers.Friend;
import fr.ambox.p2p.peers.FriendshipService;
import fr.ambox.p2p.peers.PeerId;
import fr.ambox.p2p.peers.PeersService;
import fr.ambox.p2p.utils.CircularList;

public class ChatService extends UserService {
    private static int ListLength = 256;

    private CircularList<ChatLineData> publicMessages;
    private CircularList<ChatLineData> friendsMessages;
    private HashMap<PeerId, CircularList<ChatLineData>> privateMessages;

    public ChatService() {
        this.publicMessages = new CircularList<ChatLineData>(ChatService.ListLength);
        this.friendsMessages = new CircularList<ChatLineData>(ChatService.ListLength);
        this.privateMessages = new HashMap<PeerId, CircularList<ChatLineData>>();
    }

    @Override
    public void run() {

    }

    synchronized private void storeLine(ChatLineData cl) {
        if (cl.range == ChatPDURange.PUBLIC) {
            this.publicMessages.add(cl);
        } else if (cl.range == ChatPDURange.FRIENDS) {
            this.friendsMessages.add(cl);
        } else if (cl.range == ChatPDURange.PRIVATE) {
            //this.access.getLogger().log("storing in private ("+cl.local+")");
            PeerId peerId;
            if (cl.local) {
                peerId = cl.receptionData.destinationPeerId;
            } else {
                peerId = cl.receptionData.sourcePeerId;
            }
            CircularList<ChatLineData> list = this.privateMessages.get(peerId);
            if (list == null) {
                //this.access.getLogger().log("creating message buffer for "+peerId.getId());
                list = new CircularList<ChatLineData>(ChatService.ListLength);
                this.privateMessages.put(peerId, list);
            }
            list.add(cl);
            //this.access.getLogger().log("stored in "+peerId.getId());
        }
    }

    @SuppressWarnings("unchecked")
    synchronized private JsonArray JsonMessageList(ChatPDURange range, String id) {
        ArrayList<ChatLineData> data = new ArrayList<ChatLineData>();
        if (range == ChatPDURange.PUBLIC) {
            data.addAll(this.publicMessages.values());
        } else if (range == ChatPDURange.FRIENDS) {
            data.addAll(this.friendsMessages.values());
        } else if (range == ChatPDURange.PRIVATE) {
            PeersService peersService = (PeersService) this.getService("peers");
            PeerId peerId = peersService.getPeer(id);

            if (peerId != null) {
                CircularList<ChatLineData> list = this.privateMessages.get(peerId);
                if (list != null) {
                    data.addAll(list.values());
                } else {
                    this.log("no discussion with " + id);
                }
            }
        }

        JsonArray jsonArray = new JsonArray();
        for (Object o : data) {
            ChatLineData cl = (ChatLineData) o;
            JsonObject mjson = new JsonObject();
            mjson.addProperty("author", cl.receptionData.sourcePeerId.getNickname());
            mjson.addProperty("text", cl.text);
            mjson.addProperty("time", cl.time);
            mjson.addProperty("hops", cl.receptionData.hops);
            jsonArray.add(mjson);
        }
        return jsonArray;
    }

    @Override
    public void handle(PDU pdu, ReceptionData ReceptionData) {
        ChatPDU chatpdu = (ChatPDU) pdu;
        ChatLineData cl = new ChatLineData();
        cl.receptionData = ReceptionData;
        cl.range = chatpdu.getRange();
        cl.text = chatpdu.getText();
        cl.time = chatpdu.getTime();
        this.storeLine(cl);
        this.log("ChatPDU received : " + cl.text);
    }

    @Override
    public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
        String range = params.get("range");
        String id = params.get("id");
        if (range != null && !range.isEmpty()) {
            JsonArray res = new JsonArray();
            if (range.equals("private") && id != null && !id.isEmpty()) {
                res = this.JsonMessageList(ChatPDURange.PRIVATE, id);
            } else if (range.equals("friends")) {
                res = this.JsonMessageList(ChatPDURange.FRIENDS, null);
            } else if (range.equals("public")) {
                res = this.JsonMessageList(ChatPDURange.PUBLIC, null);
            }

            return new HttpResponse(200, res.toString());
        }
        return HttpResponse.fail();
    }

    public void post(String text, ChatPDURange range) {
        this.post(text, range, null);
    }

    public void post(String text, ChatPDURange range, PeerId target) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        ChatPDU chatpdu = new ChatPDU(text);
        chatpdu.setRange(range);

        // prepare fake reception for the local chat line
        ReceptionData fakeReceptionData = new ReceptionData();
        fakeReceptionData.sourcePeerId = this.getIdentityService().getMyId();
        fakeReceptionData.hops = 0;

        if (target != null) {
            fakeReceptionData.destinationPeerId = target;
        }

        // publish the chat pdu
        ConnexionEmitter emitter = (ConnexionEmitter) this.getService("emitter");

        if (range == ChatPDURange.PRIVATE) {
            emitter.unicast(chatpdu, target);
        } else if (range == ChatPDURange.FRIENDS) {
            for (Friend f : this.getFriendshipService().getFriends()) {
                emitter.direct(chatpdu, f);
            }
        } else if (range == ChatPDURange.PUBLIC) {
            emitter.broadcast(chatpdu);
        }

        // store the sent chat line
        ChatLineData cl = new ChatLineData();
        cl.receptionData = fakeReceptionData;
        cl.range = chatpdu.getRange();
        cl.text = chatpdu.getText();
        cl.time = chatpdu.getTime();
        cl.local = true;
        this.storeLine(cl);
    }

    @Override
    public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
        String text = params.get("text");
        String range = params.get("range");
        String id = params.get("id");

        if (text == null || text.isEmpty() || range == null || (!range.equals("private") && !range.equals("friends") && !range.equals("public"))) {
            return HttpResponse.fail();
        }

        this.post(text, ChatPDURange.valueOf(range), this.getPeersService().getPeer(id));
        return HttpResponse.success();
    }

    @Override
    public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
        return HttpResponse.fail();
    }

    public Collection<ChatLineData> getPrivateMessages(PeerId peerId) {
        CircularList<ChatLineData> messageList = this.privateMessages.get(peerId);
        if (messageList == null) {
            return new ArrayList<ChatLineData>();
        }
        return messageList.values();
    }

    public Collection<ChatLineData> getFriendsMessages() {
        return this.friendsMessages.values();
    }

    public Collection<ChatLineData> getPublicMessages() {
        return this.publicMessages.values();
    }

    public IdentityService getIdentityService() {
        return (IdentityService) this.getService("identity");
    }

    public PeersService getPeersService() {
        return (PeersService) this.getService("peers");
    }

    public FriendshipService getFriendshipService() {
        return (FriendshipService) this.getService("friendship");
    }
}
