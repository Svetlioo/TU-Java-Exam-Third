import java.io.PrintStream;
import java.util.Scanner;

public class Connection {
    private Player player;
    private PrintStream out;
    private Scanner in;

    public void receiveMessage(String message) {
        out.println(message);
    }

    public String sendMessage() {
        return in.nextLine();
    }

    public Connection(Player player, Scanner in, PrintStream out) {
        this.player = player;
        this.out = out;
        this.in = in;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public Scanner getIn() {
        return in;
    }

    public void setIn(Scanner in) {
        this.in = in;
    }
}
