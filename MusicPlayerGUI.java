import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// MusicPlayerGUI = the Swing window. It only shows things and calls MusicLibrary.
// Run this file (it has the main method).
public class MusicPlayerGUI extends JFrame {

    private MusicLibrary library = new MusicLibrary("Student");
    private ArrayList<Song> currentSongs = new ArrayList<>();   // songs currently shown in the table

    // ---------- Songs tab ----------
    private JTextField idField = new JTextField();
    private JTextField titleField = new JTextField();
    private JTextField artistField = new JTextField();
    private JTextField albumField = new JTextField();
    private JComboBox<String> genreBox = new JComboBox<>(MusicLibrary.GENRES);
    private JComboBox<Integer> ratingBox = new JComboBox<>();
    private JTextField searchField = new JTextField(12);
    private JComboBox<String> searchByBox = new JComboBox<>(new String[]{"Title", "Artist", "Album"});
    private JComboBox<String> sortByBox = new JComboBox<>(new String[]{"Title", "Artist", "Rating"});
    private DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"ID", "Title", "Artist", "Album", "Genre", "Rating"}, 0);
    private JTable songTable = new JTable(tableModel);

    // ---------- Playlists tab ----------
    private DefaultListModel<String> playlistNamesModel = new DefaultListModel<>();
    private JList<String> playlistList = new JList<>(playlistNamesModel);
    private DefaultListModel<String> playlistSongsModel = new DefaultListModel<>();
    private JTextField playlistNameField = new JTextField(12);
    private JTextField playlistSongIdField = new JTextField(6);
    private JTextArea reportArea = new JTextArea();

    // ---------- Player tab ----------
    private JLabel nowPlayingLabel = new JLabel();
    private DefaultListModel<String> queueModel = new DefaultListModel<>();
    private DefaultListModel<String> historyModel = new DefaultListModel<>();

    // ---------- Artists tab ----------
    private JTextArea artistArea = new JTextArea();

    public MusicPlayerGUI() {
        library.loadSampleData();

        setTitle("Music Playlist Management System");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);   // open in the center of the screen

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Songs", createSongsTab());
        tabs.addTab("Playlists", createPlaylistsTab());
        tabs.addTab("Player", createPlayerTab());
        tabs.addTab("Artists & Albums", createArtistsTab());
        add(tabs);

        showAllSongs();
        refreshPlaylists();
        refreshPlayer();
        refreshArtists();
    }

    // =====================================================
    //                      SONGS TAB
    // =====================================================
    private JPanel createSongsTab() {
        // Rating dropdown is filled from the RATINGS array
        for (int r : MusicLibrary.RATINGS) {
            ratingBox.addItem(r);
        }

        // Form: labels on row 1, inputs on row 2
        JPanel form = new JPanel(new GridLayout(2, 6, 5, 5));
        form.add(new JLabel("Song ID (e.g. S109)"));
        form.add(new JLabel("Title"));
        form.add(new JLabel("Artist"));
        form.add(new JLabel("Album"));
        form.add(new JLabel("Genre"));
        form.add(new JLabel("Rating"));
        form.add(idField);
        form.add(titleField);
        form.add(artistField);
        form.add(albumField);
        form.add(genreBox);
        form.add(ratingBox);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton playBtn = new JButton("Play");
        JButton queueBtn = new JButton("Add to Queue");

        JPanel buttons = new JPanel();
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);
        buttons.add(playBtn);
        buttons.add(queueBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        // Table: read-only, clicking a row fills the form
        songTable.setDefaultEditor(Object.class, null);
        songTable.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        // Search + sort bar
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JButton sortBtn = new JButton("Sort");

        JPanel bottom = new JPanel();
        bottom.add(new JLabel("Search:"));
        bottom.add(searchField);
        bottom.add(searchByBox);
        bottom.add(searchBtn);
        bottom.add(showAllBtn);
        bottom.add(new JLabel("      Sort by:"));
        bottom.add(sortByBox);
        bottom.add(sortBtn);

        // ---------- Button actions ----------
        addBtn.addActionListener(e -> {
            try {
                library.addSong(idField.getText(), titleField.getText(), artistField.getText(),
                        albumField.getText(), getSelectedGenre(), getSelectedRating());
                showAllSongs();
                refreshArtists();
                clearForm();
                info("Song added!");
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                library.updateSong(idField.getText(), titleField.getText(), artistField.getText(),
                        albumField.getText(), getSelectedGenre(), getSelectedRating());
                showAllSongs();
                refreshArtists();
                refreshPlayer();
                refreshPlaylistSongs();
                info("Song updated!");
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        deleteBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Delete song " + idField.getText() + "?");
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                library.deleteSong(idField.getText());
                showAllSongs();
                refreshArtists();
                refreshPlayer();
                refreshPlaylistSongs();
                clearForm();
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        clearBtn.addActionListener(e -> clearForm());

        playBtn.addActionListener(e -> {
            try {
                Song s = library.playSong(idField.getText());
                refreshPlayer();
                info("Now playing: " + s.getTitle() + " - " + s.getArtist());
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        queueBtn.addActionListener(e -> {
            try {
                library.addToQueue(idField.getText());
                refreshPlayer();
                info("Added to queue.");
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        searchBtn.addActionListener(e -> {
            try {
                ArrayList<Song> results = library.search(searchField.getText(), (String) searchByBox.getSelectedItem());
                showSongs(results);
                if (results.isEmpty()) {
                    info("No songs found.");
                }
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            showAllSongs();
        });

        sortBtn.addActionListener(e -> {
            library.sortSongs(currentSongs, (String) sortByBox.getSelectedItem());
            showSongs(currentSongs);
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(songTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // Puts a list of songs into the table
    private void showSongs(ArrayList<Song> list) {
        currentSongs = list;
        tableModel.setRowCount(0);   // clear old rows
        for (Song s : list) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getTitle(), s.getArtist(), s.getAlbum(), s.getGenre(), s.getRating()
            });
        }
    }

    private void showAllSongs() {
        ArrayList<Song> all = library.getAllSongs();
        library.sortSongs(all, "Title");
        showSongs(all);
    }

    // When a row is clicked, copy that song's details into the form
    private void fillFormFromTable() {
        int row = songTable.getSelectedRow();
        if (row == -1 || row >= currentSongs.size()) {
            return;
        }
        Song s = currentSongs.get(row);
        idField.setText(s.getId());
        titleField.setText(s.getTitle());
        artistField.setText(s.getArtist());
        albumField.setText(s.getAlbum());
        genreBox.setSelectedItem(s.getGenre());
        ratingBox.setSelectedItem(s.getRating());
    }

    private void clearForm() {
        idField.setText("");
        titleField.setText("");
        artistField.setText("");
        albumField.setText("");
        genreBox.setSelectedIndex(0);
        ratingBox.setSelectedIndex(0);
        songTable.clearSelection();
    }

    private String getSelectedGenre() {
        return (String) genreBox.getSelectedItem();
    }

    private int getSelectedRating() {
        return (Integer) ratingBox.getSelectedItem();
    }

    // =====================================================
    //                    PLAYLISTS TAB
    // =====================================================
    private JPanel createPlaylistsTab() {
        JButton createBtn = new JButton("Create");
        JButton deletePlBtn = new JButton("Delete Selected Playlist");
        JButton addSongBtn = new JButton("Add Song");
        JButton removeSongBtn = new JButton("Remove Song");
        JButton reportBtn = new JButton("Generate Report");

        JPanel row1 = new JPanel();
        row1.add(new JLabel("New playlist name:"));
        row1.add(playlistNameField);
        row1.add(createBtn);
        row1.add(deletePlBtn);

        JPanel row2 = new JPanel();
        row2.add(new JLabel("Song ID:"));
        row2.add(playlistSongIdField);
        row2.add(addSongBtn);
        row2.add(removeSongBtn);
        row2.add(reportBtn);

        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.add(row1);
        controls.add(row2);

        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel lists = new JPanel(new GridLayout(1, 3, 5, 5));
        lists.add(titledScroll(playlistList, "Playlists (click one)"));
        lists.add(titledScroll(new JList<>(playlistSongsModel), "Songs in playlist"));
        lists.add(titledScroll(reportArea, "Report"));

        playlistList.addListSelectionListener(e -> refreshPlaylistSongs());

        // ---------- Button actions ----------
        createBtn.addActionListener(e -> {
            try {
                library.createPlaylist(playlistNameField.getText());
                playlistNameField.setText("");
                refreshPlaylists();
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        deletePlBtn.addActionListener(e -> {
            try {
                library.deletePlaylist(getSelectedPlaylistName());
                refreshPlaylists();
                reportArea.setText("");
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        addSongBtn.addActionListener(e -> {
            try {
                library.addSongToPlaylist(getSelectedPlaylistName(), playlistSongIdField.getText());
                playlistSongIdField.setText("");
                refreshPlaylistSongs();
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        removeSongBtn.addActionListener(e -> {
            try {
                library.removeSongFromPlaylist(getSelectedPlaylistName(), playlistSongIdField.getText());
                playlistSongIdField.setText("");
                refreshPlaylistSongs();
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        reportBtn.addActionListener(e -> {
            try {
                reportArea.setText(library.getPlaylistReport(getSelectedPlaylistName()));
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(controls, BorderLayout.NORTH);
        panel.add(lists, BorderLayout.CENTER);
        return panel;
    }

    private String getSelectedPlaylistName() throws InvalidOperationException {
        String name = playlistList.getSelectedValue();
        if (name == null) {
            throw new InvalidOperationException("Select a playlist first.");
        }
        return name;
    }

    private void refreshPlaylists() {
        playlistNamesModel.clear();
        for (Playlist p : library.getPlaylists()) {
            playlistNamesModel.addElement(p.getName());
        }
        playlistSongsModel.clear();
    }

    private void refreshPlaylistSongs() {
        playlistSongsModel.clear();
        String name = playlistList.getSelectedValue();
        if (name == null) {
            return;
        }
        try {
            for (Song s : library.getPlaylist(name).getSongs()) {
                playlistSongsModel.addElement(s.toString());
            }
        } catch (InvalidOperationException ex) {
            error(ex.getMessage());
        }
    }

    // =====================================================
    //                     PLAYER TAB
    // =====================================================
    private JPanel createPlayerTab() {
        nowPlayingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JButton playNextBtn = new JButton("Play Next from Queue");

        JPanel top = new JPanel();
        top.add(nowPlayingLabel);
        top.add(playNextBtn);

        JPanel lists = new JPanel(new GridLayout(1, 2, 5, 5));
        lists.add(titledScroll(new JList<>(queueModel), "Up Next (Queue)"));
        lists.add(titledScroll(new JList<>(historyModel), "Recently Played (History)"));

        playNextBtn.addActionListener(e -> {
            try {
                library.playNext();
                refreshPlayer();
            } catch (InvalidOperationException ex) {
                error(ex.getMessage());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(top, BorderLayout.NORTH);
        panel.add(lists, BorderLayout.CENTER);
        return panel;
    }

    private void refreshPlayer() {
        queueModel.clear();
        for (Song s : library.getQueue()) {
            queueModel.addElement(s.toString());
        }

        historyModel.clear();
        for (Song s : library.getHistory()) {
            historyModel.addElement(s.toString());
        }

        // The newest song in history is the one playing now
        if (library.getHistory().isEmpty()) {
            nowPlayingLabel.setText("Nothing playing yet");
        } else {
            Song s = library.getHistory().getFirst();
            nowPlayingLabel.setText("Now Playing: " + s.getTitle() + " - " + s.getArtist() + "     ");
        }
    }

    // =====================================================
    //                ARTISTS & ALBUMS TAB
    // =====================================================
    private JPanel createArtistsTab() {
        artistArea.setEditable(false);
        artistArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(artistArea), BorderLayout.CENTER);
        return panel;
    }

    private void refreshArtists() {
        artistArea.setText(library.getArtistsAndAlbumsReport());
    }

    // =====================================================
    //                       HELPERS
    // =====================================================
    private JScrollPane titledScroll(Component c, String title) {
        JScrollPane scroll = new JScrollPane(c);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================
    //                        MAIN
    // =====================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicPlayerGUI().setVisible(true));
    }
}
