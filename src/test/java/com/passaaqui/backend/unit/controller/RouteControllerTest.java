package com.passaaqui.backend.unit.controller;

import com.passaaqui.backend.modules.route.controller.RouteController;
import com.passaaqui.backend.modules.route.dto.LocationDTO;
import com.passaaqui.backend.modules.route.dto.RouteSessionDTO;
import com.passaaqui.backend.modules.route.dto.StartRouteDTO;
import com.passaaqui.backend.modules.route.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteController controller;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(new org.springframework.security.core.context.SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("1", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TOURIST")))
        ));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void start_shouldReturn200WithNewSession() throws Exception {
        var session = new RouteSessionDTO("ACTIVE", null, null);
        when(routeService.start(eq("1"), any(StartRouteDTO.class))).thenReturn(session);

        mockMvc.perform(post("/api/route/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": -23.5505, \"longitude\": -46.6333}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void start_shouldReturn200WithNoBody() throws Exception {
        var session = new RouteSessionDTO("ACTIVE", null, null);
        when(routeService.start(eq("1"), any(StartRouteDTO.class))).thenReturn(session);

        mockMvc.perform(post("/api/route/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void current_shouldReturn200WithSession() throws Exception {
        var session = new RouteSessionDTO("ACTIVE", null, null);
        when(routeService.getCurrentSession("1")).thenReturn(session);

        mockMvc.perform(get("/api/route/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateLocation_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/route/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": -23.5505, \"longitude\": -46.6333}"))
                .andExpect(status().isOk());

        verify(routeService).updateLocation(eq("1"), any(LocationDTO.class));
    }

    @Test
    void deleteCurrent_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/route/current"))
                .andExpect(status().isNoContent());

        verify(routeService).stop("1");
    }
}
