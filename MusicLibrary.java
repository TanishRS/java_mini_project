import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

// MusicLibrary = the "brain" of the app.
// All data + all logic lives here. The GUI only calls these methods.
public class MusicLibrary {

    // ===== ARRAYS: fixed lists of allowed values =====
    public static final String[] GENRES = {"Pop", "Rock", "Hip-Hop", "Bollywood", "Classical", "Jazz", "EDM"};
    public static final int[] RATINGS = {1, 2, 3, 4, 5};

    private static final int MAX_HISTORY = 10;

    // ===== COLLECTIONS =====
    private HashMap<String, Song> songs = new HashMap<>();       // Song ID -> Song (fast lookup by ID)
    private TreeMap<String, Artist> artists = new TreeMap<>();   // Artist name -> Artist (auto-sorted A-Z)
    private TreeMap<String, Album> albums = new TreeMap<>();     // Album title -> Album (auto-sorted A-Z)
    private LinkedList<Song> queue = new LinkedList<>();         // songs waiting to play (FIFO)
    private LinkedList<Song> history = new LinkedList<>();       // recently played, newest first
    private User user;

    public MusicLibrary(String userName) {
        user = new User(userName);
    }

    // =====================================================
    //                    VALIDATION
    // =====================================================
    private void validateSong(String id, String title, String artist, String album, String genre, int rating)
            throws InvalidOperationException {

        // "S\\d{3}" means: letter S followed by exactly 3 digits (S101, S999...)
        if (!id.matches("S\\d{3}")) {
            throw new InvalidOperationException("Song ID must be S + 3 digits (e.g. S101).");
        }
        if (title.isEmpty() || title.length() > 50) {
            throw new InvalidOperationException("Title must be 1-50 characters.");
        }
        if (artist.isEmpty()) {
            throw new InvalidOperationException("Artist name can't be empty.");
        }
        if (album.isEmpty()) {
            throw new InvalidOperationException("Album name can't be empty.");
        }
        if (!isValidGenre(genre)) {
            throw new InvalidOperationException("Invalid genre: " + genre);
        }
        if (rating < 1 || rating > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5.");
        }
    }

    // Checks the genre against the GENRES array
    private boolean isValidGenre(String genre) {
        for (String g : GENRES) {
            if (g.equals(genre)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================
    //                SONG CRUD (Create/Read/Update/Delete)
    // =====================================================

    // CREATE
    public void addSong(String id, String title, String artist, String album, String genre, int rating)
            throws InvalidOperationException {
        id = id.trim().toUpperCase();
        title = title.trim();
        artist = artist.trim();
        album = album.trim();

        validateSong(id, title, artist, album, genre, rating);

        if (songs.containsKey(id)) {
            throw new InvalidOperationException("A song with ID " + id + " already exists.");
        }

        Song song = new Song(id, title, artist, album, genre, rating);
        songs.put(id, song);
        linkToArtistAndAlbum(song);
    }

    // READ (one song)
    public Song getSong(String id) throws InvalidOperationException {
        Song song = songs.get(id.trim().toUpperCase());
        if (song == null) {
            throw new InvalidOperationException("No song found with ID \"" + id + "\"");
        }
        return song;
    }

    // READ (all songs)
    public ArrayList<Song> getAllSongs() {
        return new ArrayList<>(songs.values());
    }

    // UPDATE (ID stays the same, everything else can change)
    public void updateSong(String id, String title, String artist, String album, String genre, int rating)
            throws InvalidOperationException {
        Song song = getSong(id);
        title = title.trim();
        artist = artist.trim();
        album = album.trim();

        validateSong(song.getId(), title, artist, album, genre, rating);

        unlinkFromArtistAndAlbum(song);   // remove from old artist/album
        song.setTitle(title);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setGenre(genre);
        song.setRating(rating);
        linkToArtistAndAlbum(song);       // add to new artist/album
    }

    // DELETE (also removes it from playlists, queue and history)
    public void deleteSong(String id) throws InvalidOperationException {
        Song song = getSong(id);
        songs.remove(song.getId());
        unlinkFromArtistAndAlbum(song);

        for (Playlist p : user.getPlaylists()) {
            p.getSongs().remove(song);
        }
        queue.remove(song);
        history.remove(song);
    }

    // =====================================================
    //            ARTIST & ALBUM MANAGEMENT
    // =====================================================

    // Adds the song to its Artist and Album (creates them if they don't exist yet)
    private void linkToArtistAndAlbum(Song song) {
        Artist artist = artists.get(song.getArtist());
        if (artist == null) {
            artist = new Artist(song.getArtist());
            artists.put(song.getArtist(), artist);
        }
        artist.addSong(song);

        Album album = albums.get(song.getAlbum());
        if (album == null) {
            album = new Album(song.getAlbum(), song.getArtist());
            albums.put(song.getAlbum(), album);
        }
        album.addSong(song);
    }

    // Removes the song from its Artist and Album (deletes them if they become empty)
    private void unlinkFromArtistAndAlbum(Song song) {
        Artist artist = artists.get(song.getArtist());
        if (artist != null) {
            artist.removeSong(song);
            if (artist.getSongs().isEmpty()) {
                artists.remove(artist.getName());
            }
        }

        Album album = albums.get(song.getAlbum());
        if (album != null) {
            album.removeSong(song);
            if (album.getSongs().isEmpty()) {
                albums.remove(album.getTitle());
            }
        }
    }

    // TreeMap keeps keys sorted, so this list comes out A-Z automatically
    public String getArtistsAndAlbumsReport() {
        String text = "===== ARTISTS (A-Z) =====\n";
        for (Artist a : artists.values()) {
            text += a.getName() + "  (" + a.getSongs().size() + " songs)\n";
            for (Song s : a.getSongs()) {
                text += "     - " + s.getTitle() + "\n";
            }
        }

        text += "\n===== ALBUMS (A-Z) =====\n";
        for (Album al : albums.values()) {
            text += al.getTitle() + " by " + al.getArtistName() + "  (" + al.getSongs().size() + " songs)\n";
        }
        return text;
    }

    // =====================================================
    //                SEARCH (linear search)
    // =====================================================
    public ArrayList<Song> search(String text, String searchBy) throws InvalidOperationException {
        text = text.trim().toLowerCase();
        if (text.isEmpty()) {
            throw new InvalidOperationException("Type something to search.");
        }

        ArrayList<Song> results = new ArrayList<>();
        for (Song s : songs.values()) {
            String value;
            if (searchBy.equals("Artist")) {
                value = s.getArtist();
            } else if (searchBy.equals("Album")) {
                value = s.getAlbum();
            } else {
                value = s.getTitle();
            }

            if (value.toLowerCase().contains(text)) {
                results.add(s);
            }
        }
        return results;
    }

    // =====================================================
    //                      SORTING
    // =====================================================
    // (a, b) -> ... is a Comparator: negative = a first, positive = b first
    public void sortSongs(ArrayList<Song> list, String sortBy) {
        if (sortBy.equals("Artist")) {
            list.sort((a, b) -> a.getArtist().compareToIgnoreCase(b.getArtist()));
        } else if (sortBy.equals("Rating")) {
            list.sort((a, b) -> b.getRating() - a.getRating());   // highest rating first
        } else {
            list.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        }
    }

    // =====================================================
    //            PLAYBACK QUEUE & HISTORY (LinkedList)
    // =====================================================
    public void addToQueue(String id) throws InvalidOperationException {
        Song song = getSong(id);
        if (queue.contains(song)) {
            throw new InvalidOperationException("\"" + song.getTitle() + "\" is already in the queue.");
        }
        queue.addLast(song);   // join at the back
    }

    public Song playNext() throws InvalidOperationException {
        if (queue.isEmpty()) {
            throw new InvalidOperationException("Queue is empty. Add some songs first.");
        }
        Song song = queue.removeFirst();   // take from the front
        addToHistory(song);
        return song;
    }

    public Song playSong(String id) throws InvalidOperationException {
        Song song = getSong(id);
        addToHistory(song);
        return song;
    }

    private void addToHistory(Song song) {
        history.remove(song);        // if already there, remove it (no duplicates)
        history.addFirst(song);      // newest goes on top
        if (history.size() > MAX_HISTORY) {
            history.removeLast();    // keep only last 10
        }
    }

    public LinkedList<Song> getQueue()   { return queue; }
    public LinkedList<Song> getHistory() { return history; }

    // =====================================================
    //                 PLAYLIST MANAGEMENT
    // =====================================================
    public void createPlaylist(String name) throws InvalidOperationException {
        name = name.trim();
        if (name.isEmpty() || name.length() > 30) {
            throw new InvalidOperationException("Playlist name must be 1-30 characters.");
        }
        if (user.findPlaylist(name) != null) {
            throw new InvalidOperationException("Playlist \"" + name + "\" already exists.");
        }
        user.addPlaylist(new Playlist(name));
    }

    public Playlist getPlaylist(String name) throws InvalidOperationException {
        Playlist p = user.findPlaylist(name);
        if (p == null) {
            throw new InvalidOperationException("Playlist not found: " + name);
        }
        return p;
    }

    public void deletePlaylist(String name) throws InvalidOperationException {
        user.removePlaylist(getPlaylist(name));
    }

    public void addSongToPlaylist(String playlistName, String songId) throws InvalidOperationException {
        getPlaylist(playlistName).addSong(getSong(songId));
    }

    public void removeSongFromPlaylist(String playlistName, String songId) throws InvalidOperationException {
        getPlaylist(playlistName).removeSong(getSong(songId));
    }

    public ArrayList<Playlist> getPlaylists() {
        return user.getPlaylists();
    }

    // =====================================================
    //                  PLAYLIST REPORT
    // =====================================================
    public String getPlaylistReport(String name) throws InvalidOperationException {
        Playlist p = getPlaylist(name);
        ArrayList<Song> list = p.getSongs();

        String report = "===== PLAYLIST REPORT =====\n";
        report += "Playlist   : " + p.getName() + "\n";
        report += "Owner      : " + user.getName() + "\n";
        report += "Songs      : " + list.size() + "\n";

        if (list.isEmpty()) {
            return report + "\n(This playlist is empty)";
        }

        int total = 0;
        Song top = list.get(0);
        int[] genreCount = new int[GENRES.length];   // one counter per genre

        for (Song s : list) {
            total += s.getRating();
            if (s.getRating() > top.getRating()) {
                top = s;
            }
            for (int i = 0; i < GENRES.length; i++) {
                if (GENRES[i].equals(s.getGenre())) {
                    genreCount[i]++;
                }
            }
        }

        double average = (double) total / list.size();
        report += "Avg Rating : " + String.format("%.1f", average) + " / 5\n";
        report += "Top Song   : " + top.getTitle() + " - " + top.getArtist() + "\n\n";

        report += "Genre Breakdown:\n";
        for (int i = 0; i < GENRES.length; i++) {
            if (genreCount[i] > 0) {
                report += "   " + GENRES[i] + ": " + genreCount[i] + "\n";
            }
        }

        report += "\nTrack List:\n";
        for (int i = 0; i < list.size(); i++) {
            report += "   " + (i + 1) + ". " + list.get(i) + "\n";
        }
        return report;
    }

    // =====================================================
    //          SAMPLE DATA (so the app isn't empty)
    // =====================================================
    public void loadSampleData() {
        try {
            addSong("S101", "Tum Hi Ho", "Arijit Singh", "Aashiqui 2", "Bollywood", 5);
            addSong("S102", "Kesariya", "Arijit Singh", "Brahmastra", "Bollywood", 4);
            addSong("S103", "Shape of You", "Ed Sheeran", "Divide", "Pop", 4);
            addSong("S104", "Perfect", "Ed Sheeran", "Divide", "Pop", 5);
            addSong("S105", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", 5);
            addSong("S106", "Blinding Lights", "The Weeknd", "After Hours", "Pop", 4);
            addSong("S107", "Lose Yourself", "Eminem", "8 Mile", "Hip-Hop", 5);
            addSong("S108", "Take Five", "Dave Brubeck", "Time Out", "Jazz", 3);

            createPlaylist("Chill Vibes");
            addSongToPlaylist("Chill Vibes", "S104");
            addSongToPlaylist("Chill Vibes", "S108");
            addSongToPlaylist("Chill Vibes", "S101");

            createPlaylist("Gym Mode");
            addSongToPlaylist("Gym Mode", "S107");
            addSongToPlaylist("Gym Mode", "S106");
        } catch (InvalidOperationException e) {
            System.out.println("Sample data error: " + e.getMessage());
        }
    }
}
