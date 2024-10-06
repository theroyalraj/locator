package labs.kjbn.locator.service.impl;

import labs.kjbn.locator.model.FoodTruck;
import labs.kjbn.locator.repository.FoodTruckRepository;
import labs.kjbn.locator.service.FoodTruckUpload;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FoodTruckUploadImpl implements FoodTruckUpload {
    @Autowired
    private FoodTruckRepository foodTruckRepository;

    @Override
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
}
