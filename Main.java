package src;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.apache.derby.jdbc.EmbeddedDriver;


public class Main 
{
    private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

    public static void main(String[] args) {
		try (Connection conn = getConnection("jdbc:derby:db/studentdb")) {
			//Driver d = new EmbeddedDriver();
			//Connection conn = d.connect("jdbc:derby:LibraryDB;create=true", null);
			//createTables(conn);
			//resetTables(conn);
			displayMenu();
			loop: while (true) {
				switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
				case "0": // Quit
					//conn.close();
					break loop;

				case "1": // Reset
					resetTables(conn);
					break;

				case "2": // List all books available
					listAllSongs(conn);
					break;

				case "3": // Selects a book to checkout
					selectBookToCheckout(conn);
					break;

				case "4": // Displays checked out books
					displayCheckoutList(conn);
					break;

				case "5": // Add a book to the database
					addBook(conn);
					break;

				case "6": //  List all books of a specific department
					displayBooksOfDepartment(conn);
					break;
					
				case "7": // Displays books issued by a specific student
					displayBooksIssuedByAStudent(conn);
					break;
				
				case "8": // Removes a checked out entry
					deleteCheckOutEntry(conn);
					break;

				default:
					displayMenu();
					break;
				}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		out.println("Done");
	}

    private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List all books available");
		out.println("3: Select a book to checkout");
		out.println("4: Display checked out books");
		out.println("5: Add a book to the database");
		out.println("6: List all books of a specific department");
		out.println("7: Display books issued by a specific student");
		out.println("8: Remove a checked out entry");
	}

    private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List students");
		out.println("3: Show transcript");
		out.println("4: Add student");
		out.println("5: Add enrollment");
		out.println("6: Change grade");
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
		sb.append("create table SONG(");
		sb.append("  songID int not null,");
		sb.append("  sname varchar(50) not null,");
		sb.append("  duration int not null,");
        sb.append("  albumID int not null,");
		sb.append("  primary key(songID)");
        sb.append("  foreign key (albumID) references ALBUM on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table SONG created.");
		
		sb = new StringBuilder();
		sb.append("create table ALBUM(");
		sb.append("  albumID int not null,");
		sb.append("  alname varchar(50) not null,");
		sb.append("  date int not null,");
        sb.append("  artistID int not null,");
		sb.append("  primary key(albumID)");
        sb.append("  foreign key (artistID) references ARTIST on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table ALBUM created.");

		sb = new StringBuilder();
		sb.append("create table ARTIST(");
		sb.append("  artistID int not null,");
		sb.append("  arname varchar(50) not null,");
		sb.append("  genre varchar(50) not null,");
		sb.append("  primary key(artistID)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table ARTIST created.");

		sb = new StringBuilder();
		sb.append("create table PLAYLIST(");
		sb.append("  playlistID int not null,");
		sb.append("  pname varchar(50) not null,");
		sb.append("  creator varchar(50) not null,");
		sb.append("  primary key(playlistID)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table PLAYLIST created.");
	}

    private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table SONG", "Table SONG dropped.");
		doUpdateNoError(conn, "drop table ALBUM", "Table ALBUM dropped.");
		doUpdateNoError(conn, "drop table ARTIST", "Table ARTIST dropped.");
		doUpdateNoError(conn, "drop table PLAYLIST", "Table PLAYLIST dropped.");	
	}

    private static void resetTables(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			int count = 0;
			count += stmt.executeUpdate("delete from SONG");
			count += stmt.executeUpdate("delete from ALBUM");
			count += stmt.executeUpdate("delete from ARTIST");
			count += stmt.executeUpdate("delete from PLAYLIST");
			System.out.println(count + " records deleted");

			String[] songvals = {
					"('12', 'Someone Like You', '446','5')", 
                    "('34', 'Desperado', '305','16')", 
                    "('67', 'Perfect', '423','37')",
                    "('2', 'ocean eyes', '320','7')",
                    "('46', 'Time of Our Lives', '349','63')",
                    "('46', 'Solo', '426','55')"
			};
			count = 0;
			for (String val : songvals) {
				count += stmt.executeUpdate("insert into SONG(songID, sname, duration, albumID) values " + val);
			}
			System.out.println(count + " SONG records inserted.");
			
			String[] albumVals = {
					"('5', '21', '2011','12')",
					"('16', 'ANTI', '2016','4')",
					"('37', 'Divide', '2017','25')",
                    "('7', 'dont smile at me', '2016','19')",
                    "('63', 'Globalization', '2014','48')",
                    "('55', 'HNDRXX', '2017','67')"
			};
			count = 0;
			for (String val : albumVals) {
				count += stmt.executeUpdate("insert into ALBUM(albumID, alname, date, artistID) values " + val);
			}
			System.out.println(count + " ALBUM records inserted.");
			
			String[] artistvals = {
					"('12', 'Adele', 'Pop')",
					"('4', 'Rihanna', 'Pop')",
					"('25', 'Ed Sheeran', 'Pop')",
					"('19', 'Billie Eilish', 'Alternative')",
					"('48', 'Pitbull', 'Pop')",
                    "('67', 'Future', 'Rap')"
			};
			count = 0;
			for (String val : artistvals) {
				count += stmt.executeUpdate("insert into ARTIST(artistID, arname, genre) values " + val);
			}
			System.out.println(count + " ARTIST records inserted.");

			String[] playlistvals = {
					"('3', 'Party', 'SaraG')",
                    "('42', 'Sad', 'EllaP')",
                    "('24', '2016 HITS', 'MikeT')",
                    "('13', 'Best Songs', 'JamesH')",
                    "('73', 'Rap', 'LivR')"      
			};
			count = 0;
			for (String val : playlistvals) {
				count += stmt.executeUpdate("insert into PLAYLIST(playlistID, pname, creator) values " + val);
			}
			System.out.println(count + " PLAYLIST records inserted.");

		} catch (SQLException e) {
			e.printStackTrace();
		}

        // Print all the songs from a specific artist

        private static void displaySongsOfArtist(Connection conn) {
            String arname = requestString("Artist Name? ");
            
            StringBuilder query = new StringBuilder();
            query.append("select s.sname, a.arname");
            query.append("  from Song s, Artist a");
            query.append("  where sname = a.arname");
            command.append("    and a.albumID = s.albumID");

    
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setString(1, arname);
                ResultSet rs = pstmt.executeQuery();

                out.printf("%-20s %-20s\n","Song Name", "Artist Name");
                out.println("----------------------------");
                while (rs.next()) {
                    String sname = rs.getString("sname");
                    String arname = rs.getString("arname");
    
                    out.printf("%-20s %-20s\n", sname, arname);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
        }
    

        // Add a song

        private static void addSong(Connection conn) {
            Int songID = requestInt("Song ID? ");
            String name = requestString("Song name? ");
            Int duration = requestInt("Song duration? ");
            Int albumID = requestInt("Album ID? ");
            
            
    
            StringBuilder command = new StringBuilder();
            command.append("insert into SONG(songID, name, duration, albumID) values (?,?,?,?)");
    
            try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
                pstmt.setInt(1, songID);
                pstmt.setString(2, name);
                pstmt.setInt(3, duration);
                pstmt.setInt(4, albumID);
                int count = pstmt.executeUpdate();
    
                out.println(count + " song(s) inserted");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



	}


    
}
