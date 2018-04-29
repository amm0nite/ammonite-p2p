import fr.ambox.p2p.App;
import fr.ambox.p2p.Group;

import fr.ambox.p2p.chat.ChatLineData;
import fr.ambox.p2p.chat.ChatPDURange;
import fr.ambox.p2p.peers.PeerId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class NetworkTest {

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Test
    public void privateChatTest() {
        Group group = new Group(2);
        group.start();

        App app1 = group.get(0);
        App app2 = group.get(1);

        PeerId app1Id = app1.getIdentityService().getMyId();
        PeerId app2Id = app2.getIdentityService().getMyId();

        group.makeFullFriends(app1, app2);

        app1.getChatService().post("Hello world", ChatPDURange.PRIVATE, app2Id);

        this.sleep(200);

        Collection<ChatLineData> messages1 = app1.getChatService().getPrivateMessages(app2Id);
        Collection<ChatLineData> messages2 = app2.getChatService().getPrivateMessages(app1Id);

        Assert.assertEquals(1, messages1.size());
        Assert.assertEquals(1, messages2.size());

        // TODO test message contents and metadata
    }
}
