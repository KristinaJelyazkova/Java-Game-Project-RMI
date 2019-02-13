package get15;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CallBackImplementation extends UnicastRemoteObject
        implements CallBack {
    // The client will be called by the server through callback
    private Get15ClientRMI thisClient;

    /** Constructor */
    public CallBackImplementation(Object client) throws RemoteException {
        thisClient = (Get15ClientRMI)client;
    }

    /** The server notifies the client for taking a turn */
    public void takeTurn(boolean turn) throws RemoteException {
        thisClient.setMyTurn(turn);
    }

    /** The server sends a message to be displayed by the client */
    public void notify(String message) throws RemoteException {
        thisClient.setMessage(message);
    }

    /** The server notifies a client of the other player's move */
    public void mark(int number, char color)
            throws RemoteException {
        thisClient.mark(number, color);
    }
}
