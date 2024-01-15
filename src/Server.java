import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Server {
    private static final String FILE_NAME = "players.data";
    private static final Pattern VALID_MOVE_PATTERN = Pattern.compile("^[KQRBN]x?[a-h][1-8][+#]?$");
    private ServerSocket server;
    private final Object playersLock;
    private Player[] players;
    private final Queue<Connection> playersQueue;

    public Server() {
        this.playersLock = new Object();
        this.players = loadPlayers();
        this.playersQueue = new ArrayDeque<>();
    }


    public void start() {
        try {
            this.server = new ServerSocket(8080);
            while (true) {
                Socket client = server.accept();

                Thread clientThread = new Thread(() -> {
                    try (Scanner in = new Scanner(client.getInputStream()); PrintStream out = new PrintStream(client.getOutputStream())) {
                        userMenu(in, out);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    private void userMenu(Scanner in, PrintStream out) {
        out.println("Enter Y to login");
        if (!in.nextLine().equalsIgnoreCase("Y")) {
            out.println("Goodbye.");
        }
        out.println("Enter username");
        String username = in.nextLine();
        out.println("Enter password: ");
        String password = in.nextLine();
        Player player = login(username, password);
        if (player == null) {
            out.println("No such credentials.");
            return;
        }
        out.println("Enter W to play with the whites. Enter B to play with the blacks.");
        String input = in.nextLine();
        Connection currentPlayer = new Connection(player, in, out);
        if (input.equalsIgnoreCase("B")) {
            playersQueue.offer(currentPlayer);
        } else if (input.equalsIgnoreCase("W")) {
            Connection enemyPlayer = getPlayer2(currentPlayer);
            if (enemyPlayer == null) {
                out.println("No appropriate player found.");
                return;
            }
            currentPlayer.getPlayer().setPlaying(true);
            enemyPlayer.getPlayer().setPlaying(true);
            startNewGame(currentPlayer, enemyPlayer);
        } else {
            out.println("Invalid option.");
        }


    }

    private void startNewGame(Connection player1, Connection player2) {

        player1.receiveMessage(String.format("Game has started.\nYou are currently playing versus %s.", player2.getPlayer().getName()));
        player2.receiveMessage(String.format("Game has started.\nYou are currently playing versus %s.", player1.getPlayer().getName()));

        while (true) {
            if (chessMove(player1, player2)) break;
            if (chessMove(player2, player1)) break;
        }

    }

    private boolean chessMove(Connection player1, Connection player2) {
        player1.receiveMessage("Enter your move: ");
        String player1Move = player1.sendMessage();
        if (isValidMove(player1Move)) {
            player2.receiveMessage("Your opponents move: " + player1Move);
            if (player1Move.contains("#")) {
                player1.receiveMessage("You have won the game.");
                player2.receiveMessage("You have lost the game.");
                updateRating(player1, player2);
                player1.receiveMessage("Your new rating: " + player1.getPlayer().getRating());
                player2.receiveMessage("Your new rating: " + player2.getPlayer().getRating());
                return true;
            }
        }
        return false;
    }

    private boolean isValidMove(String move) {
        return VALID_MOVE_PATTERN.matcher(move).matches();
    }

    private Player[] loadPlayers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Player[]) in.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Error loading the file.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("There is no player class.");
        }
    }

    private void savePlayers(Player[] players) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(players);
        } catch (IOException e) {
            throw new RuntimeException("Error saving the players to the file.");
        }
    }

    private void updateRating(Connection winner, Connection loser) {
        synchronized (playersLock) {
            Player[] players = loadPlayers();
            for (Player player : loadPlayers()) {
                if (player.equals(winner.getPlayer())) {
                    Player winnerPlayer = winner.getPlayer();
                    winnerPlayer.setRating(winnerPlayer.getRating() + 10);
                }
                if (player.equals(loser.getPlayer())) {
                    Player loserPlayer = loser.getPlayer();
                    if (loserPlayer.getRating() >= 10) {
                        loserPlayer.setRating(loserPlayer.getRating() - 10);
                    }
                }
            }
            savePlayers(players);

        }
    }

    private Player login(String name, String password) {
        synchronized (playersLock) {
            this.players = loadPlayers();
        }

        for (Player player : this.players) {
            if (Objects.equals(player.getName(), name) && Objects.equals(player.getPassword(), password)) {
                return player;
            }
        }
        return null;

    }

    private Connection getPlayer2(Connection player1) {
        for (Connection playerConnection : playersQueue) {
            if (!playerConnection.equals(player1) && Math.abs(playerConnection.getPlayer().getRating() - player1.getPlayer().getRating()) < 200 && !playerConnection.getPlayer().isPlaying()) {
                return playerConnection;
            }
        }
        return null;
    }


}
