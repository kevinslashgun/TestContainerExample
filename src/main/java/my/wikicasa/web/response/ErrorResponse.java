package my.wikicasa.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatusCode status;
    private String message;
}
