package fit.networks.controller;

import fit.networks.protocol.Protocol;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class ProtoMessagesListenerImpl implements ProtoMessagesListener {
    private static ProtoMessagesListener listener = null;

    private ProtoMessagesListenerImpl() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Queue<Message> messages = MessageControllerImpl.getInstance().receiveMessages();
                for (Message message: messages) {
                    if (GameControllerImpl.getInstance() != null) {
                        MessageHandlerImpl.getInstance().handle(message);
                    }
                }
            }

        }, 0, Protocol.getMessageReceivingInterval());
    }

    public static void subscribe() {
        if (listener == null) {
            listener = new ProtoMessagesListenerImpl();
        }
    }
}
