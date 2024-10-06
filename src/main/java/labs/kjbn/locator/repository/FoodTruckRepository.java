package labs.kjbn.locator.repository;

import labs.kjbn.locator.model.FoodTruck;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FoodTruckRepository extends ElasticsearchRepository<FoodTruck, String> {
    // Custom query methods
//    List<FoodTruck> findByType(String type);

}