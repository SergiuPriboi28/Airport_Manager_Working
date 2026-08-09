package com.example.airportManager.controllers;

import com.example.airportManager.security.CustomUserDetailsService;
import com.example.airportManager.security.JwtUtil;
import com.example.airportManager.service.PassengerService;
import com.example.airportManager.web.controller.PassengerController;
import io.jsonwebtoken.Jwt;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PassengerController.class)
public class PassengerControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PassengerService passengerService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    private static final String NEW_PASSENGER_JSON = """
            {
            "firstName" : "Test",
            "lastName" : "Jhon" ,
            "email" : "test@example.com",
            "docType" : "Passport" ,
            "docNumber" : "123456",
            "nationality" : "romanian"
            }
            """;

    //CREATE APIs --------------------------------------------

    @Test //expected to fail due to #1
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotCreatePassenger() throws Exception {
        mvc.perform(post("/api/passengers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_PASSENGER_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreatePassenger() throws Exception {
        when(passengerService.create(any())).thenReturn(null);

        mvc.perform(post("/api/passengers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_PASSENGER_JSON)
        ).andExpect(status().isCreated());
    }

    //GET APIs --------------------------------------------------

    @Test //expected to fail again because of #1?
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotGetPassenger() throws Exception {
        mvc.perform(get("/api/passengers/1"))
                .andExpect(status().isForbidden());
    }

    @Test //expected to fail again because of #1?
    @WithMockUser(roles = "ADMIN")
    void adminCanGetPassenger() throws Exception {
        mvc.perform(get("/api/passengers/1"))
                .andExpect(status().isOk());
    }

}
