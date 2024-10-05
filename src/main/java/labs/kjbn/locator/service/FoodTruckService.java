package labs.kjbn.locator.service;

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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class FoodTruckService {

    @Autowired
    private FoodTruckRepository foodTruckRepository;
    @Autowired
    private ElasticsearchClient client;

    public Iterable<FoodTruck> findAll() {
        return foodTruckRepository.findAll();
    }

    public Optional<FoodTruck> findById(String id) {
        return foodTruckRepository.findById(id);
    }

    public FoodTruck save(FoodTruck foodTruck) {
        return foodTruckRepository.save(foodTruck);
    }

    public void deleteById(String id) {
        foodTruckRepository.deleteById(id);
    }

    public boolean saveCsv(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<FoodTruck> foodTrucks = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                double[] cordinates = new double[]{
                        Double.parseDouble(csvRecord.get("Longitude")),
                        Double.parseDouble(csvRecord.get("Latitude"))
                };
                FoodTruck foodTruck = FoodTruck.builder()
                        .dayOrder(Integer.parseInt(csvRecord.get("DayOrder")))
                        .dayOfWeekStr(csvRecord.get("DayOfWeekStr"))
                        .startTime(csvRecord.get("starttime"))
                        .endTime(csvRecord.get("endtime"))
                        .permit(csvRecord.get("permit"))
                        .permitLocation(csvRecord.get("PermitLocation"))
                        .locationDesc(csvRecord.get("locationdesc"))
                        .optionalText(csvRecord.get("optionaltext"))
                        .locationId(csvRecord.get("locationid"))
                        .scheduleId(csvRecord.get("scheduleid"))
                        .addrDateCreate(parseDate(csvRecord.get("Addr_Date_Create")))
                        .addrDateModified(parseDate(csvRecord.get("Addr_Date_Modified")))
                        .start24(csvRecord.get("start24"))
                        .end24(csvRecord.get("end24"))
                        .cnn(csvRecord.get("CNN"))
                        .block(csvRecord.get("block"))
                        .lot(csvRecord.get("lot"))
                        .coldTruck(csvRecord.get("ColdTruck"))
                        .applicant(csvRecord.get("Applicant"))
                        .coordinates(cordinates)
                        .createdAt(new Date())  // Assuming you want the current timestamp
                        .build();
                foodTruck.setId(foodTruck.getPermit() + "_" + foodTruck.getDayOrder());
//                foodTruckRepository.save(foodTruck);
                foodTrucks.add(foodTruck);
            }

            // Save all food trucks to Elasticsearch
            foodTruckRepository.saveAll(foodTrucks);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

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
                            .distance(distance+unit)
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
