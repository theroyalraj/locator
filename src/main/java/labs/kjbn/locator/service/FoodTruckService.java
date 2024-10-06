package labs.kjbn.locator.service;

import labs.kjbn.locator.dto.FoodTruckResponseDto;
import labs.kjbn.locator.model.FoodTruck;

import java.io.IOException;
import java.util.Optional;

public interface FoodTruckService {

    Iterable<FoodTruck> findAll();

    Optional<FoodTruck> findById(String id);

    FoodTruck save(FoodTruck foodTruck);

    void deleteById(String id);

    FoodTruckResponseDto<Object> search(String query, Double lat, Double lon, String distance, String unit, int dayOrder, int limit, int offset) throws IOException;
}
