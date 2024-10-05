package labs.kjbn.locator.controller;

import labs.kjbn.locator.dto.FoodTruckResponseDto;
import labs.kjbn.locator.model.FoodTruck;
import labs.kjbn.locator.service.FoodTruckService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/foodtrucks")
public class FoodTruckRestController {

    @Autowired
    private FoodTruckService foodTruckService;

    // Get all food trucks
    @GetMapping
    public Iterable<FoodTruck> getFoodTrucks() {
        return foodTruckService.findAll();
    }

    // Get a specific food truck by ID
    @GetMapping("/{id}")
    public Optional<FoodTruck> getFoodTruckById(@PathVariable String id) {
        return foodTruckService.findById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<FoodTruckResponseDto<Object>> searchFoodTrucks(@RequestParam(value = "query", required = false) String query,
                                            @RequestParam(value = "lat", required = false) Double lat,
                                            @RequestParam(value = "lon", required = false) Double lon,
                                            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                                            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                            @RequestParam(value = "dayOrder", required = false, defaultValue = "1") Integer dayOrder,
                                            @RequestParam(value = "distance", required = false) String distance,
                                            @RequestParam(value = "unit", defaultValue = "km", required = false) String unit) {
        try {
            FoodTruckResponseDto<Object> search = foodTruckService.search(query, lat, lon, distance, unit, dayOrder, limit, offset);
            return ResponseEntity.status(HttpStatus.OK).body(search);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( FoodTruckResponseDto.builder().payload(ExceptionUtils.getStackTrace(e)).build());
        }
    }

    // Create a new food truck
    @PostMapping
    public FoodTruck createFoodTruck(@RequestBody FoodTruck foodTruck) {
        return foodTruckService.save(foodTruck);
    }

    // Delete a food truck by ID
    @DeleteMapping("/{id}")
    public void deleteFoodTruck(@PathVariable String id) {
        foodTruckService.deleteById(id);
    }


    @PutMapping("/upload-csv")
    public ResponseEntity<String> uploadCSVFile(@RequestParam("file") MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        try {
            boolean success = foodTruckService.saveCsv(file);
            return ResponseEntity.status(HttpStatus.OK).body("CSV file uploaded and data saved successfully " + success);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the file");
        }
    }
}