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
                Queue<Message> messages = MessageControllerImpl.getMessageController().receiveMessages();
                for (Message message: messages) {
                    MessageHandlerImpl.getMessageHandler().handle(message);
                }
            }

        }, 0, Protocol.getMessageReceivingInterval());
    }

    public static ProtoMessagesListener getListener() {
        if (listener == null) {
            listener = new ProtoMessagesListenerImpl();
        }
        return listener;
    }
}