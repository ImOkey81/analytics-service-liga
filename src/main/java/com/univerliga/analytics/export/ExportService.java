package com.univerliga.analytics.export;

import com.univerliga.analytics.dto.SubcategoryFrequencyResponse;
import com.univerliga.analytics.service.ReportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExportService {

    private final ReportService reportService;

    public ExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] export(ExportFormat format, LocalDate from, LocalDate to, String departmentId, String teamId, String personId, ExportMode mode) {
        SubcategoryFrequencyResponse response = reportService.subcategoryFrequency(from, to, departmentId, teamId, personId, null, 200, "total");
        return switch (format) {
            case csv -> asCsv(response.items(), from, to);
            case xlsx -> asXlsx(response.items(), from, to);
        };
    }

    private byte[] asCsv(List<SubcategoryFrequencyResponse.Item> items, LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        sb.append("categoryName,subcategoryName,positive,negative,total,avgRating,periodFrom,periodTo\n");
        for (SubcategoryFrequencyResponse.Item item : items) {
            sb.append("N/A").append(',')
                    .append(escape(item.subcategoryName())).append(',')
                    .append(item.positive()).append(',')
                    .append(item.negative()).append(',')
                    .append(item.total()).append(',')
                    .append("0.00").append(',')
                    .append(from).append(',')
                    .append(to)
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] asXlsx(List<SubcategoryFrequencyResponse.Item> items, LocalDate from, LocalDate to) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("aggregated");
            Row header = sheet.createRow(0);
            String[] cols = {"categoryName", "subcategoryName", "positive", "negative", "total", "avgRating", "periodFrom", "periodTo"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int row = 1;
            for (SubcategoryFrequencyResponse.Item item : items) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue("N/A");
                r.createCell(1).setCellValue(item.subcategoryName());
                r.createCell(2).setCellValue(item.positive());
                r.createCell(3).setCellValue(item.negative());
                r.createCell(4).setCellValue(item.total());
                r.createCell(5).setCellValue(0.0);
                r.createCell(6).setCellValue(from.toString());
                r.createCell(7).setCellValue(to.toString());
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate XLSX", e);
        }
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
