package labs.kjbn.locator.service;

import org.springframework.web.multipart.MultipartFile;

public interface FoodTruckUpload {
    boolean saveCsv(MultipartFile file);
}
