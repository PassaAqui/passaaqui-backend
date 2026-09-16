package com.passaaqui.backend.unit.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.poi.dto.CheckinRequestDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO;
import com.passaaqui.backend.modules.poi.dto.CheckinResponseDTO.AppliedRules;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.PoiVisitModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.poi.repository.PoiVisitRepository;
import com.passaaqui.backend.modules.poi.service.PoiCheckinService;
import com.passaaqui.backend.modules.poi.service.XpCalculationService;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoiCheckinServiceTest {

    @Mock
    private PoiRepository poiRepository;

    @Mock
    private PoiVisitRepository poiVisitRepository;

    @Mock
    private TouristRepository touristRepository;

    @Mock
    private XpCalculationService xpCalculationService;

    @InjectMocks
    private PoiCheckinService poiCheckinService;

    private PoiModel poi;
    private TouristModel tourist;

    @BeforeEach
    void setUp() {
        poi = new PoiModel();
        poi.setId(1);
        poi.setName("Praça Central");
        poi.setType(PoiType.TOURIST_POINT);
        poi.setXpReward(50);

        tourist = new TouristModel();
        tourist.setId(10);
        tourist.setCurrentXP(100);
    }

    @Test
    void checkin_shouldAcquirePessimisticLockOnTourist_andGrantXp() {
        var request = new CheckinRequestDTO(0.05);
        var calculationResponse = new CheckinResponseDTO(50, null, new AppliedRules(true, false), "Check-in successful");

        when(poiRepository.findById(1)).thenReturn(Optional.of(poi));
        when(touristRepository.findByIdForUpdate(10)).thenReturn(Optional.of(tourist));
        when(poiVisitRepository.countByPoiIdAndVisitedAtAfterAndUserIdNot(eq(1), any(), eq(10))).thenReturn(0L);
        when(poiVisitRepository.findFirstByPoiIdAndUserIdOrderByVisitedAtDesc(1, 10)).thenReturn(Optional.empty());
        when(xpCalculationService.calculate(eq(10), eq(1), eq("tourist"), eq(0.05), eq(0), isNull(), eq(50)))
                .thenReturn(calculationResponse);

        var response = poiCheckinService.checkin(1, 10, request);

        assertNotNull(response);
        assertEquals(50, response.xpGranted());
        assertEquals(150, tourist.getCurrentXP());
        verify(touristRepository).findByIdForUpdate(10);
        verify(touristRepository).save(tourist);
        verify(poiVisitRepository).save(any(PoiVisitModel.class));
    }

    @Test
    void checkin_shouldReturnZeroXp_whenPoiNotTouristPoint() {
        poi.setType(PoiType.STORE);
        var request = new CheckinRequestDTO(0.05);

        when(poiRepository.findById(1)).thenReturn(Optional.of(poi));

        var response = poiCheckinService.checkin(1, 10, request);

        assertNotNull(response);
        assertEquals(0, response.xpGranted());
        assertEquals("POI is not a tourist point", response.blockReason());
        verify(touristRepository, never()).findByIdForUpdate(anyInt());
        verify(touristRepository, never()).save(any());
        verify(poiVisitRepository, never()).save(any());
    }

    @Test
    void checkin_shouldThrow_whenPoiNotFound() {
        var request = new CheckinRequestDTO(0.05);
        when(poiRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> poiCheckinService.checkin(999, 10, request));
    }

    @Test
    void checkin_shouldThrow_whenTouristNotFound() {
        var request = new CheckinRequestDTO(0.05);
        when(poiRepository.findById(1)).thenReturn(Optional.of(poi));
        when(touristRepository.findByIdForUpdate(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> poiCheckinService.checkin(1, 10, request));
    }

    @Test
    void checkinResponseDTO_shouldSerializeWithEnglishJsonProperties() throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var calculation = new CheckinResponseDTO.Calculation(3.0, 7.5, 0, 100.0, 750.0, 750);
        var rules = new CheckinResponseDTO.AppliedRules(false, false);
        var dto = new CheckinResponseDTO(750, calculation, rules, null);

        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("\"xp_granted\":750"));
        assertTrue(json.contains("\"calculation\":{"));
        assertTrue(json.contains("\"distance_km\":3.0"));
        assertTrue(json.contains("\"displacement_factor\":7.5"));
        assertTrue(json.contains("\"recent_visits\":0"));
        assertTrue(json.contains("\"invisibility_factor\":100.0"));
        assertTrue(json.contains("\"raw_xp\":750.0"));
        assertTrue(json.contains("\"final_xp\":750"));
        assertTrue(json.contains("\"applied_rules\":{"));
        assertTrue(json.contains("\"anti_farming_active\":false"));
        assertTrue(json.contains("\"invalid_gps\":false"));
        assertTrue(json.contains("\"block_reason\":null"));
    }
}
