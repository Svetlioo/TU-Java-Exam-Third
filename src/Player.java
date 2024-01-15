import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private String password;
    private int rating;
    private boolean isPlaying;

    public Player(String name, String password, int rating, boolean isPlaying) {
        this.name = name;
        this.password = password;
        this.rating = rating;
        this.isPlaying = isPlaying;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
