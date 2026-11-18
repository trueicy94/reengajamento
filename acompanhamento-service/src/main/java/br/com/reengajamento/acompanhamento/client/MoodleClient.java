package br.com.reengajamento.acompanhamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.JsonNode;

@FeignClient(
        name = "moodleClient",
        url = "${moodle.url}"
)
public interface MoodleClient {

    @GetMapping("/webservice/rest/server.php")
    JsonNode getAssignmets(
            @RequestParam("wstoken") String token,
            @RequestParam("wsfunction") String function,
            @RequestParam("moodlewsrestformat") String format,
            @RequestParam("courseids[0]") Long courseId
    );

    @GetMapping("/webservice/rest/server.php")
    JsonNode getEnrolledUsers(
            @RequestParam("wstoken") String token,
            @RequestParam("wsfunction") String function,
            @RequestParam("moodlewsrestformat") String format,
            @RequestParam("courseid") Long courseId
    );

    @GetMapping("/webservice/rest/server.php")
    JsonNode getSubmissions(
            @RequestParam("wstoken") String token,
            @RequestParam("wsfunction") String function,
            @RequestParam("moodlewsrestformat") String format,
            @RequestParam("assignmentids[0]") Long assignmentId
    );
}

