package com.passaaqui.backend.security;

import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.tourist.service.TouristService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TouristService touristService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private PoiRepository poiRepository;

    @BeforeEach
    void setUp() {
        touristService.createUser("test@test.com", "Test", passwordEncoder.encode("pass"), "52998224725");

        var city = new CityModel();
        city.setName("Test City");
        city.setDescription("Test Description");
        cityRepository.save(city);

        var poi = new PoiModel();
        poi.setName("Test POI");
        poi.setDescription("Test Description");
        poi.setType(PoiType.TOURIST_POINT);
        poi.setCity(city);
        poiRepository.save(poi);
    }

    @Test
    void authPublicEndpoints_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN_USER")
    void protectedEndpoint_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isOk());
    }

    @Test
    void touristEndpoint_shouldAllowTouristRole() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "1", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_TOURIST"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(post("/api/pois/{poiId}/ratings", 1)
                        .contentType("application/json")
                        .content("{\"rating\":4}"))
                .andExpect(status().isOk());
    }
}
