package it.uniroma3.cyberlab.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Ottieni il codice di errore
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            // Aggiungi informazioni al model
            model.addAttribute("status", statusCode);
            model.addAttribute("error", request.getAttribute("jakarta.servlet.error.message"));
            model.addAttribute("path", request.getAttribute("jakarta.servlet.error.request_uri"));
            
            // Redirect alle tue pagine esistenti in base al codice di errore
            switch (statusCode) {
                case 404:
                    return "error/404"; // ✅ Usa la tua pagina 404 esistente
                case 500:
                    return "error/500"; // ✅ Usa la tua pagina 500 esistente
                default:
                    return "error/500"; // Fallback alla pagina 500
            }
        }
        
        // Fallback se non c'è status code
        return "error/500";
    }
}