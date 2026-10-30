package com.example.airportManager.controllers;

import com.example.airportManager.security.CustomUserDetailsService;
import com.example.airportManager.security.JwtUtil;
import com.example.airportManager.service.AirportService;
import com.example.airportManager.web.controller.AirportController;
import lombok.With;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(AirportController.class)
public class AirportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AirportService airportService;

    @MockBean
    JwtUtil jwtUtil;


    @MockBean
    CustomUserDetailsService customUserDetailsService;

    private static final String NEW_AIRPORT_JSON = """
            {"iata":"OTP","icao":"LROP","name":"Otopeni","city":"Bucharest",
             "country":"Romania","timezone":"Europe/Bucharest"}
            """;


    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCanListAirports() throws Exception {
        mvc.perform(get("/api/airports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListAirports() throws Exception {
        mvc.perform(get("/api/airports"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotListAirports() throws Exception {
        mvc.perform(get("/api/airports"))
                .andExpect(status().isUnauthorized());
    }



    //Post APIs ----------------------------------------------------
    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotCreateAirport() throws Exception {
        mvc.perform(post("/api/airports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_AIRPORT_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateAirport() throws Exception {
        when(airportService.create(any())).thenReturn(null);

        // Act + Assert
        mvc.perform(post("/api/airports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_AIRPORT_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCannotCreateAirport() throws Exception {
        mvc.perform(post("/api/airports")).andExpect(status().isForbidden());
    }

    //UPDATE APIs --------------------------------------------

    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotUpdateAirport() throws Exception {
        mvc.perform(put("/api/airports/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_AIRPORT_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateAirport() throws Exception {
        when(airportService.update(anyLong(),any())).thenReturn(null);

        // Act + Assert
        mvc.perform(put("/api/airports/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_AIRPORT_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotUpdateAirport() throws Exception {
        mvc.perform(put("/api/airports/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_AIRPORT_JSON))
                .andExpect(status().isUnauthorized());
    }

}