package labs.kjbn.locator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodTruckResponseDto<T> {
    private int size;
    private int offset;
    private long total;
    private T payload;
}
