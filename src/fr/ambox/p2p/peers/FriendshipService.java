package fr.ambox.p2p.peers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.JsonArray;
import fr.ambox.p2p.UserService;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;

public class FriendshipService extends UserService {
    private ArrayList<Friend> friends;

    public FriendshipService() {
        this.friends = new ArrayList<Friend>();
    }

    @Override
    public void run() {

    }

    private JsonArray JsonFriendList() {
        JsonArray jsonArray = new JsonArray();
        for (Friend f : this.friends) {
            jsonArray.add(f.toJSON());
        }
        return jsonArray;
    }

    private void deleteFriend(HostAndPort hp) {
        Iterator<Friend> it = this.friends.iterator();
        while (it.hasNext()) {
            Friend f = it.next();
            if (f.getHostAndPort().equals(hp)) {
                it.remove();
            }
        }
    }

    public Friend[] getFriends() {
        Friend[] res = new Friend[this.friends.size()];
        return this.friends.toArray(res);
    }

    @Override
    public void handle(PDU pdu, ReceptionData receptionData) {

    }

    @Override
    public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
        return new HttpResponse(200, this.JsonFriendList().toString());
    }

    public void add(String host, PeerId identity) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host must not be empty");
        }

        Friend friend = new Friend(HostAndPort.fromString(host).withDefaultPort(8080), new PeerId(identity.getPublicKey()));
        this.friends.add(friend);

        PeersService peers = (PeersService) this.getService("peers");
        peers.addPeer(identity);
    }

    @Override
    public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
        String address = params.get("address");
        String identity = params.get("identity");

        if (address == null || address.isEmpty() || identity == null || identity.isEmpty()) {
            return HttpResponse.fail();
        }

        try {
            PeerId peerIdentiy = PeerId.fromBase64(identity);
            this.add(address, peerIdentiy);
        } catch (Exception e) {
            return HttpResponse.fail(e);
        }

        return HttpResponse.success();

    }

    @Override
    public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
        String address = params.get("address");
        this.deleteFriend(HostAndPort.fromString(address).withDefaultPort(8080));
        return HttpResponse.success();
    }
}
