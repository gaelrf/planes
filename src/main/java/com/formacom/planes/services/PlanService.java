package com.formacom.planes.services;

import com.formacom.planes.models.Plan;
import com.formacom.planes.models.Usuario;
import com.formacom.planes.repositories.PlanRepository;
import com.formacom.planes.repositories.UsuarioRepository;

import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Plan> getLatestPlans() {
        return planRepository.findTop5ByOrderByFechaDesc();
    }

    public Optional<Plan> getPlanById(Long id) {
        return planRepository.findById(id);
    }

    @Transactional
    public Plan savePlan(Plan plan) {
        // Set the logged-in user as the creator of the plan and add them to the participants list
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            Usuario usuario = usuarioRepository.findByEmail(email);
            if (usuario != null) {
                plan.setCreador(usuario);
                plan.getParticipantes().add(usuario);
                entityManager.merge(usuario); // Persist the user entity if needed
            }
        }
        return planRepository.save(plan);
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Plan getPlanWithParticipants(Long id) {
        Plan plan = planRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
        Hibernate.initialize(plan.getParticipantes());
        return plan;
    }
}