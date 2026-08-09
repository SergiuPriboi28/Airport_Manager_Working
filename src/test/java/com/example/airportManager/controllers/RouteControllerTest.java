package com.example.airportManager.controllers;

import com.example.airportManager.security.CustomUserDetails;
import com.example.airportManager.security.CustomUserDetailsService;
import com.example.airportManager.security.JwtUtil;
import com.example.airportManager.service.RouteService;
import com.example.airportManager.web.controller.RouteController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
public class RouteControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RouteService routeService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    private static final String NEW_ROUTE_JSON = """
            {"originAirportId":"1", "destAirportId":"2", 
             "distanceNm": "100", "stdDurationMin": "200"
            }
            """;


    //Get APIs --------------------------------------

    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCanListRoute() throws Exception {
        mvc.perform(get("/api/routes"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotListRoute() throws Exception {
        mvc.perform(get("/api/routes"))
                .andExpect(status().isUnauthorized());
    }


    //Post APIs -------------------------------------

    @Test //expect to fail the test as specified in #1 - SP
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotCreateRoute() throws Exception {
        mvc.perform(post("/api/routes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ROUTE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void adminCanCreateRoute() throws Exception {
        when(routeService.create(any())).thenReturn(null);

        mvc.perform(post("/api/routes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_ROUTE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get("/api/routes")).andExpect(status().isUnauthorized());
    }

    //Delete APIs ----------------------------------


    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotDeleteRoute() throws Exception {
        mvc.perform(delete("/api/routes/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteRoute() throws Exception {
        mvc.perform(delete("/api/routes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_anonymous_isUnauthorized() throws Exception {
        mvc.perform(delete("/api/routes/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
