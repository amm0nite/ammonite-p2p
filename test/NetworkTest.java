import fr.ambox.p2p.App;
import fr.ambox.p2p.Group;

import fr.ambox.p2p.chat.ChatPDURange;
import org.junit.Test;

public class NetworkTest {

    @Test
    public void publicChatTest() {
        Group group = new Group(2);
        group.start();

        App app1 = group.get(0);
        app1.getChatService().post("Hello world", ChatPDURange.PUBLIC);
    }
}
