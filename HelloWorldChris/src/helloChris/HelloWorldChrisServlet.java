package helloChris;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.sap.security.auth.login.LoginContextFactory;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

/**
 * Chris' HelloWorld Servlet printing out info's using SAP HANA Cloud APIs
 */
public class HelloWorldChrisServlet extends HttpServlet {
	private static final String USER_PROVIDER_NAME = "java:comp/env/user/Provider";
	private static final String DATASOURCE_NAME = "java:comp/env/jdbc/DefaultDB";
	private static final DateFormat dfmt = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss z");
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		log("entering HelloWorldChrisServlet.doGet()");
		handleRequest(request, response);
		log("leaving HelloWorldChrisServlet.doGet()");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		log("entering HelloWorldChrisServlet.doPost()");
		handleRequest(request, response);
		log("leaving HelloWorldChrisServlet.doPost()");
	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) {
		printRequest(request, response);
		String authType = request.getParameter("auth");
		if (request.getRemoteUser() == null && authType != null)
			try {
				response.getWriter().println(
						"\nWill trigger authentication of type " + authType
								+ "\n");
				log("Request was unauthenticated and auth parameter was set to "
						+ authType + ". Trigger authentication.");
				try {
					LoginContextFactory.createLoginContext(authType).login();
					log("Login was successfull. Print the (potentially) modified request again.");
					printRequest(request, response);
				} catch (LoginException loginexc) {
					log("Received a LoginException:", loginexc);
				}
			} catch (IOException ioexc) {
				log("catched an IOException: ", ioexc);
			}

	}

	private void printRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, NamingException {
		PrintWriter writer = null;

		writer = response.getWriter();

		// describe the request
		writer.println();
		writer.println("  Information about the request (request.get...()):");
		writer.println("    request.getAuthType(): " + request.getAuthType());
		writer.println("    request.getCharacterEncoding(): "
				+ request.getCharacterEncoding());
		writer.println("    request.getContentLength(): "
				+ request.getContentLength());
		writer.println("    request.getContentType(): "
				+ request.getContentType());
		writer.println("    request.getContextPath(): "
				+ request.getContextPath());
		writer.println("    request.getLocalAddr(): " + request.getLocalAddr());
		writer.println("    request.getLocalName(): " + request.getLocalName());
		writer.println("    request.getLocalPort(): " + request.getLocalPort());
		writer.println("    request.getMethod(): " + request.getMethod());
		writer.println("    request.getPathInfo(): " + request.getPathInfo());
		writer.println("    request.getPathTranslated(): "
				+ request.getPathTranslated());
		writer.println("    request.getProtocol(): " + request.getProtocol());
		writer.println("    request.getRemoteAddr(): "
				+ request.getRemoteAddr());
		writer.println("    request.getRemoteHost(): "
				+ request.getRemoteHost());
		writer.println("    request.getRemotePort(): "
				+ request.getRemotePort());
		writer.println("    request.getRemoteUser(): "
				+ request.getRemoteUser());
		writer.println("    request.getRequestedSessionId(): "
				+ request.getRequestedSessionId());
		writer.println("    request.getRequestURI(): "
				+ request.getRequestURI());
		writer.println("    request.getRequestURL(): "
				+ request.getRequestURL());
		writer.println("    request.getScheme(): " + request.getScheme());
		writer.println("    request.getServerName(): "
				+ request.getServerName());
		writer.println("    request.getServerPort(): "
				+ request.getServerPort());
		writer.println("    request.getServletPath(): "
				+ request.getServletPath());
		writer.println("    request.getUserPrincipal(): "
				+ request.getUserPrincipal());
		writer.println();
		writer.println("  request attributes:");
		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attName = attributeNames.nextElement();
			writer.println("    " + attName + ": "
					+ request.getAttribute(attName));
		}
		writer.println();
		writer.println("  request headers (request.getHeader(...)):");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			writer.println("    " + headerName + ": "
					+ request.getHeader(headerName));
		}
		writer.println();
		writer.println("  request parameters (request.getParameter(...)):");
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			writer.println("    " + paramName + ": "
					+ request.getParameter(paramName));
		}

		// describe the session
		writer.println();
		writer.println("  Session attributes (request.getSession().getAttribute(...)):");
		HttpSession session = request.getSession();
		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			writer.println("    " + name + ": " + session.getAttribute(name));
		}

		// describe additional information about the user principal
		writer.println();
		writer.println("  Information from the userProvider for the current user principal:");
		Principal principal = request.getUserPrincipal();
		InitialContext ctx = new InitialContext();
		
		if (principal != null) {
			
			UserProvider userProvider = (UserProvider) ctx
					.lookup(USER_PROVIDER_NAME);
			writer.println("    UserProvider: ctx.lookup(" + USER_PROVIDER_NAME
					+ "):" + userProvider);
			User user = userProvider.getUser(principal.getName());
			if (user != null) {
				writer.println("    ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()).getName(): "
						+ user.getName());
				writer.println("    Attributes of user \"" + user.getName()
						+ "\":");
				for (String attr : user.listAttributes())
					writer.println("      " + attr + ": "
							+ user.getAttribute(attr));
			} else
				writer.println("    ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()): (null)");
		}

		// describe the datasource
		writer.println();
		writer.println("  Information about the default data source (ctx.lookup(\""
				+ DATASOURCE_NAME + "\"))");
		DataSource ds = (DataSource) ctx.lookup(DATASOURCE_NAME);

		if (ds == null)
			writer.println("    Couldn't lookup a DataSource under \""
					+ DATASOURCE_NAME + "\"");
		else {
			writer.println("    DataSource: " + ds.toString());
			Connection conn = ds.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			writer.println("    Connection metadata (dataSource.getConnection().getMetaData():");
			writer.println("      Database ProductName, Version: "
					+ metaData.getDatabaseProductName() + ", "
					+ metaData.getDatabaseProductVersion());
			ResultSet rs = metaData.getTables(null, null, "%",
					new String[] { "TABLE" });
			if (rs.next())
				do {
					writer.println("      Found Table (Catalog, Schema, Name): ("
							+ rs.getString("TABLE_CAT")
							+ ", "
							+ rs.getString("TABLE_SCHEM")
							+ ", "
							+ rs.getString("TABLE_NAME") + ")");
				} while (rs.next());
			else
				writer.println("      No tables found in database");
		}

		// describe the System environment
		writer.println();
		writer.println("  System environment (System.getEnv(...)):");
		for (String key : System.getenv().keySet())
			writer.println("    " + key + ": " + System.getenv(key));
	}
}
