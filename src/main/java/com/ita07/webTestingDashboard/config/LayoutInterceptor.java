package com.ita07.webTestingDashboard.config;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor that adds common layout attributes to all views
 */
public class LayoutInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        if (modelAndView != null && modelAndView.hasView() && !isRedirectView(modelAndView) && !"login".equals(modelAndView.getViewName())) {
            // Only apply if it's not a redirect and not the login page itself.
            // We assume controllers for the main app pages will return "layout".
            // If a controller returns a different view name (e.g., "report" directly),
            // this interceptor might still run if not excluded by WebMvcConfig,
            // but the attributes might not be relevant for that specific view.

            String requestURI = request.getRequestURI();
            String activeTab = "dashboard";
            String determinedViewName = "dashboard";

            if (requestURI.contains("/analytics")) {
                activeTab = "analytics";
                determinedViewName = "analytics";
            } else if (requestURI.startsWith("/reports") && !requestURI.startsWith("/reports-viewer")) {
                // Handles /reports, but not /reports-viewer which might be a direct template
                activeTab = "reports";
                determinedViewName = "reports";
            } else if (requestURI.contains("/settings")) {
                activeTab = "settings";
                determinedViewName = "settings";
            } else if (requestURI.contains("/dashboard") || requestURI.equals("/")) {
                activeTab = "dashboard";
                determinedViewName = "dashboard";
            }
            // Note: /reports-viewer is intentionally not setting these here,
            // as ReportController returns "reports" (plural) template directly, not "layout".
            // If ReportController for /reports-viewer were to use the main layout, it should return "layout"
            // and set model.addAttribute("view", "report"); (singular).

            if (!modelAndView.getModelMap().containsAttribute("activeTab")) {
                modelAndView.addObject("activeTab", activeTab);
            }

            if (!modelAndView.getModelMap().containsAttribute("pageTitle")) {
                String title = activeTab.substring(0, 1).toUpperCase() + activeTab.substring(1);
                modelAndView.addObject("pageTitle", title + " - Web Testing");
            }

            if (!modelAndView.getModelMap().containsAttribute("view")) {
                // This is for layout.html to pick up the correct content fragment.
                // This should only be added if the controller is indeed returning "layout".
                if ("layout".equals(modelAndView.getViewName())) {
                    modelAndView.addObject("view", determinedViewName);
                }
            }
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        String viewName = modelAndView.getViewName();
        return viewName != null && viewName.startsWith("redirect:");
    }
}
