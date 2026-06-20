package br.com.scheiner.aws.console.web.navigation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
public class RedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectServlet.class);

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) {

        try {
            response.sendRedirect(request.getContextPath() + ApplicationRoute.DASHBOARD.getPath());
        } catch (IOException exception) {
            LOGGER.error("Erro redirecting to index.xhtml", exception);
        }
    }
}
