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

import com.hit.dao.NoticeDao;
import com.hit.dao.NoticeDaoImpl;
import com.hit.security.HtmlEscaper;

/**
 * Servlet implementation class RemoveNoticeSrv
 */
@WebServlet("/RemoveNoticeSrv")
public class RemoveNoticeSrv extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveNoticeSrv() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
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
		
		
		
		int noticeId = Integer.parseInt(request.getParameter("noticeid"));
		
		NoticeDao dao = new NoticeDaoImpl();
		
		String status = dao.removeNotice(noticeId);
		
		PrintWriter pw = response.getWriter();
		
		RequestDispatcher rd = request.getRequestDispatcher("removeNotice.jsp");
		
		rd.include(request, response);
		
		pw.print("<p role=\"status\">" + HtmlEscaper.escape(status) + "</p>");
		
		pw.close();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
