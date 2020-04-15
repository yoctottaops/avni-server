package org.openchs.importer.batch.csv.creator;

import org.joda.time.LocalDate;
import org.openchs.importer.batch.model.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DateCreator {

    private static Logger logger = LoggerFactory.getLogger(DateCreator.class);

    public LocalDate getDate(Row row, String header, List<String> errorMsgs, String errorMessageIfNotExists) {
        try {
            String date = row.get(header);
            if (date != null) {
                return LocalDate.parse(date);
            }

            if (date == null && errorMessageIfNotExists != null) {
                errorMsgs.add(errorMessageIfNotExists);
            }
        } catch (Exception ex) {
            logger.error(String.format("Error processing row %s", row), ex);
            errorMsgs.add(String.format("Invalid '%s'", header));
        } finally {
            return null;
        }
    }
}
