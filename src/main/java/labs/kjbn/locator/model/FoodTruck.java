package labs.kjbn.locator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import labs.kjbn.locator.constants.Indexes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import java.util.Date;

@Builder
@Data
@Document(indexName = Indexes.FOODTRUCKS)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodTruck {
    @Id
    private String id;  // This will be generated based on `permit` or `scheduleId`

    private int dayOrder;
    private String dayOfWeekStr;
    private String startTime;
    private String endTime;
    private String permit;
    private String permitLocation;
    private String locationDesc;
    private String optionalText;
    private String locationId;
    private String scheduleId;
    @Field(type = FieldType.Date)
    private Date createdAt;

    @Field(type = FieldType.Date)
    private Date addrDateCreate;

    @Field(type = FieldType.Date)
    private Date addrDateModified;

    private String start24;
    private String end24;
    private String cnn;
    private String block;
    private String lot;
    private String coldTruck;
    private String applicant;

    @GeoPointField
    private double[] coordinates;  // GeoPoint (latitude, longitude)

}