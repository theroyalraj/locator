package labs.kjbn.locator.repository;

import labs.kjbn.locator.model.FoodTruck;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodTruckRepository extends ElasticsearchRepository<FoodTruck, String> {
    // Custom query methods
//    List<FoodTruck> findByType(String type);

}