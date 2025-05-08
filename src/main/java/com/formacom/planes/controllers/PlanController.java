package com.formacom.planes.controllers;

import com.formacom.planes.models.Plan;
import com.formacom.planes.models.Usuario;
import com.formacom.planes.services.PlanService;
import com.formacom.planes.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String showLandingPage(Model model) {
        List<Plan> latestPlans = planService.getLatestPlans();
        model.addAttribute("latestPlans", latestPlans);
        return "landing";
    }

    @GetMapping("/plans/{id}")
    public String showPlanDetails(@PathVariable Long id, Model model) {
        Plan plan = planService.getPlanWithParticipants(id);
        model.addAttribute("plan", plan);
        boolean canJoin = plan.getParticipantes().size() < plan.getCapacidadMaxima() &&
                plan.getParticipantes().stream().noneMatch(u -> u.getEmail().equals(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
        model.addAttribute("canJoin", canJoin);
        return "plan-details";
    }

    @GetMapping("/plans")
    public String showAllPlans(Model model) {
        List<Plan> allPlans = planService.getAllPlans();
        model.addAttribute("allPlans", allPlans);
        return "all-plans";
    }

    @GetMapping("/plans/new")
    public String showCreatePlanForm(Model model) {
        model.addAttribute("plan", new Plan());
        return "plan-create";
    }

    @PostMapping("/plans")
    public String createPlan(@ModelAttribute Plan plan) {
        planService.savePlan(plan);
        return "redirect:/plans";
    }

    @PostMapping("/plans/{id}/join")
    public String joinPlan(@PathVariable Long id) {
        Plan managedPlan = planService.getPlanWithParticipants(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            Usuario managedUsuario = usuarioService.findByEmail(email);
            if (managedUsuario != null && managedPlan.getParticipantes().size() <= managedPlan.getCapacidadMaxima()) {
                planService.savePlan(managedPlan);
            }
        }
        return "redirect:/plans/" + id;
    }

    @ModelAttribute
    public void addCsrfToken(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
    }
}