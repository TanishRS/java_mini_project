// Song = one track in the library.
public class Song {

    private String id;       // e.g. S101
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int rating;      // 1 to 5

    // Constructor: runs when we do "new Song(...)"
    public Song(String id, String title, String artist, String album, String genre, int rating) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.rating = rating;
    }

    // Getters
    public String getId()     { return id; }
    public String getTitle()  { return title; }
    public String getArtist() { return artist; }
    public String getAlbum()  { return album; }
    public String getGenre()  { return genre; }
    public int getRating()    { return rating; }

    // Setters (ID has no setter because it should never change)
    public void setTitle(String title)   { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album)   { this.album = album; }
    public void setGenre(String genre)   { this.genre = genre; }
    public void setRating(int rating)    { this.rating = rating; }

    // How a song looks when printed / shown in a list
    @Override
    public String toString() {
        return id + " | " + title + " - " + artist + " (" + rating + "/5)";
    }
}
