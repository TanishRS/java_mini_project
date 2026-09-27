import java.util.ArrayList;

// Artist = a singer/band and all their songs in the library.
public class Artist {

    private String name;
    private ArrayList<Song> songs;

    public Artist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName()            { return name; }
    public ArrayList<Song> getSongs()  { return songs; }

    public void addSong(Song song)     { songs.add(song); }
    public void removeSong(Song song)  { songs.remove(song); }
}
