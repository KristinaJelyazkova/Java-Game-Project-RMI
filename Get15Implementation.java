package get15;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Get15Implementation extends UnicastRemoteObject
    implements Get15Interface{

    /* Declare two Callback objects, each used to call each of the two players back
     The player marking his number with red is called the red player
    and the other is the blue player*/
    private CallBack redPlayer = null;
    private CallBack bluePlayer = null;

    /* numbers holds whether a number is not chosen (' '),
    chosen by red player('r') or by the blue player ('b')
     */
    private char[] numbers = new char[9];

    /** Constructs Get15Implementation object and exports it on default port.
     */
    public Get15Implementation() throws RemoteException {
        super();
        initializeNumbers();
    }

    /** Constructs Get15Implementation object and exports it on specified
     * port.
     * @param port The port for exporting
     */
    public Get15Implementation(int port) throws RemoteException {
        super(port);
        initializeNumbers();
    }

    private void initializeNumbers(){
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = ' ';
        }
    }

    /**
     * Connect to the Get15 server and return the token.
     * If the returned token is 'r', the client is connected as
     * the red player, if it is 'b' - as the blue player
     * If the returned token is ' ', the client is not connected to
     * the server
     */
    public char connect(CallBack client) throws RemoteException {
        if (redPlayer == null) {
            // redPlayer (first player) registered
            redPlayer = client;
            redPlayer.notify("Wait for a second player to join");
            return 'r';
        }
        else if (bluePlayer == null) {
            // bluePlayer (second player) registered
            bluePlayer = client;
            bluePlayer.notify("Red player's turn");
            bluePlayer.takeTurn(false);
            redPlayer.notify("It is my turn");
            redPlayer.takeTurn(true);
            return 'b';
        }
        else {
            // Already two players
            client.notify("Two players are already in the game");
            return ' ';
        }
    }

    /** A client invokes this method to notify the server of the number it takes
     * and returns false if the number is already taken*/
    public boolean takeNumber(int number, char color)
            throws RemoteException {
        // check whether the number is already taken - in this case it can not be taken again
        if(numbers[number - 1] != ' '){
            return false;
        }
        // Set number as taken by the player with this color
        numbers[number - 1] = color;

        if(checkIfDisconnected(redPlayer, bluePlayer) ||
                checkIfDisconnected(bluePlayer, redPlayer)){
            return false;
        }

        // Notify the other player of the move
        if (color == 'r')
            bluePlayer.mark(number, 'r');
        else
            redPlayer.mark(number, 'b');

        // Check if the player with this color wins
        if (isWon(color)) {
            if (color == 'r') {
                redPlayer.notify("I won!");
                bluePlayer.notify("I lost!");
                redPlayer.takeTurn(false);
            }
            else {
                bluePlayer.notify("I won!");
                redPlayer.notify("I lost!");
                bluePlayer.takeTurn(false);
            }

            redPlayer = null;
            bluePlayer = null;
            initializeNumbers();
        }
        else if (allNumbersAreTaken()) {
            redPlayer.notify("All numbers are taken!");
            bluePlayer.notify("All numbers are taken!");

            redPlayer = null;
            bluePlayer = null;
            initializeNumbers();
        }
        else if (color == 'r') {
            redPlayer.notify("Blue player's turn");
            redPlayer.takeTurn(false);
            bluePlayer.notify("It is my turn");
            bluePlayer.takeTurn(true);
        }
        else if (color == 'b') {
            bluePlayer.notify("Red player's turn");
            bluePlayer.takeTurn(false);
            redPlayer.notify("It is my turn");
            redPlayer.takeTurn(true);
        }

        return true;
    }

    private boolean checkIfDisconnected(CallBack player1, CallBack player2) throws RemoteException {
        if(player1 == null){
            if(player2 != null){
                player2.notify("Other player disconnected.");
                player2.takeTurn(false);
                return true;
            }
        }

        return false;
    }

    /** A client invokes this method on close request
     * in order to inform the server it disconnects
     */
    @Override
    public void disconnect(char color) throws RemoteException {
        if(color == 'r'){
            redPlayer = null;
            if(bluePlayer != null){
                bluePlayer.notify("Other player disconnected.");
                bluePlayer.takeTurn(false);
            }
        }
        else if(color == 'b'){
            bluePlayer = null;
            if(redPlayer != null){
                redPlayer.notify("Other player disconnected.");
                redPlayer.takeTurn(false);
            }
        }
        initializeNumbers();
    }

    /** Check if a player with the specified color wins */
    private boolean isWon(char color) {
        // the numbers the player of the specified color has taken
        // since the numbers are 9 in total, then the maximum size is 5
        int[] numbersOfColor = new int[5];
        // next index in numbersOfColor to add a number
        int index = 0;

        // find all numbers of this color and place them in numbersOfColor
        for (int i = 0; i < numbers.length; i++) {
            if(numbers[i] == color){
                numbersOfColor[index++] = i + 1;
            }
        }

        // index now holds the actual length of numberOfColor
        // if the player has chosen less than 3 numbers then there are no 3 numbers with sum 15
        if(index < 3){
            return false;
        }

        // find all sums of three numbers taken by the player
        for (int i = 0; i < index - 2; i++) {
            for (int j = i + 1; j < index - 1; j++) {
                for (int k = j + 1; k < index; k++) {

                    // check if sum is 15
                    if(numbersOfColor[i] + numbersOfColor[j] + numbersOfColor[k] == 15){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean allNumbersAreTaken(){
        for (int i = 0; i < numbers.length; i++) {
            if(numbers[i] == ' '){
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        try {
            Get15Interface obj = new Get15Implementation();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Get15Implementation", obj);
            System.out.println("Server " + obj + " registered");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
