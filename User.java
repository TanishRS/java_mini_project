import java.util.ArrayList;

// User = the person using the app. Owns the playlists.
public class User {

    private String name;
    private ArrayList<Playlist> playlists;

    public User(String name) {
        this.name = name;
        this.playlists = new ArrayList<>();
    }

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public ArrayList<Playlist> getPlaylists() { return playlists; }

    public void addPlaylist(Playlist p)      { playlists.add(p); }
    public void removePlaylist(Playlist p)   { playlists.remove(p); }

    // Returns the playlist with this name, or null if not found
    public Playlist findPlaylist(String name) {
        for (Playlist p : playlists) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }
}
