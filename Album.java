import java.util.ArrayList;

// Album = a collection of songs released together.
public class Album {

    private String title;
    private String artistName;
    private ArrayList<Song> songs;

    public Album(String title, String artistName) {
        this.title = title;
        this.artistName = artistName;
        this.songs = new ArrayList<>();
    }

    public String getTitle()           { return title; }
    public String getArtistName()      { return artistName; }
    public ArrayList<Song> getSongs()  { return songs; }

    public void addSong(Song song)     { songs.add(song); }
    public void removeSong(Song song)  { songs.remove(song); }
}
