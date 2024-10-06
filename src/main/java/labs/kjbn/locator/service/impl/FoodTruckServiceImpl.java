package labs.kjbn.locator.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import labs.kjbn.locator.constants.Indexes;
import labs.kjbn.locator.dto.FoodTruckResponseDto;
import labs.kjbn.locator.model.FoodTruck;
import labs.kjbn.locator.repository.FoodTruckRepository;
import labs.kjbn.locator.service.FoodTruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class FoodTruckServiceImpl implements FoodTruckService {

    @Autowired
    private FoodTruckRepository foodTruckRepository;
    @Autowired
    private ElasticsearchClient client;

    @Override
    public Iterable<FoodTruck> findAll() {
        return foodTruckRepository.findAll();
    }

    @Override
    public Optional<FoodTruck> findById(String id) {
        return foodTruckRepository.findById(id);
    }

    @Override
    public FoodTruck save(FoodTruck foodTruck) {
        return foodTruckRepository.save(foodTruck);
    }

    @Override
    public void deleteById(String id) {
        foodTruckRepository.deleteById(id);
    }

    @Override
    public FoodTruckResponseDto<Object> search(String query, Double lat, Double lon, String distance, String unit, int dayOrder, int limit, int offset) throws IOException {
        // Multi-match query for text search
        // Text query for multi-field search
        Query textQuery;
        if (query != null && !query.isBlank()) {
            textQuery = Query.of(q -> q
                    .multiMatch(m -> m
                            .query(query)
                            .fields("locationDesc", "name", "permit", "description", "applicant", "optionalText")
                    )
            );
        } else {
            textQuery = null;
        }

        Query geoQuery;
        if (distance != null && !distance.isBlank()) {
            geoQuery = Query.of(q -> q
                    .geoDistance(g -> g
                            .field("coordinates")
                            .distance(distance + unit)
                            .location(l -> l.latlon(new LatLonGeoLocation.Builder().lat(lat).lon(lon).build()))
                    )
            );
        } else {
            geoQuery = null;
        }

        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (textQuery != null) {
                b.must(textQuery); // Add text query as must
            }
            if (geoQuery != null) {
                b.filter(geoQuery); // Add geo-distance query as filter
            }
            b.must(Query.of(q -> q.term(t -> t.field("dayOrder").value(dayOrder))));
            return b;
        });

        SortOptions sortByDistance = SortOptions.of(s -> s
                .geoDistance(g -> g
                        .field("coordinates")
                        .location(l -> l.latlon(new LatLonGeoLocation.Builder().lat(lat).lon(lon).build()))
                        .order(SortOrder.Asc)  // Sort by nearest distance (ascending)
                )
        );

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Indexes.FOODTRUCKS)
                .query(q -> q.bool(boolQuery))
                .size(limit)
                .from(offset)
                .sort(sortByDistance)  // Add sorting to the request
                .build();

        SearchResponse<FoodTruck> response = client.search(searchRequest, FoodTruck.class);

        // Process results
        List<FoodTruck> foodTrucks = new ArrayList<>();
        for (Hit<FoodTruck> hit : response.hits().hits()) {
            foodTrucks.add(hit.source());
        }
        long totalHits = response.hits().total().value();  // Total number of hits in the result set
        int currentHitsSize = response.hits().hits().size();
        return FoodTruckResponseDto.builder().offset(offset).size(currentHitsSize).payload(foodTrucks).total(totalHits).build();

    }
}
