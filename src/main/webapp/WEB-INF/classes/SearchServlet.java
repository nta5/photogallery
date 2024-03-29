import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Base64;
import java.util.Objects;

public class SearchServlet extends HttpServlet {
	private Connection con = SetUp.getConnection();
	private static String[] CAPTION_FILENAME;
	private static String[] DATA_FILENAME;
	private static String[] MY_FILENAME;
	private final Blob[] MY_BLOB = new Blob[1024];
	private static int CAPTION_LOOP = -1;
	private static int DATE_LOOP = -1;
	private static int MY_LOOP = -1;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isLoggedIn(request)) { 
			response.setStatus(302);
			response.sendRedirect("login");
		}
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession(false);
        boolean isLoggedIn = isLoggedIn(request);
		if (!isLoggedIn) {
            response.setStatus(302);
            response.sendRedirect("login");
        } else {
			String userName = "Logged in as: " + session.getAttribute("USER_ID");
			String html = "<!DOCTYPE html>" +
					"<html>" +
					"<body>" +
					"<div style=\"text-align: right;\">" + userName + "</div>" +
					"<h2> Search Filter </h2> " +
					"<form action='search' method = 'post' id = 'searchForm'>" +
					"<label for='caption'>Caption: </label>" +
					"<input type='text' id = 'caption' name = 'caption'>" +
					"<label for='date'>Date: </label>" +
					"<input type='date' placeholder='yyyy-mm-dd' id = 'date' name = 'date'>" +
					"<button type='submit' form='searchForm' value='Submit'>Search</button>" +
					"</form>" +
					"<div>" +
					"<form action='main' method='get'>" +
					"<button class='button' id='main'>Main</button>" +
					"</div>" +
					"</body>" +
					"</html>";
			PrintWriter out = response.getWriter();
			out.println(html);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("?????doPost Called???????????????");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		if (!(isLoggedIn(request))) {
			response.sendRedirect("login");
		} else {

			HttpSession session = request.getSession(false);
			String userName = "Logged in as: " + session.getAttribute("USER_ID");

			PrintWriter out = response.getWriter();
			String caption = request.getParameter("caption");
			String date = request.getParameter("date");
			out.println("<div style=\"text-align: right;\">\n");
			out.println(userName);
			out.println("\n</div>");
			out.println("<div>Search caption: " + caption);
			out.println("</div><div>Search date: " + date + "<div/>");
			findCaption(caption);
			findDate(date);
			CheckDateCaption(date, caption);

			if ((Objects.equals(caption, "")) & (Objects.equals(date, ""))) {
				out.println("<div>Fill in at least one. (Caption or data)</div>");
			}
			else if ((!Objects.equals(caption, "")) & (Objects.equals(date, ""))) {
				if ((CAPTION_FILENAME != null) & (CAPTION_LOOP >= 0)) {
					for (int i = 0; i <= CAPTION_LOOP; i++) {
						try {
							byte[] imageBytes = MY_BLOB[i].getBytes(1, (int)MY_BLOB[i].length());
							out.println("<img id = \"img_src\" src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes) + "\" alt=" + CAPTION_FILENAME[i] + " width=400 height=350>");
						} catch (SQLException e) {
							System.out.println("Search/Post: " + e.getMessage());
						}
					}

				} else {
					out.println("<div>No such photo was found! Please enter correct caption.</div>");
				}
			}
			else if (Objects.equals(caption, "")) {
				if ((DATA_FILENAME != null) & (DATE_LOOP >= 0)) {
					for (int j = 0; j <= DATE_LOOP; j++) {
						try {
							byte[] imageBytes = MY_BLOB[j].getBytes(1, (int)MY_BLOB[j].length());
							out.println("<img id = \"img_src\" src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes) + "\" alt=" + DATA_FILENAME[j] + " width=400 height=350>");
						} catch (SQLException e) {
							System.out.println("Search/Post: " + e.getMessage());
						}
					}
				} else {
					out.println("<div>No such photo was found! Please enter correct date.</div>");
				}
			}
			else {
				if (MY_LOOP >= 0) {
					for (int k = 0; k <= MY_LOOP; k++) {
						try {
							byte[] imageBytes = MY_BLOB[k].getBytes(1, (int)MY_BLOB[k].length());
							out.println("<img id = \"img_src\" src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes) + "\" alt=" + MY_FILENAME[k] + " width=400 height=350>");
						} catch (SQLException e) {
							System.out.println("Search/Post: " + e.getMessage());
						}
					}
				} else {
					out.println("<div>Caption and Data not match in one photo.</div>");
				}
			}
			out.println("<br/><div>");
			out.println("<form action='main' method='get'>");
			out.println("<button class='button' id='main'>Main</button>");
			out.println("</div>");
		}
	}

	public void CheckDateCaption(String date, String caption) {
		String[] myArray = new String[1024];
		MY_LOOP = -1;
		try {
			con = SetUp.getConnection();
			PreparedStatement s = con.prepareStatement("SELECT fileName, picture FROM Photos WHERE dateTaken = '" + date + "' && caption = '" + caption + "';");
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				MY_LOOP++;
				myArray[MY_LOOP] = rs.getString(1);
				MY_BLOB[MY_LOOP] = rs.getBlob(2);
			}
			MY_FILENAME = myArray;

		} catch (Exception e) {
			System.out.println("Search/findCaptionAndDate: " + e.getMessage());
		}
	}

	private void findDate(String date) {
		String[] dateArray = new String[1024];
		DATE_LOOP = -1;
//		String filename = null;
//		System.out.println(date);
		try {
			con = SetUp.getConnection();
			PreparedStatement s = con.prepareStatement("SELECT fileName, picture FROM Photos WHERE dateTaken = '" + date + "';");
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				DATE_LOOP++;
				dateArray[DATE_LOOP] = rs.getString(1);
				MY_BLOB[DATE_LOOP] = rs.getBlob(2);
			}
			DATA_FILENAME = dateArray;

		} catch (Exception e) {
			System.out.println("Search/findDate: " + e.getMessage());
		}

	}

	private void findCaption(String caption) {
		String[] captionArray = new	String[1024];
		CAPTION_LOOP = -1;
		try {
			con = SetUp.getConnection();
			PreparedStatement s = con.prepareStatement("SELECT fileName, picture FROM Photos WHERE caption = '" + caption + "';");
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				CAPTION_LOOP ++;
				captionArray[CAPTION_LOOP] = rs.getString(1);
				MY_BLOB[CAPTION_LOOP] = rs.getBlob(2);
			}
			CAPTION_FILENAME = captionArray;
		} catch (Exception e) {
			System.out.println("Search/findCaption: " + e.getMessage());
		}
	}

	private boolean isLoggedIn(HttpServletRequest req) {
		HttpSession session = req.getSession(false);

		return session != null && req.isRequestedSessionIdValid();
	}
}
