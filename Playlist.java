import java.util.ArrayList;

// Playlist = a named list of songs made by the user.
public class Playlist {

    private String name;
    private ArrayList<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName()            { return name; }
    public ArrayList<Song> getSongs()  { return songs; }

    public void addSong(Song song) throws InvalidOperationException {
        if (songs.contains(song)) {
            throw new InvalidOperationException("\"" + song.getTitle() + "\" is already in " + name);
        }
        songs.add(song);
    }

    public void removeSong(Song song) throws InvalidOperationException {
        // remove() returns false if the song was not in the list
        if (!songs.remove(song)) {
            throw new InvalidOperationException("\"" + song.getTitle() + "\" is not in " + name);
        }
    }
}
