package src;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.derby.jdbc.EmbeddedDriver;


public class Main 
{
    private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

    public static void main(String[] args) {

			try{
				Driver d = new EmbeddedDriver();
				Connection conn = d.connect("jdbc:derby:AppleMusicDB;create=true", null);
				createTables(conn);
				resetTables(conn);

			
			displayMenu();
			loop: while (true) {
				switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
				case "0": // Quit
					break loop;

				case "1": // Reset
					resetTables(conn);
					break;

				case "2": // List all the songs from a specific genre
                listAllSongsFromGenre(conn);
				break;

				case "3": // List all the songs from a specific artist
                    displaySongsOfArtist(conn);
					break;

				case "4": // Add a song to the database
					addSong(conn);
					break;

				case "5": // List all the albums from a specific artist
                    displayAlbumsOfArtist(conn);
					break;

				case "6": // Add a playlist to the database
					addPlaylist(conn);
					break;

				case "7": //  List all the playlists from a specific user
                    displayPlaylistsOfUser(conn);
					break;

				case "8": //  List all the songs from a specific year
                	displaySongsOfYear(conn);
					break;
					

				default:
					displayMenu();
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		out.println("Done");
	}


    private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List all the songs from a specific genre");
		out.println("3: List songs from an artist");
		out.println("4: Add song");
		out.println("5: List albums from an artist");
		out.println("6: Add playlist");
		out.println("7: List playlist from a specific user");
		out.println("8: List songs from a specific year");
	}

	private static String requestString(String prompt) {
		out.print(prompt);
		out.flush();
		return in.nextLine();
	}

	private static void createTables(Connection conn) {
		// First clean up from previous runs, if any
		dropTables(conn);

		// Now create the schema
		addTables(conn);
	}

    private static void doUpdate(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doUpdateNoError(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			// Ignore error
		}
	}

    private static void addTables(Connection conn) {

		StringBuilder sb = new StringBuilder();
		sb.append("create table ARTIST(");
		sb.append("  artistID int not null,");
		sb.append("  arname varchar(50) not null,");
		sb.append("  genre varchar(50) not null,");
		sb.append("  primary key(artistID)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table ARTIST created.");

		sb = new StringBuilder();
		sb.append("create table ALBUM(");
		sb.append("  albumID int not null,");
		sb.append("  alname varchar(100) not null,");
		sb.append("  date int not null,");
        sb.append("  artistID int not null,");
		sb.append("  primary key(albumID),");
        sb.append("  foreign key (artistID) references ARTIST on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table ALBUM created.");

		sb = new StringBuilder();
		sb.append("create table SONG(");
		sb.append("  songID int not null,");
		sb.append("  sname varchar(100) not null,");
		sb.append("  duration int not null,");
		sb.append("  artistID int not null,");
        sb.append("  albumID int not null,");
		sb.append("  primary key(songID),");
        sb.append("  foreign key (albumID) references ALBUM on delete no action,");
		sb.append("  foreign key (artistID) references ARTIST on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table SONG created.");
		

		sb = new StringBuilder();
		sb.append("create table PLAYLIST(");
		sb.append("  playlistID int not null,");
		sb.append("  pname varchar(100) not null,");
		sb.append("  creator varchar(50) not null,");
		sb.append("  pdate int not null,");
		sb.append("  primary key(playlistID)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table PLAYLIST created.");
	}

    private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table PLAYLIST", "Table PLAYLIST dropped.");
		doUpdateNoError(conn, "drop table SONG", "Table SONG dropped.");
		doUpdateNoError(conn, "drop table ALBUM", "Table ALBUM dropped.");
		doUpdateNoError(conn, "drop table ARTIST", "Table ARTIST dropped.");	
	}

    private static void resetTables(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			int count = 0;
			count += stmt.executeUpdate("delete from PLAYLIST");
			count += stmt.executeUpdate("delete from SONG");
			count += stmt.executeUpdate("delete from ALBUM");
			count += stmt.executeUpdate("delete from ARTIST");
			System.out.println(count + " records deleted");

			String[] artistvals = {
				"(12, 'Adele', 'Pop')",
				"(37, 'The Wanted', 'Pop')",
				"(4, 'Rihanna', 'Pop')",
				"(90, 'Lady A', 'Pop')",
				"(25, 'Ed Sheeran', 'Pop')",
				"(19, 'Billie Eilish', 'Alternative')",
				"(48, 'Pitbull', 'Pop')",
				"(67, 'Future', 'Rap')"
			};
			count = 0;
			for (String val : artistvals) {
				count += stmt.executeUpdate("insert into ARTIST(artistID, arname, genre) values " + val);
			}
			System.out.println(count + " ARTIST records inserted.");

			String[] albumVals = {
				"(5, 'Twenty One', 2011, 12)",
				"(9, 'Most Wanted: The Greatest hits', 2011, 37)",
				"(13, 'Loud', 2010, 4)",
				"(16, 'ANTI', 2016, 4)",
				"(25, 'Need You Know', 2010, 90)",
				"(37, 'Divide', 2017, 25)",
				"(7, 'dont smile at me', 2016, 19)",
				"(8, 'When we fall asleep, where do we go?', 2019, 19)",
				"(63, 'Globalization', 2014, 48)",
				"(62, 'Planet Pit', 2011, 48)",
				"(55, 'HNDRXX', 2017, 67)",
				"(56, 'FUTURE', 2017, 67)"
			};

			count = 0;
			for (String val : albumVals) {
				count += stmt.executeUpdate("insert into ALBUM(albumID, alname, date, artistID) values " + val);
			}
			System.out.println(count + " ALBUM records inserted.");


			String[] songvals = {
					"(12, 'Someone Like You', 446, 12, 5)", 
					"(13, 'Rolling in the Deep', 349, 12, 5)",
					"(14, 'Set Fire to the Rain', 403, 12, 5)",
					"(25, 'Glad You Came', 318, 37, 9)",
                    "(34, 'Desperado', 305, 4, 16)",
					"(35, 'Needed Me', 312, 4, 16)", 
					"(30, 'What is My Name?', 423, 4, 13)", 
					"(84, 'Need You Know', 438, 90, 25)",
                    "(67, 'Perfect', 423, 25, 37)",
					"(68, 'Galway Girl', 251, 25, 37)",
					"(69, 'happier', 328, 25, 37)",
                    "(2, 'ocean eyes', 320, 19, 7)",
					"(4, 'bad guy', 315, 19, 8)",
					"(5, 'when the party is over', 317, 19, 8)",
                    "(46, 'Time of Our Lives', 349, 48, 63)",
					"(44, 'Give me Everything', 412, 48, 62)",
					"(45, 'Pause', 300, 48, 62)",
                    "(58, 'Solo', 426, 67, 55)",
					"(59, 'Mask Off', 324, 67, 56)",
					"(60, 'Zoom', 312, 67, 56)"
			};
			count = 0;
			for (String val : songvals) {
				count += stmt.executeUpdate("insert into SONG(songID, sname, duration, artistID, albumID) values " + val);
			}
			System.out.println(count + " SONG records inserted.");
			
			
			String[] playlistvals = {
					"(3, 'Party', 'Sara G', 2017)",
                    "(42, 'Sad', 'Ella P', 2015)",
					"(43, 'Travel', 'Ella P', 2020)",
                    "(24, '2016 HITS', 'Mike T', 2016)",
					"(25, '2017 HITS', 'Mike T', 2017)",
					"(26, '2018 HITS', 'Mike T', 2018)",
                    "(13, 'Best Songs', 'James H', 2021)",
                    "(73, 'Rap', 'Liv R', 2018)",
					"(74, 'Spanish', 'Liv R', 2020)"        
			};
			count = 0;
			for (String val : playlistvals) {
				count += stmt.executeUpdate("insert into PLAYLIST(playlistID, pname, creator, pdate) values " + val);
			}
			System.out.println(count + " PLAYLIST records inserted.");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Print all the songs from a specific genre

	private static void listAllSongsFromGenre(Connection conn) {

		String genre = requestString("Genre? ");

		StringBuilder query = new StringBuilder();
		query.append("select s.sname, ar.arname, al.alname, ar.genre");
		query.append("  from Song s, Artist ar, Album al");
		query.append("   where ar.genre = ?");
        query.append("    and al.artistID = ar.artistID");
		query.append("    and ar.artistID = s.artistID");
		query.append("    and s.albumID = al.albumID");



		try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
			pstmt.setString(1, genre);
			ResultSet rs = pstmt.executeQuery();

			out.printf("%-20s %-20s %-20s %-20s\n","Song", "Artist", "Album", "Genre");
			out.println("--------------------------------------------------------------------------------");
			while (rs.next()) {
				String sname = rs.getString("sname");
				String arname = rs.getString("arname");
				String alname = rs.getString("alname");
				String argenre = rs.getString("genre");


				out.printf("%-20s %-20s %-20s %-20s\n", sname, arname, alname, argenre);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

        // Print all the songs from a specific artist 

        private static void displaySongsOfArtist(Connection conn) {
            String name = requestString("Artist Name? ");
            
            StringBuilder query = new StringBuilder();
            query.append("select s.sname, ar.arname, al.alname");
            query.append("  from Song s, Artist ar, Album al");
            query.append("  where ar.arname = ?");
			query.append("    and al.artistID = ar.artistID");
			query.append("    and ar.artistID = s.artistID");
			query.append("    and s.albumID = al.albumID");

    
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();

                out.printf("%-20s %-20s %-20s\n","Song", "Artist", "Album");
                out.println("---------------------------------------------------------------------------");
                while (rs.next()) {
                    String sname = rs.getString("sname");
					String arname = rs.getString("arname");
					String alname = rs.getString("alname");
    
                    out.printf("%-20s %-20s %-20s\n", sname, arname, alname);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
        }
    

        // Add a song

        private static void addSong(Connection conn) {
            String songID = requestString("Song ID? ");
            String sname = requestString("Song name? ");
            String duration = requestString("Song duration? ");
            String artistID = requestString("Artist ID? ");
			String albumID = requestString("Album ID? ");
            
            
    
            StringBuilder command = new StringBuilder();
            command.append("insert into SONG(songID, sname, duration, artistID, albumID) values (?,?,?,?,?)");
    
            try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
                pstmt.setString(1, songID);
                pstmt.setString(2, sname);
                pstmt.setString(3, duration);
                pstmt.setString(4, artistID);
				pstmt.setString(5, albumID);
                int count = pstmt.executeUpdate();
    
                out.println(count + " song(s) inserted");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

		// Print all the albums from a specific artist

        private static void displayAlbumsOfArtist(Connection conn) {
            String name = requestString("Artist Name? ");
            
            StringBuilder query = new StringBuilder();
            query.append("select al.alname, al.date, ar.arname");
            query.append("  from Album al, Artist ar");
            query.append("  where ar.arname = ?");
            query.append("    and ar.artistID = al.artistID");

    
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();

                out.printf("%-20s %-20s %-20s\n","Album", "Realease Date", "Artist");
                out.println("----------------------------");
                while (rs.next()) {
                    String alname = rs.getString("alname");
					int date = rs.getInt("date");
					String arname = rs.getString("arname");

                    out.printf("%-20s %-20s %-20s\n", alname, date, arname);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
        }

		//Add a playlist

		private static void addPlaylist(Connection conn) {
            String playlistID = requestString("Playlist ID? ");
            String pname = requestString("Playlist name? ");
            String creator = requestString("Creator name? ");
			String pdate = requestString("Date Created? ");
            
            
    
            StringBuilder command = new StringBuilder();
            command.append("insert into PLAYLIST(playlistID, pname, creator, pdate) values (?,?,?,?)");
    
            try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
                pstmt.setString(1, playlistID);
                pstmt.setString(2, pname);
                pstmt.setString(3, creator);
				pstmt.setString(4, pdate);
                int count = pstmt.executeUpdate();
    
                out.println(count + " Playlist(s) inserted");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

		// List Playlists names from a specific user

        private static void displayPlaylistsOfUser(Connection conn) {
            String user = requestString("User Name? ");
            
            StringBuilder query = new StringBuilder();
            query.append("select p.pname, p.creator, p.pdate");
            query.append("  from Playlist p");
            query.append("  where p.creator = ?");

    
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setString(1, user);
                ResultSet rs = pstmt.executeQuery();

                out.printf("%-20s %-20s %-20s\n","Playlist Name", "Creator", "Date Created");
                out.println("-------------------------------------------------------");
                while (rs.next()) {
                    String pname = rs.getString("pname");
					String creator = rs.getString("creator");
					String date = rs.getString("pdate");
    
                    out.printf("%-20s %-20s %-20s\n", pname, creator, date);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
        }

		// Print all songs from a specific year

		private static void displaySongsOfYear(Connection conn) {
            String year = requestString("Year? ");
            
            StringBuilder query = new StringBuilder();
            query.append("select s.sname, ar.arname, al.alname, al.date");
            query.append("  from Song s, Artist ar, Album al");
            query.append("  where al.date = ?");
            query.append("    and al.artistID = s.artistID");
			query.append("    and ar.artistID = s.artistID");
			query.append("    and s.albumID = al.albumID");

    
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setString(1, year);
                ResultSet rs = pstmt.executeQuery();

                out.printf("%-20s %-20s %-20s %-20s\n","Song", "Artist", "Album", "Release date");
                out.println("----------------------------------------------------------------------------");
                while (rs.next()) {
                    String sname = rs.getString("sname");
					String arname = rs.getString("arname");
					String alname = rs.getString("alname");
					String date = rs.getString("date");

                    out.printf("%-20s %-20s %-20s %-20s\n", sname, arname, alname, date);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
        }







}




    

