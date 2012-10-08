package de.mhus.sling.vaadin.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CoreFilter extends CoreServlet implements Filter {

	private static final long serialVersionUID = 1L;
	private ServletContext myContext;

	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(new ConfigWrapper(filterConfig,null)); 
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		service((HttpServletRequest)request, (HttpServletResponse)response);
		
	}

	public ServletContext getServletContext() {
		if (myContext == null)
			myContext = new ContextWrapper(super.getServletContext());
        return myContext;
    }

}
