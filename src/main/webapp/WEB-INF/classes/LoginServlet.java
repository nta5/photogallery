import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.security.MessageDigest;

public class LoginServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		SetUp.setUpTable();
		PrintWriter out = response.getWriter();
		out.println("<html>\n" + "<head><title>" + "Login" + "</title></head>\n" + "<body>\n"
				+ "<h1 align=\"center\">" + "Login" + "</h1>\n" + "<form action=\"login\" method=\"POST\">\n"
				+ "Username: <input type=\"text\" name=\"user_id\">\n" + "<br />\n"
				+ "Password: <input type=\"password\" name=\"password\" />\n" + "<br />\n"
				+ "<input type=\"submit\" name=\"button\" value=\"Sign in\" />\n"
				+ "<input type=\"submit\" name=\"button\" value=\"Register\" />\n"+ "</form>\n"
				+ "</form>\n" + "</body>\n</html\n");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String button = request.getParameter("button");
		response.setContentType("text/html");
		String username = request.getParameter("user_id");
		String password = request.getParameter("password");
		HttpSession session = request.getSession(true);
		session.setAttribute("USER_ID", username);
		boolean logged = false;

		final String sql = "INSERT INTO users VALUES (?, ?, ?);";

		if (Objects.equals(button, "Sign in")) {
			// System.out.println("sign in pass");
			try {
				Connection conn = SetUp.getConnection();
				Statement stmt = conn.createStatement();

				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] hash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

				ResultSet rs = stmt.executeQuery("SELECT * FROM users");
				while (rs.next()) {
					String user = rs.getString("userID");
					byte[] pass = rs.getBytes("password");
					if (user.equals(username) && Arrays.equals(pass, hash)) {
//						response.setStatus(302);
						logged = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Account doesn't exist");
			} catch (NoSuchAlgorithmException e) {
				System.out.println(e.getMessage());
			}
			if (logged) {
				response.sendRedirect("main");
			} else {
				response.sendRedirect("login");
			}
		} else if (button.equals("Register")){
			// System.out.println("register pass");
			try {
				Connection conn = SetUp.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);

				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] hash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

				stmt.setBytes(1, UuidGenerator.asBytes(UUID.randomUUID()));
				stmt.setString(2, username);
				stmt.setBytes(3, hash);
				stmt.executeUpdate();

				System.out.println("Account created");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Account exists");
			} catch (NoSuchAlgorithmException e) {
				System.out.println(e.getMessage());

			}
			response.sendRedirect("login");
		}
	}
}
