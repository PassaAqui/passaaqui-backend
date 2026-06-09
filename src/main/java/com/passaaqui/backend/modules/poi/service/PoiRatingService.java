package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.poi.dto.CreatePoiRatingDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.PoiRatingModel;
import com.passaaqui.backend.modules.poi.repository.PoiRatingRepository;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoiRatingService {

    private final PoiRatingRepository ratingRepository;
    private final PoiRepository poiRepository;
    private final TouristRepository touristRepository;

    @Transactional
    public PoiRatingModel rate(Integer poiId, Integer userId, CreatePoiRatingDTO dto) {
        PoiModel poi = poiRepository.findById(poiId)
                .orElseThrow(() -> new ResourceNotFoundException("POI not found"));

        UserModel user = touristRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        PoiRatingModel rating = ratingRepository.findByPoiIdAndUserId(poiId, userId)
                .orElseGet(PoiRatingModel::new);

        rating.setPoi(poi);
        rating.setUser(user);
        rating.setRating(dto.rating());

        ratingRepository.save(rating);

        updatePoiAverageRating(poi);

        return rating;
    }

    private void updatePoiAverageRating(PoiModel poi) {
        List<PoiRatingModel> ratings = ratingRepository.findByPoiId(poi.getId());
        double avg = ratings.stream()
                .mapToInt(PoiRatingModel::getRating)
                .average()
                .orElse(0.0);
        poi.setAverageRating(avg);
        poi.setRatingsCount(ratings.size());
        poiRepository.save(poi);
    }

    public List<PoiRatingModel> getRatingsByPoiId(Integer poiId) {
        if (!poiRepository.existsById(poiId)) {
            throw new ResourceNotFoundException("POI not found");
        }
        return ratingRepository.findByPoiId(poiId);
    }
}
