package hu.sajatnev.projekt.controller;

import hu.sajatnev.projekt.model.Vevo;
import hu.sajatnev.projekt.repository.VevoRepository;
import hu.sajatnev.projekt.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private VevoRepository vevoRepository;

    // 1. BEJELENTKEZÉSI FELÜLET (GET)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // A src/main/resources/templates/login.html fájlt fogja keresni
    }

    // 2. BEJELENTKEZÉS FELDOLGOZÁSA (POST)
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session, 
                              Model model) {
        Optional<Vevo> oVevo = authService.login(email, password);
        
        if (oVevo.isPresent()) {
            // Ha sikeres, elmentjük a bejelentkezett felhasználót a HTTP Session-be
            session.setAttribute("loggedInUser", oVevo.get());
            return "redirect:/vevok"; // Átirányítás a védett vevőlistára
        } else {
            model.addAttribute("error", "Hibás email cím vagy jelszó!");
            return "login"; // Hiba esetén újra megjelenítjük a login oldalt a hibaüzenettel
        }
    }

    // 3. REGISZTRÁCIÓS FELÜLET (GET)
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("vevo", new Vevo()); // Üres objektumot adunk át az űrlapnak
        return "register"; // A register.html fájlt fogja keresni
    }

    // 4. REGISZTRÁCIÓ FELDOLGOZÁSA (POST)
    @PostMapping("/register")
    public String handleRegister(@ModelAttribute Vevo vevo, 
                                 @RequestParam String password, 
                                 Model model) {
        String result = authService.registerVevo(vevo, password);
        
        if ("SUCCESS".equals(result)) {
            model.addAttribute("success", "Sikeres regisztráció! Most már bejelentkezhetsz.");
            return "login"; // Átdobjuk a bejelentkező oldalra
        } else {
            model.addAttribute("error", result); // A service-ből visszakapott hibaüzenet (pl. rossz email)
            return "register"; // Hiba esetén marad a regisztrációs oldalon
        }
    }

    // 5. VÉDETT VEVŐLISTA FELÜLET (GET)
    @GetMapping("/vevok")
    public String listVevok(HttpSession session, Model model) {
        // Ellenőrizzük, hogy be van-e lépve a felhasználó (létezik-e a session)
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login"; // Ha nincs belépve, visszadobjuk a login oldalra
        }
        
        // Ha be van lépve, lekérjük az ÖSSZES vevőt az Oracle adatbázisból és átadjuk a View-nak
        model.addAttribute("vevokLista", vevoRepository.findAll());
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "vevok"; // A vevok.html fájlt fogja keresni
    }

    // 6. KIJELENTKEZÉS (GET)
    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Munkamenet törlése
        return "redirect:/login";
    }
}

