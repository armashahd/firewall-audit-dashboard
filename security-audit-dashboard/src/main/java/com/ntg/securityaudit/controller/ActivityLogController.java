package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.ActivityLog;
import com.ntg.securityaudit.service.ActivityLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class ActivityLogController {

    private static final DateTimeFormatter EXPORT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/activity-logs")
    public String listActivityLogs(Model model) {
        model.addAttribute("logs", activityLogService.getAllLogs());
        return "activity-logs";
    }

    @GetMapping("/activity-logs/export.csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=activity-logs.csv");
        response.getWriter().println("Date/Time,Username,Role,Action Type,Entity Type,Entity ID/Name,Old Value,New Value,Comment");
        for (ActivityLog log : activityLogService.getAllLogs()) {
            response.getWriter().println(String.join(",",
                    csv(log.getCreatedAt() != null ? log.getCreatedAt().format(EXPORT_FORMAT) : ""),
                    csv(log.getUsername()),
                    csv(log.getRole()),
                    csv(log.getActionType()),
                    csv(log.getEntityType()),
                    csv(entityLabel(log)),
                    csv(log.getOldValue()),
                    csv(log.getNewValue()),
                    csv(log.getComment())
            ));
        }
    }

    @GetMapping("/activity-logs/print")
    public String printActivityLogs(Model model) {
        List<ActivityLog> logs = activityLogService.getAllLogs();
        model.addAttribute("logs", logs);
        return "activity-logs-print";
    }

    private String entityLabel(ActivityLog log) {
        String id = log.getEntityId() != null ? log.getEntityId() : "";
        String name = log.getEntityName() != null ? log.getEntityName() : "";
        if (!id.isBlank() && !name.isBlank()) {
            return id + " / " + name;
        }
        return !id.isBlank() ? id : name;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
