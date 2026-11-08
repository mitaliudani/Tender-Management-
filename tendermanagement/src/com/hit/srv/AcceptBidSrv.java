package com.hit.srv;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hit.beans.VendorBean;
import com.hit.dao.BidderDao;
import com.hit.dao.BidderDaoImpl;
import com.hit.security.HtmlEscaper;

/**
 * Servlet implementation class AcceptBidSrv
 */
@WebServlet("/AcceptBidSrv")
public class AcceptBidSrv extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * Creates the legacy bid-acceptance servlet without initializing external resources.
     * @see HttpServlet#HttpServlet()
     */
    public AcceptBidSrv() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * Verifies the existing admin session, accepts the identified bid, and renders
	 * an HTML-escaped status. The security filter rejects GET before this shared
	 * legacy handler can run; only CSRF-validated POST requests may reach it.
	 *
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false);
		String user = session == null ? null : (String)session.getAttribute("user");
		String uname = session == null ? null : (String)session.getAttribute("username");
		
		if(!"admin".equalsIgnoreCase(user) || uname == null || uname.trim().isEmpty()){
			
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
			
		}
		
		
		
		
		String bidderId = request.getParameter("bid");
		String tenderId = request.getParameter("tid");
		String vendorId = request.getParameter("vid");
		
		BidderDao dao = new BidderDaoImpl();
		
		String status = dao.acceptBid(bidderId, tenderId, vendorId);
		
		PrintWriter pw = response.getWriter();
		
		RequestDispatcher rd = request.getRequestDispatcher("viewTenderBidsForm.jsp");
		
		rd.include(request, response);
		
		pw.print("<p role=\"status\">" + HtmlEscaper.escape(status) + "</p>");
		
		pw.close();
	}

	/**
	 * Handles authorized bid-acceptance POSTs using the shared legacy implementation.
	 *
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
