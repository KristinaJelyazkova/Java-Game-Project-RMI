package get15;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Get15Interface extends Remote {
    /**
     * Connect to the Get15 server and return a token
     * ('r' for "red" or 'b' for "blue")
     * indicating the color with which the player is playing.
     * If the returned token is ' ', the client is not connected to
     * the server
     */
    char connect(CallBack client) throws RemoteException;

    /** A client invokes this method to notify the server of the number it takes
     * and returns false if the number is already taken*/
    boolean takeNumber(int number, char color)
            throws RemoteException;

    /** A client invokes this method on close request
     * in order to inform the server it disconnects
     */
    void disconnect(char color) throws RemoteException;
}
